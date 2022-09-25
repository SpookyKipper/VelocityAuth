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
import com.velocitypowered.proxy.auth.VelocityAuth;
import com.velocitypowered.proxy.auth.database.BannedUser;
import com.velocitypowered.proxy.auth.utils.Arr;
import com.velocitypowered.proxy.auth.utils.UtilsTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.time.Instant;
import java.util.Objects;

public final class BanCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        Arr<String> args = new Arr<>(invocation.arguments());
        if (args.length < 1) {
            source.sendMessage(Component.text("Failed! Requires minimum 1 argument: <username> (<hours> <reason>)"));
            return;
        }
        String username = args.get(0);
        long timestampExpires = args.get(1) != null ? System.currentTimeMillis() + (Long.parseLong(args.get(1)) * 3600000) :
                System.currentTimeMillis() + 86400000; // 24h
        String reason = args.get(2) != null ? args.toPrintString(2, args.length - 1) : "Your behavior violated our community guidelines and/or terms of service.";

        try {
            Player bannedPlayer = VelocityAuth.INSTANCE.findPlayerByUsername(username);
            String error = execute(username, bannedPlayer.getUniqueId().toString(),
                    VelocityAuth.INSTANCE.getPlayerIp(bannedPlayer), timestampExpires, reason);
            if (error == null) {
                source.sendMessage(Component.text("Ban success!"));
            } else {
                source.sendMessage(Component.text(error, TextColor.color(255, 0, 0)));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            source.sendMessage(Component.text("Failed! Database error."));
            return;
        }
    }

    @Override
    public String name() {
        return "ban";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.ban";
    }

    @Override
    public String execute(Object... args) throws Exception {
        if (args.length != 5)
            return "Failed! Required 5 arguments: <username> <uuid> <ip-address> <timestamp-expires> <reason>";
        String username = ((String) args[0]).trim();
        String uuid = ((String) args[1]).trim();
        String ipAddress = ((String) args[2]).trim();
        long timestampExpires = args[3] instanceof String ?
                Long.parseLong(((String) args[3])) : (long) args[3];
        String reason = ((String) args[4]).trim();
        if (BannedUser.isBanned(ipAddress, uuid))
            return "Failed! Already banned player.";
        try {
            BannedUser.add(BannedUser.create(username, ipAddress, timestampExpires, uuid, reason));
            for (Player p : VelocityAuth.INSTANCE.proxy.getAllPlayers()) {
                if (Objects.equals(p.getUniqueId().toString(), uuid)) {
                    p.disconnect(getBanText(timestampExpires, reason));
                    break;
                }
            }
            VelocityAuth.INSTANCE.logger.info("Banned '" + username + "/" + uuid + "' until " +
                    Instant.ofEpochMilli(timestampExpires).toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Database details could not updated.";
        }
        return null;
    }

    public Component getBanText(long timestampExpires, String reason) {
        return Component.text("You have been banned for " + new UtilsTime().getFormattedString(timestampExpires - System.currentTimeMillis())
                + ". Reason: " + reason, TextColor.color(255, 0, 0));
    }

}
