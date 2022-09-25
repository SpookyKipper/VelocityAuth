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
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.List;
import java.util.Random;

public final class AdminLoginCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 3) {
            source.sendMessage(Component.text("Failed! Requires 3 arguments: <username> <password> <ip-address>"));
            return;
        }
        String username = args[0];
        String password = args[1];
        String ipAddress = args[2];
        try {
            String error = execute(username, password, ipAddress);
            if (error == null) {
                source.sendMessage(Component.text("Login success!"));
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
        return "a_login";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.admin.login";
    }

    @Override
    public String execute(Object... args) throws Exception {
        if (args.length != 3) return "Failed! Required 3 arguments: <username> <password> <ip-address>";
        String username = ((String) args[0]).trim();
        String password = ((String) args[1]).trim();
        String ipAddress = ((String) args[2]).trim();
        if (VelocityAuth.INSTANCE.hasValidSession(username, ipAddress))
            return "Failed! Already logged in from another location (active session).";
        List<RegisteredUser> registeredUsers = RegisteredUser.get("username=?", username);
        if (registeredUsers.isEmpty())
            return "Failed! Could not find registered user named '" + username + "' in database.";
        if (registeredUsers.size() > 1)
            throw new Exception("There are multiple (" + registeredUsers.size() + ") registered players named '" + username
                    + "'! Its highly recommended to fix this issue.");
        RegisteredUser user = registeredUsers.get(0);
        if (!password.isEmpty() && !new Pbkdf2PasswordEncoder().matches(password, user.password))
            return "Failed! Invalid credentials!";
        // Login success
        try {
            Thread.sleep(new Random().nextInt(1000)); // Prevent password spoofing via timings
            long now = System.currentTimeMillis();
            List<Session> sessions = Session.get("username=? AND ipAddress=?", user.username, ipAddress);
            Session session = null;
            if (sessions.isEmpty()) {
                session = Session.create(user.id, ipAddress,
                        now + (VelocityAuth.INSTANCE.sessionMaxHours * 3600000L), (byte) 1, username);
                Session.add(session);
            } else {
                session = sessions.get(0);
                session.isActive = 1;
                Session.update(session);
            }
            VelocityAuth.INSTANCE.logger.info("Login success for '" + username + "', using session " + session.id);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Database details could not updated.";
        }
        return null;
    }

}
