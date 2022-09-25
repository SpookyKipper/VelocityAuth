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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Generated class by <a href="https://github.com/Osiris-Team/jSQL-Gen">jSQL-Gen</a>
 * that contains static methods for fetching/updating data from the "RegisteredUser" table.
 * A single object/instance of this class represents a single row in the table
 * and data can be accessed via its public fields. <p>
 * Its not recommended to modify this class but it should be OK to add new methods to it.
 * If modifications are really needed create a pull request directly to jSQL-Gen instead.
 */
public class RegisteredUser {
    private static final java.sql.Connection con;
    private static final java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);

    static {
        try {
            con = java.sql.DriverManager.getConnection(Database.url, Database.username, Database.password);
            try (Statement s = con.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS `RegisteredUser` (`id` INT NOT NULL PRIMARY KEY)");
                try {
                    s.executeUpdate("ALTER TABLE `RegisteredUser` ADD COLUMN `username` TEXT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `RegisteredUser` MODIFY COLUMN `username` TEXT NOT NULL");
                try {
                    s.executeUpdate("ALTER TABLE `RegisteredUser` ADD COLUMN `password` TEXT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `RegisteredUser` MODIFY COLUMN `password` TEXT NOT NULL");
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM `RegisteredUser` ORDER BY id DESC LIMIT 1")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) idCounter.set(rs.getInt(1) + 1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Database field/value. Not null. <br>
     */
    public int id;
    /**
     * Database field/value. Not null. <br>
     */
    public String username;
    /**
     * Database field/value. Not null. <br>
     */
    public String password;
    private RegisteredUser() {
    }
    /**
     * Use the static create method instead of this constructor,
     * if you plan to add this object to the database in the future, since
     * that method fetches and sets/reserves the {@link #id}.
     */
    public RegisteredUser(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    /**
     * Creates and returns an object that can be added to this table.
     * Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).
     * Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
     * Also note that this method will NOT add the object to the table.
     */
    public static RegisteredUser create(String username, String password) {
        int id = idCounter.getAndIncrement();
        RegisteredUser obj = new RegisteredUser(id, username, password);
        return obj;
    }

    /**
     * Convenience method for creating and directly adding a new object to the table.
     * Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
     */
    public static RegisteredUser createAndAdd(String username, String password) {
        int id = idCounter.getAndIncrement();
        RegisteredUser obj = new RegisteredUser(id, username, password);
        add(obj);
        return obj;
    }

    /**
     * @return a list containing all objects in this table.
     */
    public static List<RegisteredUser> get() {
        return get(null);
    }

    /**
     * @return object with the provided id or null if there is no object with the provided id in this table.
     * @throws Exception on SQL issues.
     */
    public static RegisteredUser get(int id) {
        try {
            return get("id = " + id).get(0);
        } catch (IndexOutOfBoundsException ignored) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Example: <br>
     * get("username=? AND age=?", "Peter", 33);  <br>
     *
     * @param where       can be null. Your SQL WHERE statement (without the leading WHERE).
     * @param whereValues can be null. Your SQL WHERE statement values to set for '?'.
     * @return a list containing only objects that match the provided SQL WHERE statement (no matches = empty list).
     * if that statement is null, returns all the contents of this table.
     */
    public static List<RegisteredUser> get(String where, Object... whereValues) {
        List<RegisteredUser> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT `id`,`username`,`password`" +
                        " FROM `RegisteredUser`" +
                        (where != null ? ("WHERE " + where) : ""))) {
            if (where != null && whereValues != null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i + 1, val);
                }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RegisteredUser obj = new RegisteredUser();
                list.add(obj);
                obj.id = rs.getInt(1);
                obj.username = rs.getString(2);
                obj.password = rs.getString(3);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * Searches the provided object in the database (by its id),
     * and updates all its fields.
     *
     * @throws Exception when failed to find by id or other SQL issues.
     */
    public static void update(RegisteredUser obj) {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE `RegisteredUser` SET `id`=?,`username`=?,`password`=? WHERE id=" + obj.id)) {
            ps.setInt(1, obj.id);
            ps.setString(2, obj.username);
            ps.setString(3, obj.password);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds the provided object to the database (note that the id is not checked for duplicates).
     */
    public static void add(RegisteredUser obj) {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO `RegisteredUser` (`id`,`username`,`password`) VALUES (?,?,?)")) {
            ps.setInt(1, obj.id);
            ps.setString(2, obj.username);
            ps.setString(3, obj.password);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the provided object from the database.
     */
    public static void remove(RegisteredUser obj) {
        remove("id = " + obj.id);
    }

    /**
     * Example: <br>
     * remove("username=?", "Peter"); <br>
     * Deletes the objects that are found by the provided SQL WHERE statement, from the database.
     *
     * @param whereValues can be null. Your SQL WHERE statement values to set for '?'.
     */
    public static void remove(String where, Object... whereValues) {
        java.util.Objects.requireNonNull(where);
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM `RegisteredUser` WHERE " + where)) {
            if (whereValues != null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i + 1, val);
                }
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static WHERE whereId() {
        return new WHERE("`id`");
    }

    public static WHERE whereUsername() {
        return new WHERE("`username`");
    }

    public static WHERE wherePassword() {
        return new WHERE("`password`");
    }

    public RegisteredUser clone() {
        return new RegisteredUser(this.id, this.username, this.password);
    }

    public String toPrintString() {
        return "" + "id=" + this.id + " " + "username=" + this.username + " " + "password=" + this.password + " ";
    }

    public static class WHERE {
        private final String columnName;
        /**
         * Remember to prepend WHERE on the final SQL statement.
         * This is not done by this class due to performance reasons. <p>
         * <p>
         * Note that it excepts the generated SQL string to be used by a {@link java.sql.PreparedStatement}
         * to protect against SQL-Injection. <p>
         * <p>
         * Also note that the SQL query gets optimized by the database automatically,
         * thus It's recommended to make queries as readable as possible and
         * not worry that much about performance.
         */
        public StringBuilder sqlBuilder = new StringBuilder();
        public StringBuilder orderByBuilder = new StringBuilder();
        public StringBuilder limitBuilder = new StringBuilder();
        List<Object> whereObjects = new ArrayList<>();

        public WHERE(String columnName) {
            this.columnName = columnName;
        }

        /**
         * Executes the generated SQL statement
         * and returns a list of objects matching the query.
         */
        public List<RegisteredUser> get() {
            String orderBy = orderByBuilder.toString();
            if (!orderBy.isEmpty()) orderBy = " ORDER BY " + orderBy.substring(0, orderBy.length() - 2) + " ";
            if (!whereObjects.isEmpty())
                return RegisteredUser.get(sqlBuilder.toString() + orderBy + limitBuilder.toString(), whereObjects.toArray());
            else
                return RegisteredUser.get(sqlBuilder.toString() + orderBy + limitBuilder.toString(), (Object[]) null);
        }

        /**
         * Executes the generated SQL statement
         * and removes the objects matching the query.
         */
        public void remove() {
            String orderBy = orderByBuilder.toString();
            if (!orderBy.isEmpty()) orderBy = " ORDER BY " + orderBy.substring(0, orderBy.length() - 2) + " ";
            if (!whereObjects.isEmpty())
                RegisteredUser.remove(sqlBuilder.toString() + orderBy + limitBuilder.toString(), whereObjects.toArray());
            else
                RegisteredUser.remove(sqlBuilder.toString() + orderBy + limitBuilder.toString(), (Object[]) null);
        }

        /**
         * AND (...) <br>
         */
        public WHERE and(WHERE where) {
            String sql = where.sqlBuilder.toString();
            if (!sql.isEmpty()) {
                sqlBuilder.append("AND (").append(sql).append(") ");
                whereObjects.addAll(where.whereObjects);
            }
            orderByBuilder.append(where.orderByBuilder.toString());
            return this;
        }

        /**
         * OR (...) <br>
         */
        public WHERE or(WHERE where) {
            String sql = where.sqlBuilder.toString();
            if (!sql.isEmpty()) {
                sqlBuilder.append("OR (").append(sql).append(") ");
                whereObjects.addAll(where.whereObjects);
            }
            orderByBuilder.append(where.orderByBuilder.toString());
            return this;
        }

        /**
         * columnName = ? <br>
         */
        public WHERE is(Object obj) {
            sqlBuilder.append(columnName).append(" = ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName IN (?,?,...) <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_in.asp">https://www.w3schools.com/mysql/mysql_in.asp</a>
         */
        public WHERE is(Object... objects) {
            String s = "";
            for (Object obj : objects) {
                s += "?,";
                whereObjects.add(obj);
            }
            s = s.substring(0, s.length() - 1); // Remove last ,
            sqlBuilder.append(columnName).append(" IN (" + s + ") ");
            return this;
        }

        /**
         * columnName <> ? <br>
         */
        public WHERE isNot(Object obj) {
            sqlBuilder.append(columnName).append(" <> ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName IS NULL <br>
         */
        public WHERE isNull() {
            sqlBuilder.append(columnName).append(" IS NULL ");
            return this;
        }

        /**
         * columnName IS NOT NULL <br>
         */
        public WHERE isNotNull() {
            sqlBuilder.append(columnName).append(" IS NOT NULL ");
            return this;
        }

        /**
         * columnName LIKE ? <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE like(Object obj) {
            sqlBuilder.append(columnName).append(" LIKE ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName NOT LIKE ? <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE notLike(Object obj) {
            sqlBuilder.append(columnName).append(" NOT LIKE ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName > ? <br>
         */
        public WHERE biggerThan(Object obj) {
            sqlBuilder.append(columnName).append(" > ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName < ? <br>
         */
        public WHERE smallerThan(Object obj) {
            sqlBuilder.append(columnName).append(" < ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName >= ? <br>
         */
        public WHERE biggerOrEqual(Object obj) {
            sqlBuilder.append(columnName).append(" >= ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName <= ? <br>
         */
        public WHERE smallerOrEqual(Object obj) {
            sqlBuilder.append(columnName).append(" <= ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName BETWEEN ? AND ? <br>
         */
        public WHERE between(Object obj1, Object obj2) {
            sqlBuilder.append(columnName).append(" BETWEEN ? AND ? ");
            whereObjects.add(obj1);
            whereObjects.add(obj2);
            return this;
        }

        /**
         * columnName ASC, <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE smallestFirst() {
            orderByBuilder.append(columnName + " ASC, ");
            return this;
        }

        /**
         * columnName DESC, <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE biggestFirst() {
            orderByBuilder.append(columnName + " DESC, ");
            return this;
        }

        /**
         * LIMIT number <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_limit.asp">https://www.w3schools.com/mysql/mysql_limit.asp</a>
         */
        public WHERE limit(int num) {
            limitBuilder.append("LIMIT ").append(num + " ");
            return this;
        }

    }
}
