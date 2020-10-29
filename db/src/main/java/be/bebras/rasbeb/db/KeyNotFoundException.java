/* KeyNotFoundException.java
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

package be.bebras.rasbeb.db;

/**
 * This exception is thrown when a database search for a specific key or id does not
 * returns no result although a result was expected.
 */
public class KeyNotFoundException extends DataAccessException {

    private static final long serialVersionUID = -7191061988719481276L;

    private String key;

    public String getKey() {
        return key;
    }

    public KeyNotFoundException(String message, String key) {
        super(message + "(for key: " + key + ")", null);
        this.key = key;

    }

    public KeyNotFoundException(String message, int key) {
        this(message, Integer.toString(key));
    }
}
