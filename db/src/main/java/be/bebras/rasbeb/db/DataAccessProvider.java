/* DataAccessProvider.java
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

import be.bebras.rasbeb.db.data.Role;

/**
 * Provides data access contexts through its method {@link #getDataAccessContext}.
 */
public interface DataAccessProvider extends AutoCloseable {

    /**
     * Create a new data access context for the given user and language
     *
     * @param userId ID of the user that will use this context. Use 0 for an anonymous user
     * @param lang   Two-character language code for this context.
     * @param role Role for this context (determines data access privileges)
     */
    public DataAccessContext getDataAccessContext(int userId, String lang, Role role);

    /**
     * Release all resources related to this data access context.
     */
    public void close();

}
