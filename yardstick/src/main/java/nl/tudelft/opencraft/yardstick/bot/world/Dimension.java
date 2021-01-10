/*
 * Yardstick: A Benchmark for Minecraft-like Services
 * Copyright (C) 2020 AtLarge Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package nl.tudelft.opencraft.yardstick.bot.world;

public enum Dimension {

    NETHER("minecraft:the_nether"),
    OVERWORLD("minecraft:overworld"),
    END("minecraft:the_end");

    private final String tag;

    private Dimension(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static Dimension forTag(String id) {
        for (Dimension d : Dimension.values()) {
            if (d.getTag().equals(id)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Invalid dimension: " + id);
    }

}
