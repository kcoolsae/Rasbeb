/* Filter.java
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
 * Represents a filter on a combination of fields of type string. The fields are identified by an
 * enumerated type. (Examples can be found in various DAOs.)
 *
 * Filters for a given enumerated type can be obtained from the corresponding DAOs.
 */
public interface Filter<F extends Enum<F>> {

    /**
     * Refine the filter to indicate that the given field
     * must contain the given string (trimmed and case insensitive). Should be called only once
     * for a given field.
     * <p>No action if string is null or empty (after trimming)
     */
    public void fieldContains (F field, String string);

    /**
     * Refine the filter to indicate that the given field
     * must start with the given string (trimmed and case insensitive). Should be called only once
     * for a given field.
     * <p>No action if string is null or empty (after trimming)
     */
    public void fieldStartsWith (F field, String string);

    /**
     * Refine the filter to indicate that the given field
     * must end with the given string (trimmed and case insensitive). Should be called only once
     * for a given field.
     * <p>No action if string is null or empty (after trimming)
     */
    public void fieldEndsWith (F field, String string);

}
