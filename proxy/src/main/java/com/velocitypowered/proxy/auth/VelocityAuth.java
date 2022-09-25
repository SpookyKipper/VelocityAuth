/*
 * Copyright (C) 2018 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.velocitypowered.proxy.auth;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.auth.commands.*;
import com.velocitypowered.proxy.auth.database.BannedUser;
import com.velocitypowered.proxy.auth.database.Database;
import com.velocitypowered.proxy.auth.database.RegisteredUser;
import com.velocitypowered.proxy.auth.database.Session;
import com.velocitypowered.proxy.auth.perms.NoPermissionPlayer;
import com.velocitypowered.proxy.auth.utils.UtilsTime;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Auth manager for velocity, which was originally a plugin.
 *
 * @author Osiris-Team, RGoth, HasX
 */
public class VelocityAuth implements PluginContainer {
    public static VelocityAuth INSTANCE;

    public final ProxyServer proxy;
    public final Logger logger;
    public final File authDirectory;
    public boolean isWhitelistMode = false;
    public int sessionMaxHours;
    public List<NoPermissionPlayer> noPermissionPlayers = new CopyOnWriteArrayList<>();
    public int minFailedLoginsForBan;
    public int failedLoginBanTimeSeconds;
    public int minPasswordLength;
    public ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    public LimboServer authServer;

    public VelocityAuth(VelocityServer proxy, Logger logger, File authDirectory) throws Exception {
        INSTANCE = this;
        this.proxy = proxy;
        this.logger = logger;
        this.authDirectory = authDirectory;
        if (!authDirectory.isDirectory()) throw new IllegalArgumentException(authDirectory + " must be a directory!");
        authDirectory.mkdirs();

        long now = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        Config config = new Config();
        if (config.databaseUsername.asString() == null) {
            logger.info("Welcome! Looks like this is your first run.");
            logger.info("This plugin requires access to your SQL database.");
            logger.info("Please enter your SQL database username below and press enter:");
            String username = null;
            while (username == null || username.trim().isEmpty()) {
                username = new Scanner(System.in).nextLine();
            }
            config.databaseUsername.setValues(username);
            config.save();
        }
        if (config.databasePassword.asString() == null) {
            logger.info("Please enter your SQL database password below and press enter:");
            String password = null;
            while (password == null || password.trim().isEmpty()) {
                password = new Scanner(System.in).nextLine();
            }
            config.databasePassword.setValues(password);
            config.save();
        }

        Database.rawUrl = config.databaseRawUrl.asString();
        Database.url = config.databaseUrl.asString();
        Database.username = config.databaseUsername.asString();
        Database.password = config.databasePassword.asString();
        isWhitelistMode = config.whitelistMode.asBoolean();
        sessionMaxHours = config.sessionMaxHours.asInt();
        minFailedLoginsForBan = config.minFailedLoginsForBan.asInt();
        failedLoginBanTimeSeconds = config.failedLoginBanTime.asInt();
        minPasswordLength = config.minPasswordLength.asInt();
        logger.info("Loaded configuration. " + (System.currentTimeMillis() - now) + "ms");
        now = System.currentTimeMillis();

        /*
        limboAPI = (LimboAPI) proxy.getPluginManager().getPlugin("limbo").get().getInstance().get();
        limboServer = (LimboImpl) limboAPI.createLimbo(limboAPI.createVirtualWorld(Dimension.THE_END, 0, 0, 0, 0, 0));
        limboServer.setName("auth");
        limboServer.setGameMode(GameMode.SPECTATOR);
        LoginCommand loginCommand = new LoginCommand();
        limboServer.registerCommand(loginCommand.meta(), loginCommand);
        RegisterCommand registerCommand = new RegisterCommand();
        limboServer.registerCommand(registerCommand.meta(), registerCommand);
         */
        // Note that registered commands kinda don't get executed by the limbo
        // without having the AuthSessionHandler/MySessionHandler for the player when sending to the limbo.

        authServer = new LimboServer();
        authServer.startNanoLimbo();

        logger.info("Started virtual limbo auth-server. "
                + (System.currentTimeMillis() - now) + "ms");
        now = System.currentTimeMillis();

        Database.create();
        logger.info("Database connected. " + (System.currentTimeMillis() - now) + "ms");
        now = System.currentTimeMillis();

        // Set all sessions to inactive at start
        for (Session session : Session.whereIsActive().is(1).get()) {
            session.isActive = 0;
            Session.update(session);
        }

        // Events below are registered in the order they get executed:

        proxy.getEventManager().register(this, PreLoginEvent.class, PostOrder.FIRST, e -> {
            try {
                if (isWhitelistMode && !isRegistered(e.getUsername())) {
                    e.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                            Component.text("You must be registered to join this server!")
                    ));
                    logger.info("Blocked connection for " + e.getUsername() + ". Player not registered (whitelist-mode).");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        proxy.getEventManager().register(this, LoginEvent.class, PostOrder.FIRST, e -> {
            try {
                if (BannedUser.isBanned(getPlayerIp(e.getPlayer()), e.getPlayer().getUniqueId().toString())) {
                    BannedUser bannedUser = BannedUser.getBanned(getPlayerIp(e.getPlayer()), e.getPlayer().getUniqueId().toString());
                    Component message = new BanCommand().getBanText(bannedUser.timestampExpires, bannedUser.reason);
                    e.getPlayer().disconnect(message);
                    logger.info("Blocked connection for " + e.getPlayer().getUsername() + "/" + e.getPlayer().getUniqueId().toString()
                            + ". Player is banned for " + new UtilsTime().getFormattedString(bannedUser.timestampExpires) + ".");
                    return;
                }
                if (e.getPlayer().isOnlineMode()) { // Handle paid player
                    if (isRegistered(e.getPlayer().getUsername())) {
                        String error = new AdminLoginCommand().execute(e.getPlayer().getUsername(), "", getPlayerIp(e.getPlayer()));
                        if (error != null) {
                            if (error.contains("Invalid credentials")) {
                                // Means that this account has a password even though
                                // this is not required (for paid players), which means
                                // a cracked player was using this username before and created
                                // the account. This paid player however has priority
                                // thus the cracked player will lose access to this account.
                                RegisteredUser registeredUser = getRegisteredUser(e.getPlayer());
                                registeredUser.password = "";
                                RegisteredUser.update(registeredUser);
                                error = new AdminLoginCommand().execute(e.getPlayer().getUsername(), "", getPlayerIp(e.getPlayer()));
                                if (error != null) e.getPlayer().disconnect(Component.text(error));
                            } else
                                e.getPlayer().disconnect(Component.text(error));
                        }
                        // Successfully logged in.
                    } else { // Not registered yet
                        String error = new AdminRegisterCommand().execute(e.getPlayer().getUsername(), "", true);
                        if (error != null) e.getPlayer().disconnect(Component.text(error));
                        error = new AdminLoginCommand().execute(e.getPlayer().getUsername(), "", getPlayerIp(e.getPlayer()));
                        if (error != null) e.getPlayer().disconnect(Component.text(error));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                e.getPlayer().disconnect(Component.text(ex.getMessage()));
            }
        });
        proxy.getEventManager().register(this, ServerPreConnectEvent.class, PostOrder.FIRST, e -> {
            try {

                ConnectedPlayer player = (ConnectedPlayer) e.getPlayer();

                // Forward to limbo server for login/registration
                // This server allows multiple players with the same username online
                // at the same time and thus is perfect for safe authentication
                // on offline (as well as online) servers.
                if (!hasValidSession(player)) {
                    // Remove all permissions of the user (except login/register commands), if not logged in
                    // and restore them later, when logged in.
                    noPermissionPlayers.add(new NoPermissionPlayer(
                            player,
                            NoPermissionPlayer.tempPermissionFunction,
                            player.getPermissionFunction()));
                    player.setPermissionFunction(NoPermissionPlayer.tempPermissionFunction);

                    e.setResult(ServerPreConnectEvent.ServerResult.allowed(authServer.registeredServer));
                    //e.setResult(ServerPreConnectEvent.ServerResult.denied());
                    //limboServer.spawnPlayer(player, new LimboAuthSessionHandler());
                    logger.info("Blocked connect to '" + e.getOriginalServer().getServerInfo().getName()
                            + "' and forwarded " + player.getUsername() + " to authentication. Player not logged in.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        proxy.getEventManager().register(this, ServerConnectedEvent.class, PostOrder.FIRST, e -> {
            onServerConnect(e.getPlayer());
        });
        proxy.getEventManager().register(this, DisconnectEvent.class, PostOrder.LAST, e -> {
            try {
                long now2 = System.currentTimeMillis();
                for (Session session : Session.get("username=?", e.getPlayer().getUsername())) {
                    session.isActive = 0;
                    if (now2 > session.timestampExpires)
                        Session.remove(session);
                    else
                        Session.update(session);
                }
                noPermissionPlayers.removeIf(perm -> Objects.equals(perm.player.getUniqueId(), e.getPlayer().getUniqueId()));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        logger.info("Listeners registered. " + (System.currentTimeMillis() - now) + "ms");
        now = System.currentTimeMillis();

        new AdminRegisterCommand().register();
        new AdminUnRegisterCommand().register();
        new AdminLoginCommand().register();
        new RegisterCommand().register(); // Only available in virtual limbo auth server
        new LoginCommand().register(); // Only available in virtual limbo auth server
        new BanCommand().register();
        new UnbanCommand().register();
        new ListSessionsCommand().register();
        new ClearSessionsCommand().register();
        logger.info("Commands registered. " + (System.currentTimeMillis() - now) + "ms");

        logger.info("Initialised successfully! " + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public PluginDescription getDescription() {
        return new PluginDescription() {
            @Override
            public String getId() {
                return "auth";
            }

            @Override
            public Optional<String> getName() {
                return Optional.of("VelocityAuth");
            }

            @Override
            public Optional<String> getVersion() {
                return Optional.of("internal");
            }

            @Override
            public Optional<String> getDescription() {
                return Optional.of("Manages authentication of players.");
            }

            @Override
            public List<String> getAuthors() {
                return Collections.singletonList("Osiris-Team");
            }
        };
    }

    @Override
    public Optional<?> getInstance() {
        return Optional.of(this);
    }

    public String getPlayerIp(Player player) {
        return player.getRemoteAddress().getAddress().getHostAddress();
    }

    public boolean hasValidSession(Player player) throws Exception {
        return getActiveSession(player) != null;
    }

    public Session getActiveSession(Player player) throws Exception {
        return getActiveSession(player.getUsername(), getPlayerIp(player));
    }

    public boolean hasValidSession(String username, String ipAddress) throws Exception {
        return getActiveSession(username, ipAddress) != null;
    }

    /**
     * Returns true, if this username/ip-address has no session, aka
     * the player never logged in, or another older session expired.
     */
    public Session getActiveSession(String username, String ipAddress) throws Exception {
        List<Session> sessions = Session.get("username=? AND ipAddress=? AND isActive=1", username, ipAddress);
        if (sessions.isEmpty()) {
            return null;
        }
        if (sessions.size() > 1) throw new RuntimeException("Cannot have multiple(" + sessions.size()
                + ") sessions for one username(" + username + ")/ip-address(" + ipAddress + ").");
        return sessions.get(0);
    }

    public Player findPlayerByUsername(String username) {
        Player player = null;
        for (Player p : proxy.getAllPlayers()) {
            if (Objects.equals(p.getUsername(), username)) {
                player = p;
                break;
            }
        }
        return player;
    }

    /**
     * Checks whether the provided username exists in the database.
     */
    public boolean isRegistered(String username) throws Exception {
        return !RegisteredUser.get("username=?", username).isEmpty();
    }

    private RegisteredUser getRegisteredUser(Player player) throws Exception {
        return RegisteredUser.get("username=?", player.getUsername()).get(0);
    }

    public void onServerConnect(Player player) {
        VelocityAuth.INSTANCE.executor.execute(() -> {
            try {
                int maxSeconds = 60;
                for (int i = maxSeconds; i >= 0; i--) {
                    if (!player.isActive() || VelocityAuth.INSTANCE.isRegistered(player.getUsername())) break;
                    player.sendActionBar(Component.text(i + " seconds remaining to: /register <password> <confirm-password>",
                            TextColor.color(184, 25, 43)));
                    if (i == 0) {
                        player.disconnect(Component.text("Please register within " + maxSeconds + " seconds after joining the server.",
                                TextColor.color(184, 25, 43)));
                    }
                    Thread.sleep(1000);
                }
                for (int i = maxSeconds; i >= 0; i--) {
                    if (!player.isActive() || VelocityAuth.INSTANCE.hasValidSession(player))
                        break;
                    player.sendActionBar(Component.text(i + " seconds remaining to: /login <password>", TextColor.color(184, 25, 43)));
                    if (i == 0) {
                        player.disconnect(Component.text("Please login within " + maxSeconds + " seconds after joining the server.",
                                TextColor.color(184, 25, 43)));
                    }
                    Thread.sleep(1000);
                }

                for (int i = maxSeconds; i >= 0; i--) {
                    ServerConnection con = player.getCurrentServer().orElse(null);
                    if (con == null || // Already disconnected
                            !Objects.equals(con.getServer(), authServer.registeredServer))
                        break;
                    player.sendActionBar(Component.text(i + " seconds remaining to join another server", TextColor.color(184, 25, 43)));
                    if (i == 0) {
                        player.disconnect(Component.text("Please join another server within " + maxSeconds + " seconds after logging in.",
                                TextColor.color(184, 25, 43)));
                    }
                    Thread.sleep(1000);
                }

                Session session = VelocityAuth.INSTANCE.getActiveSession(player);
                if (session != null) {
                    session.isActive = 1;
                    Session.update(session);
                }
            } catch (InterruptedException ignored) {
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
