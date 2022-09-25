/*
 * Copyright (C) 2020 Nan1t
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.nanit.limbo.world;

public class Location {

    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    Location(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    Location(double x, double y, double z) {
        this(x, y, z, 0.0F, 0.0F);
    }

    public static Location of(double x, double y, double z) {
        return new Location(x, y, z);
    }

    public static Location of(double x, double y, double z, float yaw, float pitch) {
        return new Location(x, y, z, yaw, pitch);
    }

    public static Location pos(int x, int y, int z) {
        return new Location(x, y, z);
    }

    public double getX() {
        return x;
    }

    public int getBlockX() {
        return (int) x;
    }

    public double getY() {
        return y;
    }

    public int getBlockY() {
        return (int) y;
    }

    public double getZ() {
        return z;
    }

    public int getBlockZ() {
        return (int) z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
