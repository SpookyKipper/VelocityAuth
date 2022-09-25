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

import java.util.List;

public class ClearSessionsCommand implements Command {
    @Override
    public String name() {
        return "clear_sessions";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.clear.sessions";
    }

    @Override
    public String execute(Object... args) throws Exception {
        return null;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        int countSessions = 0, countSessionsRemove = 0;
        String username = null;
        if (args.length == 1) {
            username = args[0].trim();
            try {
                List<Session> list = Session.get("username=?", username);
                countSessions = list.size();
                for (Session session : list) {
                    Session.remove(session);
                    countSessionsRemove++;
                }
                source.sendMessage(Component.text("Removed " + countSessionsRemove + "/" + countSessions + " sessions! "));
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! Removed " + countSessionsRemove + "/" + countSessions + " sessions! " + e.getMessage(), TextColor.color(255, 0, 0)));
            }
        } else {
            try {
                List<Session> list = Session.get();
                countSessions = list.size();
                for (Session session : list) {
                    Session.remove(session);
                    countSessionsRemove++;
                }
                source.sendMessage(Component.text("Removed " + countSessionsRemove + "/" + countSessions + " sessions! "));
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! Removed " + countSessionsRemove + "/" + countSessions + " sessions! " + e.getMessage(), TextColor.color(255, 0, 0)));
            }
        }
    }
}
