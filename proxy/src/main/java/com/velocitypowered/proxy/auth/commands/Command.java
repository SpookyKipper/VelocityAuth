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


import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.proxy.auth.VelocityAuth;

import java.util.function.Consumer;

public interface Command extends SimpleCommand {

    String name();

    String[] aliases();

    String permission();

    /**
     * This is the function that gets executed by the velocity API. <br>
     * If you would like to separate your actual command logic from a specific
     * API (in this case the velocity API) override the {@link #execute(Object...)} function,
     * and call it inside this function.
     *
     * @param invocation the invocation context
     */
    @Override
    void execute(Invocation invocation);

    /**
     * Code to execute on command execution. <br>
     * Not bound to velocity API and thus usable everywhere. <br>
     *
     * @param args arguments.
     * @return error message, null if no error.
     * @throws Exception if something went really wrong.
     */
    default String execute(Object... args) throws Exception {
        return null;
    }

    default void register() {
        CommandManager commandManager = VelocityAuth.INSTANCE.proxy.getCommandManager();
        commandManager.register(meta(), this);
    }

    @Override
    default boolean hasPermission(Invocation invocation) {
        return invocation.source() // Get the person executing this command (player or console for example)
                .hasPermission(permission());
    }

    default boolean hasPermission(CommandSource source) {
        return source // The person executing this command (player or console for example)
                .hasPermission(permission()); // Use that persons' permission function for hasPermission check
    }

    default CommandMeta meta() {
        CommandManager commandManager = VelocityAuth.INSTANCE.proxy.getCommandManager();
        return meta(commandManager);
    }

    default CommandMeta meta(CommandManager commandManager) {
        return commandManager.metaBuilder(name()).aliases(aliases()).build();
    }

    class Builder {
        public String name;
        public String[] aliases;
        public String permission;
        public Consumer<Invocation> execute;

        public Builder(String name, String[] aliases, String permission, Consumer<Invocation> execute) {
            this.name = name;
            this.aliases = aliases;
            this.permission = permission;
            this.execute = execute;
        }

        public Builder(String name, String permission, Consumer<Invocation> execute, String... aliases) {
            this.name = name;
            this.aliases = aliases;
            this.permission = permission;
            this.execute = execute;
        }

        public Command build() {
            return new Command() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public String[] aliases() {
                    return aliases;
                }

                @Override
                public String permission() {
                    return permission;
                }

                @Override
                public void execute(Invocation invocation) {
                    execute.accept(invocation);
                }
            };
        }
    }
}
