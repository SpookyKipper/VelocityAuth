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

package ru.nanit.limbo.server.data;

public class BossBar {

    public String text;
    public float health;
    public Color color;
    public Division division;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public enum Color {

        PINK(0),
        BLUE(1),
        RED(2),
        GREEN(3),
        YELLOW(4),
        PURPLE(5),
        WHITE(6);

        private final int index;

        Color(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public enum Division {

        SOLID(0),
        DASHES_6(1),
        DASHES_10(2),
        DASHES_12(3),
        DASHES_20(4);

        private final int index;

        Division(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
