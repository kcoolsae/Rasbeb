/* UserDAO.java
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

import be.bebras.rasbeb.db.data.Role;
import be.bebras.rasbeb.db.data.Student;
import be.bebras.rasbeb.db.data.User;

/**
 * Provides basic operations on users, mostly password and login related
 */
public interface UserDAO {

    /**
     * Returns the user with the given id.
     */
    public User getUser(int id);

    /**
     * Create a new user. Automatically assigns a bebras id to that user. Password
     * should be set separately. If successful also removes any activation token
     * that was stored for this user.
     *
     * @throws be.bebras.rasbeb.db.UniqueViolationException when the email address is already
     *                                                      in the system
     */
    public int createUser(String email, Role role, String name);

    /**
     * Set a new password for the given user.
     */
    public void setPassword(int id, String password);

    /**
     * Set a new password for the current user.
     */
    public void setPassword(String password);

    /**
     * Check the password for the current user.
     */
    public boolean isPassword (String password);

    /**
     * Returns the user for the given email address. Note that this will also
     * return a user that has been deactivated.
     * @return user or null if no user exists with this email address
     */
    public User findUserByEmail(String email);

    /**
     * Returns the user with the given bebras id,  Note that this will also
     * return a user that has been deactivated.
     * @return user or null if no user exists with this email address
     */
    public User findUserByBebrasId(String bebraslId);

    /**
     * Returns the user for the given credentials.
     * @param login either an email address or the bebrasId of the user. (If the string
     *              contains a @@ it is considered an email address.)
     * @return user, or null if credentials were not valid
     */
    public User getUser(String login, String password);

    /**
     * Check whether there is at least one valid administrator account in the system.
     */
    public boolean hasAdministrator();

    /**
     * Returns a student user with the given email address, or if that is null, with the
     * give Bebras id. Returns null if such a user does not exist. Must not be called
     * with two null arguments.
     */
    public Student findStudent (String email, String bebrasId);

    /**
     * Create a student user.
     */
    public int createStudent (String email, String name, boolean male, String initialPassword);

    /**
     * Reset the password and initial password for an existing student user
     */
    public void resetInitialPassword (int studentId, String initialPassword);

    /**
     * Retrieve the data for a student user
     */
    public Student getStudent (int id);

}
