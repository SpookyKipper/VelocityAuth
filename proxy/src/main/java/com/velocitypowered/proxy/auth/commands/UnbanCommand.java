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
import com.velocitypowered.proxy.auth.VelocityAuth;
import com.velocitypowered.proxy.auth.database.BannedUser;
import com.velocitypowered.proxy.auth.utils.Arr;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

public final class UnbanCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        Arr<String> args = new Arr<>(invocation.arguments());
        if (args.length != 1) {
            source.sendMessage(Component.text("Failed! Requires 1 argument: <username> "));
            return;
        }
        String username = args.get(0);
        try {
            String error = execute(username);
            if (error == null) {
                source.sendMessage(Component.text("Unban success!"));
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
        return "unban";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.unban";
    }

    @Override
    public String execute(Object... args) throws Exception {
        if (args.length != 1)
            return "Failed! Required 1 argument: <username>";
        String username = ((String) args[0]).trim();
        try {
            List<BannedUser> bannedUsers = BannedUser.getBannedUsernames(username);
            if (bannedUsers.isEmpty()) return "Failed! No banned players by name '" + username + "' found!";
            for (BannedUser bannedUser : bannedUsers) {
                bannedUser.timestampExpires = System.currentTimeMillis();
                BannedUser.update(bannedUser);
            }
            VelocityAuth.INSTANCE.logger.info("Unbanned '" + username + "' now.");
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Database details could not updated.";
        }
        return null;
    }

}
