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

package com.velocitypowered.proxy.auth.utils;

/**
 * Generic array wrapper class,
 * to provided additional useful functionality
 * for arrays.
 */
public class Arr<T> {
    public int length;
    Object[] content;

    public Arr(int size) {
        this.content = new Object[size];
        this.length = size;
    }

    public Arr(T[] content) {
        this.content = content;
        this.length = content.length;
    }

    public Arr<T> set(int i, T obj) {
        content[i] = obj;
        return this;
    }

    /**
     * Returns null instead of throwing {@link IndexOutOfBoundsException}.
     */
    public T get(int i) {
        try {
            return (T) content[i];
        } catch (Exception ignored) {
        }
        return null;
    }

    public String toPrintString() {
        return toPrintString(0, length - 1);
    }

    public String toPrintString(int startIndex, int endIndex) {
        return toPrintString(" ", startIndex, endIndex);
    }

    public String toPrintString(String separator, int startIndex, int endIndex) {
        StringBuilder s = new StringBuilder();
        for (int i = startIndex; i <= endIndex; i++) {
            s.append(get(i));
            s.append(separator);
        }
        return s.toString();
    }
}
