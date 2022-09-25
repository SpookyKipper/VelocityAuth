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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Objects;

public class RegisterCommand implements Command {
    @Override
    public String name() {
        return "register";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.register";
    }

    @Override
    public String execute(Object... args) throws Exception {
        return null;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 2) {
            source.sendMessage(Component.text("Failed! Requires 2 arguments: <password> <confirm-password>",
                    TextColor.color(255, 0, 0)));
            return;
        }
        String password = args[0];
        String confirmPassword = args[1];
        if (!Objects.equals(password, confirmPassword)) {
            source.sendMessage(Component.text("Failed! <password> does not match <confirm-password>",
                    TextColor.color(255, 0, 0)));
            return;
        }
        if (source instanceof Player) {
            Player player = (Player) source;
            try {
                String error = new AdminRegisterCommand().execute(player.getUsername(), password);
                if (error == null) {
                    source.sendMessage(Component.text("Registration success!"));
                } else {
                    source.sendMessage(Component.text(error, TextColor.color(255, 0, 0)));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! " + e.getMessage(), TextColor.color(255, 0, 0)));
                return;
            }
        } else
            VelocityAuth.INSTANCE.logger.error("Failed! Must be player to execute this command.");
    }
}
