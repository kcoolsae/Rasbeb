/* UserDAOTest.java
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

package be.bebras.rasbeb.db.jdbc;

import be.bebras.rasbeb.db.KeyNotFoundException;
import be.bebras.rasbeb.db.dao.ActivationDAO;
import be.bebras.rasbeb.db.dao.UserDAO;
import be.bebras.rasbeb.db.data.Role;
import be.bebras.rasbeb.db.data.Status;
import be.bebras.rasbeb.db.data.User;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for the JDBC implementation of {@link UserDAO}
 */
public class UserDAOTest extends DAOTest {

    private UserDAO dao;

    private int userId;

    @Before
    public void getDAOAndInitFixtures () {
        dao = context.getUserDAO();

        userId = dao.createUser("ref.user@Email.com", Role.ORGANIZER, "Smith, John");
        dao.setPassword(userId, "secret");
    }

    @Test
    public void existingUser () {
        User user = dao.getUser(userId);
        assertEquals (userId, user.getId());
        assertEquals (Status.DEFAULT, user.getStatus());
        assertEquals("Smith, John", user.getName());
        assertEquals("ref.user@email.com", user.getEmail()); // to lower case!

        assertEquals (Role.ORGANIZER, user.getRole());

        String bebrasId = user.getBebrasId();
        assertEquals (userId, Integer.parseInt(bebrasId) / 200 - 5000);
    }

    @Test(expected= KeyNotFoundException.class)
    public void nonexistentUser () {
        dao.getUser (-1);
    }

    @Test
    public void correctPassword1 () {
        User user = dao.getUser("  Ref.User@email.Com  ", "secret") ;  // note spaces in name!
        assertNotNull(user);
        assertEquals (userId, user.getId());
    }

    @Test
    public void correctPassword2 () {
        User user = dao.getUser(userId) ;
        String bebrasId = user.getBebrasId();

        user =  dao.getUser(bebrasId, "secret") ;
        assertNotNull(user);
        assertEquals (userId, user.getId());
    }

    @Test
    public void incorrectPassword () {
        User user = dao.getUser("Ref.User@email.Com", "SECRET") ;
        assertNull(user);
    }

    @Test
    public void incorrectEmail () {
        User user = dao.getUser("Ref.@email.Com", "secret") ;
        assertNull(user);
    }

    @Test
    public void testHasAdministrator () {
        assertFalse (dao.hasAdministrator());
        dao.createUser("Some-email@gmail.com", Role.ADMIN, "Istrator, Admin");
        assertTrue (dao.hasAdministrator());
    }

    @Test
    public void testIsPassword () {
        context.setUserId(userId);
        assertTrue (dao.isPassword("secret"));
    }

    @Test
    public void findUserByEmail () {
        User user = dao.findUserByEmail("Ref.User@email.Com") ;
        assertNotNull(user);
        assertEquals (userId, user.getId());
    }

    @Test
    public void findUserByBebrasId () {
	String bebrasId = dao.getUser(userId).getBebrasId();
        User user = dao.findUserByBebrasId(bebrasId) ;
        assertNotNull(user);
        assertEquals (userId, user.getId());
    }

    @Test
    public void fetchNonexistentUser () {
        assertNull (dao.findUserByEmail("UNknown"));
    }

    @Test
    public void activationRemovedAfterLogin1 () {
        ActivationDAO adao = context.getActivationDAO();
        String token = adao.createToken("ref.user@Email.com", Role.ORGANIZER, false, false);
        dao.getUser("REF.User@email.Com", "secret") ;
        assertNull (adao.checkToken("ref.user@Email.com", token));
    }

    @Test
    public void activationRemovedAfterLogin2 () {
        User user = dao.getUser(userId) ;
        ActivationDAO adao = context.getActivationDAO();
        String token = adao.createToken("ref.user@Email.com", user.getRole(), false, false);

        String bebrasId = user.getBebrasId();
        dao.getUser(bebrasId,  "secret") ;

        assertNull (adao.checkToken("ref.user@Email.com", token));

    }

    @Test
    public void activationRemovedAfterCreateUser () {
        String email = "some.other@email.email";

        ActivationDAO adao = context.getActivationDAO();
        String token = adao.createToken(email, Role.STUDENT_TO_BE, false, false);
        dao.createUser(email, Role.STUDENT_TO_BE, "Janssen, Piet");
        assertNull (adao.checkToken(email,token));

    }


}
