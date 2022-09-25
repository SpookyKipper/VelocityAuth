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
import com.velocitypowered.proxy.auth.utils.Arr;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public final class AdminRegisterCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 2) {
            source.sendMessage(Component.text("Failed! Requires 2 arguments: <username> <password>"));
            return;
        }
        try {
            String error = execute(args[0], args[1]);
            if (error == null) {
                source.sendMessage(Component.text("Registration success!"));
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
        return "a_register";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.admin.register";
    }

    @Override
    public String execute(Object... args) throws Exception {
        if (args.length < 2) return "Failed! Required 2 arguments: <username> <password>";
        String username = ((String) args[0]).trim();
        String password = ((String) args[1]).trim();
        boolean isMojangAuthValid = new Arr<Object>(args).get(2) != null;
        if (VelocityAuth.INSTANCE.isRegistered(username))
            return "Failed! Already registered!";
        if (!isMojangAuthValid && password.length() < VelocityAuth.INSTANCE.minPasswordLength)
            return "Failed! Password too short. Minimum length is " + VelocityAuth.INSTANCE.minPasswordLength + ".";
        String encodedPassword = (isMojangAuthValid ? "" : new Pbkdf2PasswordEncoder().encode(password));
        try {
            RegisteredUser user = RegisteredUser.create(username, encodedPassword);
            RegisteredUser.add(user);
            VelocityAuth.INSTANCE.logger.info("Register success for '" + username + "', assigned id " + user.id);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Details could not be added to the database.";
        }
        return null;
    }
}
