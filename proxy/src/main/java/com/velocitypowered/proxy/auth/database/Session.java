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
 * that contains static methods for fetching/updating data from the "Session" table.
 * A single object/instance of this class represents a single row in the table
 * and data can be accessed via its public fields. <p>
 * Its not recommended to modify this class but it should be OK to add new methods to it.
 * If modifications are really needed create a pull request directly to jSQL-Gen instead.
 */
public class Session {
    private static final java.sql.Connection con;
    private static final java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);

    static {
        try {
            con = java.sql.DriverManager.getConnection(Database.url, Database.username, Database.password);
            try (Statement s = con.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS `Session` (`id` INT NOT NULL PRIMARY KEY)");
                try {
                    s.executeUpdate("ALTER TABLE `Session` ADD COLUMN `userId` INT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN `userId` INT NOT NULL");
                try {
                    s.executeUpdate("ALTER TABLE `Session` ADD COLUMN `ipAddress` TEXT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN `ipAddress` TEXT NOT NULL");
                try {
                    s.executeUpdate("ALTER TABLE `Session` ADD COLUMN `timestampExpires` BIGINT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN `timestampExpires` BIGINT NOT NULL");
                try {
                    s.executeUpdate("ALTER TABLE `Session` ADD COLUMN `isActive` TINYINT");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN `isActive` TINYINT");
                try {
                    s.executeUpdate("ALTER TABLE `Session` ADD COLUMN `username` TEXT NOT NULL");
                } catch (Exception ignored) {
                }
                s.executeUpdate("ALTER TABLE `Session` MODIFY COLUMN `username` TEXT NOT NULL");
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM `Session` ORDER BY id DESC LIMIT 1")) {
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
    public int userId;
    /**
     * Database field/value. Not null. <br>
     */
    public String ipAddress;
    /**
     * Database field/value. Not null. <br>
     */
    public long timestampExpires;
    /**
     * Database field/value. <br>
     */
    public byte isActive;
    /**
     * Database field/value. Not null. <br>
     */
    public String username;
    private Session() {
    }
    /**
     * Use the static create method instead of this constructor,
     * if you plan to add this object to the database in the future, since
     * that method fetches and sets/reserves the {@link #id}.
     */
    public Session(int id, int userId, String ipAddress, long timestampExpires, String username) {
        this.id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.timestampExpires = timestampExpires;
        this.username = username;
    }
    /**
     * Use the static create method instead of this constructor,
     * if you plan to add this object to the database in the future, since
     * that method fetches and sets/reserves the {@link #id}.
     */
    public Session(int id, int userId, String ipAddress, long timestampExpires, byte isActive, String username) {
        this.id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.timestampExpires = timestampExpires;
        this.isActive = isActive;
        this.username = username;
    }

    /**
     * Creates and returns an object that can be added to this table.
     * Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).
     * Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
     * Also note that this method will NOT add the object to the table.
     */
    public static Session create(int userId, String ipAddress, long timestampExpires, String username) {
        int id = idCounter.getAndIncrement();
        Session obj = new Session(id, userId, ipAddress, timestampExpires, username);
        return obj;
    }

    /**
     * Creates and returns an object that can be added to this table.
     * Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).
     * Note that this method will NOT add the object to the table.
     */
    public static Session create(int userId, String ipAddress, long timestampExpires, byte isActive, String username) {
        int id = idCounter.getAndIncrement();
        Session obj = new Session();
        obj.id = id;
        obj.userId = userId;
        obj.ipAddress = ipAddress;
        obj.timestampExpires = timestampExpires;
        obj.isActive = isActive;
        obj.username = username;
        return obj;
    }

    /**
     * Convenience method for creating and directly adding a new object to the table.
     * Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
     */
    public static Session createAndAdd(int userId, String ipAddress, long timestampExpires, String username) {
        int id = idCounter.getAndIncrement();
        Session obj = new Session(id, userId, ipAddress, timestampExpires, username);
        add(obj);
        return obj;
    }

    /**
     * Convenience method for creating and directly adding a new object to the table.
     */
    public static Session createAndAdd(int userId, String ipAddress, long timestampExpires, byte isActive, String username) {
        int id = idCounter.getAndIncrement();
        Session obj = new Session();
        obj.id = id;
        obj.userId = userId;
        obj.ipAddress = ipAddress;
        obj.timestampExpires = timestampExpires;
        obj.isActive = isActive;
        obj.username = username;
        add(obj);
        return obj;
    }

    /**
     * @return a list containing all objects in this table.
     */
    public static List<Session> get() {
        return get(null);
    }

    /**
     * @return object with the provided id or null if there is no object with the provided id in this table.
     * @throws Exception on SQL issues.
     */
    public static Session get(int id) {
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
    public static List<Session> get(String where, Object... whereValues) {
        List<Session> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT `id`,`userId`,`ipAddress`,`timestampExpires`,`isActive`,`username`" +
                        " FROM `Session`" +
                        (where != null ? ("WHERE " + where) : ""))) {
            if (where != null && whereValues != null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i + 1, val);
                }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Session obj = new Session();
                list.add(obj);
                obj.id = rs.getInt(1);
                obj.userId = rs.getInt(2);
                obj.ipAddress = rs.getString(3);
                obj.timestampExpires = rs.getLong(4);
                obj.isActive = rs.getByte(5);
                obj.username = rs.getString(6);
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
    public static void update(Session obj) {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE `Session` SET `id`=?,`userId`=?,`ipAddress`=?,`timestampExpires`=?,`isActive`=?,`username`=? WHERE id=" + obj.id)) {
            ps.setInt(1, obj.id);
            ps.setInt(2, obj.userId);
            ps.setString(3, obj.ipAddress);
            ps.setLong(4, obj.timestampExpires);
            ps.setByte(5, obj.isActive);
            ps.setString(6, obj.username);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds the provided object to the database (note that the id is not checked for duplicates).
     */
    public static void add(Session obj) {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO `Session` (`id`,`userId`,`ipAddress`,`timestampExpires`,`isActive`,`username`) VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, obj.id);
            ps.setInt(2, obj.userId);
            ps.setString(3, obj.ipAddress);
            ps.setLong(4, obj.timestampExpires);
            ps.setByte(5, obj.isActive);
            ps.setString(6, obj.username);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the provided object from the database.
     */
    public static void remove(Session obj) {
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
                "DELETE FROM `Session` WHERE " + where)) {
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

    public static WHERE whereUserId() {
        return new WHERE("`userId`");
    }

    public static WHERE whereIpAddress() {
        return new WHERE("`ipAddress`");
    }

    public static WHERE whereTimestampExpires() {
        return new WHERE("`timestampExpires`");
    }

    public static WHERE whereIsActive() {
        return new WHERE("`isActive`");
    }

    public static WHERE whereUsername() {
        return new WHERE("`username`");
    }

    public Session clone() {
        return new Session(this.id, this.userId, this.ipAddress, this.timestampExpires, this.isActive, this.username);
    }

    public String toPrintString() {
        return "" + "id=" + this.id + " " + "userId=" + this.userId + " " + "ipAddress=" + this.ipAddress + " " + "timestampExpires=" + this.timestampExpires + " " + "isActive=" + this.isActive + " " + "username=" + this.username + " ";
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
        public List<Session> get() {
            String orderBy = orderByBuilder.toString();
            if (!orderBy.isEmpty()) orderBy = " ORDER BY " + orderBy.substring(0, orderBy.length() - 2) + " ";
            if (!whereObjects.isEmpty())
                return Session.get(sqlBuilder.toString() + orderBy + limitBuilder.toString(), whereObjects.toArray());
            else
                return Session.get(sqlBuilder.toString() + orderBy + limitBuilder.toString(), (Object[]) null);
        }

        /**
         * Executes the generated SQL statement
         * and removes the objects matching the query.
         */
        public void remove() {
            String orderBy = orderByBuilder.toString();
            if (!orderBy.isEmpty()) orderBy = " ORDER BY " + orderBy.substring(0, orderBy.length() - 2) + " ";
            if (!whereObjects.isEmpty())
                Session.remove(sqlBuilder.toString() + orderBy + limitBuilder.toString(), whereObjects.toArray());
            else
                Session.remove(sqlBuilder.toString() + orderBy + limitBuilder.toString(), (Object[]) null);
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
