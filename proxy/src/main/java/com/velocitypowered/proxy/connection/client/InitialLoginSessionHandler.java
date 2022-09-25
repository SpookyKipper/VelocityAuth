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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.connection.client;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import com.google.gson.JsonObject;
import com.osiris.autoplug.core.json.Json;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.auth.VelocityAuth;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.crypto.IdentifiedKeyImpl;
import com.velocitypowered.proxy.protocol.netty.MinecraftDecoder;
import com.velocitypowered.proxy.protocol.packet.EncryptionRequest;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.LoginPluginResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;
import static com.velocitypowered.proxy.VelocityServer.GENERAL_GSON;
import static com.velocitypowered.proxy.connection.VelocityConstants.EMPTY_BYTE_ARRAY;
import static com.velocitypowered.proxy.crypto.EncryptionUtils.decryptRsa;
import static com.velocitypowered.proxy.crypto.EncryptionUtils.generateServerId;

public class InitialLoginSessionHandler implements MinecraftSessionHandler {

    static class CachedResult{
        public String usernameOrUuid;
        public boolean isValid;

        public CachedResult(String usernameOrUuid, boolean isValid) {
            this.usernameOrUuid = usernameOrUuid;
            this.isValid = isValid;
        }
    }

    static final String MOJANG_BASE = "https://api.mojang.com/users/profiles/minecraft/";
    static final String MINETOOLS_BASE = "https://api.minetools.eu/uuid/";
    static final Queue<CachedResult> cachedResults = new ArrayDeque<>(100);
    private static final Logger logger = LogManager.getLogger(InitialLoginSessionHandler.class);
    private static final String MOJANG_HASJOINED_URL =
            System.getProperty("mojang.sessionserver", "https://sessionserver.mojang.com/session/minecraft/hasJoined")
                    .concat("?username=%s&serverId=%s");
    private final VelocityServer server;
    private final MinecraftConnection mcConnection;
    private final LoginInboundConnection inbound;
    private @MonotonicNonNull ServerLogin login;
    private byte[] verify = EMPTY_BYTE_ARRAY;
    private LoginState currentState = LoginState.LOGIN_PACKET_EXPECTED;
    private final boolean forceKeyAuthentication;

    InitialLoginSessionHandler(VelocityServer server, MinecraftConnection mcConnection,
                               LoginInboundConnection inbound) {
        this.server = Preconditions.checkNotNull(server, "server");
        this.mcConnection = Preconditions.checkNotNull(mcConnection, "mcConnection");
        this.inbound = Preconditions.checkNotNull(inbound, "inbound");
        this.forceKeyAuthentication = System.getProperties().containsKey("auth.forceSecureProfiles")
                ? Boolean.getBoolean("auth.forceSecureProfiles") : server.getConfiguration().isForceKeyAuthentication();
    }

    @Override
    public boolean handle(ServerLogin packet) {
        assertState(LoginState.LOGIN_PACKET_EXPECTED);
        this.currentState = LoginState.LOGIN_PACKET_RECEIVED;
        VelocityAuth.INSTANCE.logger.debug("Received login packet.");
        IdentifiedKey playerKey = packet.getPlayerKey();
        if (playerKey != null) {
            if (playerKey.hasExpired()) {
                inbound.disconnect(Component.translatable("multiplayer.disconnect.invalid_public_key_signature"));
                return true;
            }

            boolean isKeyValid;
            if (playerKey.getKeyRevision() == IdentifiedKey.Revision.LINKED_V2
                    && playerKey instanceof IdentifiedKeyImpl) {
                IdentifiedKeyImpl keyImpl = (IdentifiedKeyImpl) playerKey;
                isKeyValid = keyImpl.internalAddHolder(packet.getHolderUuid());
            } else {
                isKeyValid = playerKey.isSignatureValid();
            }

            if (!isKeyValid) {
                inbound.disconnect(Component.translatable("multiplayer.disconnect.invalid_public_key"));
                return true;
            }
        } else if (mcConnection.getProtocolVersion().compareTo(ProtocolVersion.MINECRAFT_1_19) >= 0
                && forceKeyAuthentication) {
            inbound.disconnect(Component.translatable("multiplayer.disconnect.missing_public_key"));
            return true;
        }
        inbound.setPlayerKey(playerKey);
        this.login = packet;

        PreLoginEvent event = new PreLoginEvent(inbound, login.getUsername());
        server.getEventManager().fire(event)
                .thenRunAsync(() -> {
                    if (mcConnection.isClosed()) {
                        // The player was disconnected
                        return;
                    }

                    PreLoginComponentResult result = event.getResult();
                    Optional<Component> disconnectReason = result.getReasonComponent();
                    if (disconnectReason.isPresent()) {
                        // The component is guaranteed to be provided if the connection was denied.
                        inbound.disconnect(disconnectReason.get());
                        return;
                    }

                    inbound.loginEventFired(() -> {
                        if (mcConnection.isClosed()) {
                            // The player was disconnected
                            return;
                        }

                        if (mcConnection.getProtocolVersion().compareTo(ProtocolVersion.MINECRAFT_1_19_1) >= 0) {
                            VelocityAuth.INSTANCE.logger.debug("client >= 1.19.1");
                            // 1.19.1 and above
                            // online-mode players: 100% OK
                            // offline-mode players: 100% OK
                            mcConnection.eventLoop().execute(() -> {
                                if (packet.getHolderUuid() == null ||
                                !isValidAccount(packet.getHolderUuid().toString())) { // Probably a connection from an offline/cracked player
                                    VelocityAuth.INSTANCE.logger.debug("client = offline");
                                    if (server.getConfiguration().isOnlineMode()) {
                                        inbound.disconnect(Component.text("This server does not allow offline-mode players."));
                                        return;
                                    }
                                    connectOfflineMode();
                                } else { // Probably a connection from an online/regular player
                                    VelocityAuth.INSTANCE.logger.debug("client = online");
                                    sendEncryptionRequest();
                                }
                            });
                        } else {
                            VelocityAuth.INSTANCE.logger.debug("client < 1.19.1");
                            // 1.19.0 and below
                            // online-mode players: 100% OK
                            // offline-mode players: 99% OK
                            // Work on all Minecraft clients, some will get "Invalid Session" error and directly disconnect from server,
                            // if they are not premium players with nickname from some premium account the encryption request was sent.
                            mcConnection.eventLoop().execute(() -> {
                                if (isValidAccount(login.getUsername())) { // Probably a connection from an online/regular player
                                    VelocityAuth.INSTANCE.logger.debug("client = online");
                                    byte[] oldVerify = (this.verify != null ? Arrays.copyOf(this.verify, this.verify.length) : null);
                                    LoginState oldState = this.currentState;
                                    sendEncryptionRequest();

                                    if (!server.getConfiguration().isOnlineMode()) {
                                        VelocityAuth.INSTANCE.executor.execute(() -> {
                                            try {
                                                for (int i = 0; i < 30; i++) { // 3 seconds max
                                                    Thread.sleep(100);
                                                    if (this.currentState == LoginState.ENCRYPTION_RESPONSE_RECEIVED) {
                                                        // Means we received a response which is handled in the
                                                        // handle(EncryptionResponse packet) method further below.
                                                        // Thus, our job here is done, and we return.
                                                        return;
                                                    }
                                                }
                                                VelocityAuth.INSTANCE.logger.warn("client = offline");
                                                mcConnection.eventLoop().execute(() -> {
                                                    // Means that we didn't receive a response for the encryption within 3 seconds
                                                    // thus continue with offline mode login instead:
                                                    this.verify = oldVerify;
                                                    this.currentState = oldState;
                                                    connectOfflineMode();
                                                });
                                            } catch (Exception e) {
                                                logger.error("Exception in pre-login stage", e);
                                            }
                                        });
                                    }

                                } else { // Probably a connection from an offline/cracked player
                                    connectOfflineMode();
                                }
                            });
                        }
                    });
                }, mcConnection.eventLoop())
                .exceptionally((ex) -> {
                    logger.error("Exception in pre-login stage", ex);
                    return null;
                });

        return true;
    }

    private void connectOfflineMode() {
        if (server.getConfiguration().isOnlineMode()) {
            inbound.disconnect(Component.text("This server does not allow offline-mode players."));
        } else {
            mcConnection.setSessionHandler(new AuthSessionHandler(
                    server, inbound, GameProfile.forOfflinePlayer(login.getUsername()), false
            ));
        }
    }

    private void sendEncryptionRequest() {
        EncryptionRequest request = generateEncryptionRequest();
        this.verify = Arrays.copyOf(request.getVerifyToken(), 4);
        mcConnection.write(request);
        this.currentState = LoginState.ENCRYPTION_REQUEST_SENT;
    }

    /**
     * Returns true if the provided username is registered
     * at Mojang and false if not.
     * @param usernameOrUuid Mojang username or uuid.
     */
    public static boolean isValidAccount(String usernameOrUuid) {
        // Check cache first
        synchronized (cachedResults){
            for (CachedResult cachedResult : cachedResults) {
                if(Objects.equals(cachedResult.usernameOrUuid, usernameOrUuid)){
                    return cachedResult.isValid;
                }
            }
        }
        // Check Mojang API
        try {
            Json.fromUrlAsObject(MOJANG_BASE + usernameOrUuid);
            synchronized (cachedResults){
                if(cachedResults.size()>=100) cachedResults.poll();
                cachedResults.add(new CachedResult(usernameOrUuid, true));
            }
            return true;
        } catch (Exception ignored) {
            // Always throws exception when status != 200, which happens when the
            // username does not exist in the Mojang database, or the API is down
        }

        // Fallback to Minetools API
        try {
            JsonObject obj = Json.fromUrlAsObject(MINETOOLS_BASE + usernameOrUuid);
            // If the username does not exist this status has ERR
            if (obj.get("status").getAsString().equals("ERR")) {
                synchronized (cachedResults){
                    if(cachedResults.size()>=100) cachedResults.poll();
                    cachedResults.add(new CachedResult(usernameOrUuid, false));
                }
                return false;
            }
            String apiUsername = obj.get("name").getAsString();
            boolean result = Objects.equals(usernameOrUuid, apiUsername);
            synchronized (cachedResults){
                if(cachedResults.size()>=100) cachedResults.poll();
                cachedResults.add(new CachedResult(usernameOrUuid, result));
            }
            return result;
        } catch (Exception e) { // Means that both APIs are down
            logger.error("Exception when trying to get Minecraft profile for " + usernameOrUuid + ". Mojang and Minetools APIs probably down.", e);
        }

        synchronized (cachedResults){
            if(cachedResults.size()>=100) cachedResults.poll();
            cachedResults.add(new CachedResult(usernameOrUuid, false));
        }
        return false;
    }

    @Override
    public boolean handle(LoginPluginResponse packet) {
        this.inbound.handleLoginPluginResponse(packet);
        return true;
    }

    @Override
    public boolean handle(EncryptionResponse packet) {
        assertState(LoginState.ENCRYPTION_REQUEST_SENT);
        this.currentState = LoginState.ENCRYPTION_RESPONSE_RECEIVED;
        ServerLogin login = this.login;
        if (login == null) {
            throw new IllegalStateException("No ServerLogin packet received yet.");
        }

        if (verify.length == 0) {
            throw new IllegalStateException("No EncryptionRequest packet sent yet.");
        }

        try {
            KeyPair serverKeyPair = server.getServerKeyPair();
            if (inbound.getIdentifiedKey() != null) {
                IdentifiedKey playerKey = inbound.getIdentifiedKey();
                if (!playerKey.verifyDataSignature(packet.getVerifyToken(), verify, Longs.toByteArray(packet.getSalt()))) {
                    throw new IllegalStateException("Invalid client public signature.");
                }
            } else {
                byte[] decryptedVerifyToken = decryptRsa(serverKeyPair, packet.getVerifyToken());
                if (!MessageDigest.isEqual(verify, decryptedVerifyToken)) {
                    throw new IllegalStateException("Unable to successfully decrypt the verification token.");
                }
            }

            byte[] decryptedSharedSecret = decryptRsa(serverKeyPair, packet.getSharedSecret());
            String serverId = generateServerId(decryptedSharedSecret, serverKeyPair.getPublic());

            String playerIp = ((InetSocketAddress) mcConnection.getRemoteAddress()).getHostString();
            String url = String.format(MOJANG_HASJOINED_URL,
                    urlFormParameterEscaper().escape(login.getUsername()), serverId);

            if (server.getConfiguration().shouldPreventClientProxyConnections()) {
                url += "&ip=" + urlFormParameterEscaper().escape(playerIp);
            }

            ListenableFuture<Response> hasJoinedResponse = server.getAsyncHttpClient().prepareGet(url)
                    .execute();
            hasJoinedResponse.addListener(() -> {
                if (mcConnection.isClosed()) {
                    // The player disconnected after we authenticated them.
                    return;
                }

                // Go ahead and enable encryption. Once the client sends EncryptionResponse, encryption
                // is enabled.
                try {
                    mcConnection.enableEncryption(decryptedSharedSecret);
                } catch (GeneralSecurityException e) {
                    logger.error("Unable to enable encryption for connection", e);
                    // At this point, the connection is encrypted, but something's wrong on our side and
                    // we can't do anything about it.
                    mcConnection.close(true);
                    return;
                }

                try {
                    Response profileResponse = hasJoinedResponse.get();
                    if (profileResponse.getStatusCode() == 200) {
                        final GameProfile profile = GENERAL_GSON.fromJson(profileResponse.getResponseBody(), GameProfile.class);
                        // Not so fast, now we verify the public key for 1.19.1+
                        if (inbound.getIdentifiedKey() != null
                                && inbound.getIdentifiedKey().getKeyRevision() == IdentifiedKey.Revision.LINKED_V2
                                && inbound.getIdentifiedKey() instanceof IdentifiedKeyImpl) {
                            IdentifiedKeyImpl key = (IdentifiedKeyImpl) inbound.getIdentifiedKey();
                            if (!key.internalAddHolder(profile.getId())) {
                                inbound.disconnect(Component.translatable("multiplayer.disconnect.invalid_public_key"));
                            }
                        }

                        // All went well, initialize the session.
                        mcConnection.setSessionHandler(new AuthSessionHandler(
                                server, inbound, profile, true
                        ));
                    } else if (profileResponse.getStatusCode() == 204) {
                        // Apparently an offline-mode user logged onto this online-mode proxy.
                        inbound.disconnect(Component.translatable("velocity.error.online-mode-only",
                                NamedTextColor.RED));
                    } else {
                        // Something else went wrong
                        logger.error(
                                "Got an unexpected error code {} whilst contacting Mojang to log in {} ({})",
                                profileResponse.getStatusCode(), login.getUsername(), playerIp);
                        inbound.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                    }

                } catch (ExecutionException e) {
                    logger.error("Unable to authenticate with Mojang", e);
                    inbound.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                } catch (InterruptedException e) {
                    // not much we can do usefully
                    Thread.currentThread().interrupt();
                }
            }, mcConnection.eventLoop());
        } catch (GeneralSecurityException e) {
            logger.error("Unable to enable encryption", e);
            mcConnection.close(true);
        }
        return true;
    }

    private EncryptionRequest generateEncryptionRequest() {
        byte[] verify = new byte[4];
        ThreadLocalRandom.current().nextBytes(verify);

        EncryptionRequest request = new EncryptionRequest();
        request.setPublicKey(server.getServerKeyPair().getPublic().getEncoded());
        request.setVerifyToken(verify);
        return request;
    }

    @Override
    public void handleUnknown(ByteBuf buf) {
        mcConnection.close(true);
    }

    @Override
    public void disconnected() {
        this.inbound.cleanup();
    }

    private void assertState(LoginState expectedState) {
        if (this.currentState != expectedState) {
            if (MinecraftDecoder.DEBUG) {
                logger.error("{} Received an unexpected packet requiring state {}, but we are in {}", inbound,
                        expectedState, this.currentState);
            }
            mcConnection.close(true);
        }
    }

    private enum LoginState {
        LOGIN_PACKET_EXPECTED,
        LOGIN_PACKET_RECEIVED,
        ENCRYPTION_REQUEST_SENT,
        ENCRYPTION_RESPONSE_RECEIVED
    }
}
