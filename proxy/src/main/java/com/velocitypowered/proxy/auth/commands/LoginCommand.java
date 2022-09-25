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

package com.velocitypowered.proxy.auth.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.auth.VelocityAuth;
import com.velocitypowered.proxy.auth.database.FailedLogin;
import com.velocitypowered.proxy.auth.perms.NoPermissionPlayer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Objects;

public class LoginCommand implements Command {
    @Override
    public String name() {
        return "login";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.login";
    }

    @Override
    public String execute(Object... args) throws Exception {
        return null;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        VelocityAuth.INSTANCE.executor.execute(() -> {
            if (source instanceof Player) {
                Player player = (Player) source;
                try {
                    if (FailedLogin.get("(uuid=? OR ipAddress=?) AND timestamp > (?-60000)", player.getUniqueId().toString(),
                                    VelocityAuth.INSTANCE.getPlayerIp(player),
                                    System.currentTimeMillis())
                            .size() >= VelocityAuth.INSTANCE.minFailedLoginsForBan) {
                        new BanCommand().execute(player.getUsername(), player.getUniqueId().toString(), player.getRemoteAddress().getAddress().getHostAddress(),
                                System.currentTimeMillis() + (VelocityAuth.INSTANCE.failedLoginBanTimeSeconds * 1000L), "Too many failed login attempts.");
                    }
                    if (args.length != 1) {
                        sendFailedLogin(player, "Failed! Requires 1 argument: <password>");
                        return;
                    }
                    String password = args[0];
                    try {
                        if (password == null || password.trim().isEmpty()) {
                            sendFailedLogin(player, "Failed! Password cannot be null/empty.");
                            return;
                        }
                        String error = new AdminLoginCommand().execute(player.getUsername(), password,
                                VelocityAuth.INSTANCE.getPlayerIp(player));
                        if (error == null) {
                            source.sendMessage(Component.text("Logged in!"));
                        } else {
                            sendFailedLogin(player, error);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendFailedLogin(player, "Failed! Details could not be added to the database.");
                        return;
                    }

                    // Restore default permission function
                    try {
                        for (NoPermissionPlayer perm : VelocityAuth.INSTANCE.noPermissionPlayers) {
                            if (Objects.equals(perm.player.getUniqueId(), player.getUniqueId())) {
                                VelocityAuth.INSTANCE.noPermissionPlayers.remove(perm);
                                ((ConnectedPlayer) perm.player).setPermissionFunction(perm.oldPermissionFunction);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendFailedLogin(player, "Failed! " + e.getMessage());
                    }

                    // Forward user to first server
                    for (RegisteredServer s : VelocityAuth.INSTANCE.proxy.getAllServers()) {
                        player.createConnectionRequest(s).fireAndForget();
                        return;
                    }
                    source.sendMessage(Component.text("Unable to forward to another server, because there aren't any.", TextColor.color(255, 0, 0)));
                } catch (Exception e) {
                    e.printStackTrace();
                    sendFailedLogin(player, "Failed! " + e.getMessage());
                }
            } else
                VelocityAuth.INSTANCE.logger.error("Failed! Must be player to execute this command.");
        });
    }

    private void sendFailedLogin(Player player, String reason) {
        try {
            player.sendMessage(Component.text(reason, TextColor.color(255, 0, 0)));
            FailedLogin.add(FailedLogin.create(player.getUsername(),
                    player.getRemoteAddress().getAddress().getHostAddress(),
                    System.currentTimeMillis(), reason, player.getUniqueId().toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
