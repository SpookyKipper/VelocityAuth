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
import com.velocitypowered.proxy.auth.database.RegisteredUser;
import com.velocitypowered.proxy.auth.database.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public final class AdminUnRegisterCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 1) {
            source.sendMessage(Component.text("Failed! Requires 1 argument: <username>"));
            return;
        }
        try {
            String error = execute(args[0]);
            if (error == null) {
                source.sendMessage(Component.text("Unregister success!"));
            } else {
                source.sendMessage(Component.text(error, TextColor.color(255, 0, 0)));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            source.sendMessage(Component.text("Failed! " + e.getMessage(), TextColor.color(255, 0, 0)));
        }
    }

    @Override
    public String name() {
        return "a_unregister";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.admin.unregister";
    }

    @Override
    public String execute(Object... args) throws Exception {
        if (args.length != 1) return "Failed! Required 1 arguments: <username>";
        String username = (String) args[0];
        if (!VelocityAuth.INSTANCE.isRegistered(username))
            return "Failed! No registered player named '" + username + "' found!";
        try {
            RegisteredUser user = RegisteredUser.get("username=?", username).get(0);
            RegisteredUser.remove(user);
            for (Session session : Session.get("username=?", username)) {
                Session.remove(session);
            }
            VelocityAuth.INSTANCE.logger.info("Unregister success for '" + username + "', removed id " + user.id + " and related sessions");
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Details could not be added to the database.";
        }
        return null;
    }
}
