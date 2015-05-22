/* ActivationDAO.java
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

package be.bebras.rasbeb.db.dao;

import be.bebras.rasbeb.db.data.Activation;
import be.bebras.rasbeb.db.data.Role;

/**
 * Provides basic operations related to user registration and password resets.
 */
public interface ActivationDAO {

    /**
     * Create and return new activation token for the given email address and role
     * If a token for that email address already existed, it is removed.
     * @param longLived true when the token is created by an organizer, false when created by a user request. Determines
     *                  how long the token will be valid.
     * @param registration  whether this is a registration request or a request to change a password
     */
    public String createToken (String email, Role role, boolean longLived, boolean registration);

    /**
     * List all pending registrations
     */
    public Iterable<Activation> listPendingRegistrations();

    /**
     * Remove the token for the given email address if it exists.
     */
    public void deleteToken (String email);

    /**
     * Checks whether the given token was stored for the given email address.
     * @return the corresponding role or null if the combination does not exist
     * or is already expired
     */
    public Role checkToken (String email, String token);


}
