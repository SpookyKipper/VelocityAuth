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

package com.velocitypowered.proxy.auth.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class Database {
    public static String rawUrl = "jdbc:mysql://localhost/";
    public static String url = "jdbc:mysql://localhost/velocity";
    public static String name = "velocity";
    public static String username;
    public static String password;

    public static void create() {

        // Do the below to avoid "No suitable driver found..." exception
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        try {
            Class<?> driverClass = Class.forName(driverClassName);
            Objects.requireNonNull(driverClass);
        } catch (ClassNotFoundException e) {
            try {
                driverClassName = "com.mysql.jdbc.Driver"; // Try deprecated driver as fallback
                Class<?> driverClass = Class.forName(driverClassName);
                Objects.requireNonNull(driverClass);
            } catch (ClassNotFoundException ex) {
                System.err.println("Failed to find critical database driver class: " + driverClassName);
                ex.printStackTrace();
            }
        }

        // Create database if not exists
        try (Connection c = DriverManager.getConnection(Database.rawUrl, Database.username, Database.password);
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + Database.name + "`");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}






