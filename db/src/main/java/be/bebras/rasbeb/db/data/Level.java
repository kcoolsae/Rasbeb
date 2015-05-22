/* Level.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright (C) 2015 Universiteit Gent
 * 
 * This file is part of the Rasbeb project, an interactive web
 * application for Bebras competitions.
 * 
 * Corresponding author:
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Rasbeb Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Rasbeb Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package be.bebras.rasbeb.db.data;

/**
 * Describes a level of difficulty for a competition. There are four levels (numbered 1-4) roughly corresponding
 * to the age groups 10-12, 12-14, 14-16, 16-18
 */
public class Level {

    private int level;

    private String lang;

    private String name;

    private String description;

    /**
     * Numeric value of this level, in the range 1-4
     */
    public int getLevel() {
        return level;
    }

    /**
     * Language code for this record (2 characters)
     */

    public String getLang() {
        return lang;
    }

    /**
     * Short name for this level
     */

    public String getName() {
        return name;
    }

    /**
     * Longer (internationalized) description for this level
     */

    public String getDescription() {
        return description;
    }

    public Level(int level, String lang, String name, String description) {
        this.level = level;
        this.lang = lang;
        this.name = name;
        this.description = description;
    }
}
