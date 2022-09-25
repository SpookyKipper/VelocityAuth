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
import com.velocitypowered.proxy.auth.database.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ListSessionsCommand implements Command {
    @Override
    public String name() {
        return "list_sessions";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.list.sessions";
    }

    @Override
    public String execute(Object... args) throws Exception {
        return null;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        String username = null;
        if (args.length == 1) {
            username = args[0].trim();
            try {
                for (Session session : Session.get("username=?", username)) {
                    source.sendMessage(Component.text(session.toPrintString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! " + e.getMessage(), TextColor.color(255, 0, 0)));
            }
        } else {
            try {
                for (Session session : Session.get()) {
                    source.sendMessage(Component.text(session.toPrintString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! " + e.getMessage(), TextColor.color(255, 0, 0)));
            }
        }
    }
}
