/* ActivationDAOTest.java
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

import be.bebras.rasbeb.db.dao.ActivationDAO;
import be.bebras.rasbeb.db.data.Activation;
import be.bebras.rasbeb.db.data.Role;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 *  Test for the JDBC implementation of {@link ActivationDAO}
 */
public class ActivationDAOTest extends DAOTest {

    private ActivationDAO dao;

    private String[] tokens;

    @Before
    public void getDAOAndFixtures () {
        dao = context.getActivationDAO();

        tokens = new String[] {
            dao.createToken("first@email.com", Role.STUDENT_TO_BE, false, false),
            dao.createToken("SECOND@Email.COM", Role.TEACHER_TO_BE, true, true)
        };
    }

    @Test
    public void existingToken () {
        assertEquals (Role.STUDENT_TO_BE, dao.checkToken("first@email.com", tokens[0]));
        assertEquals (Role.TEACHER_TO_BE, dao.checkToken("second@email.com",tokens[1]));
    }

    @Test
    public void listPending () {
        Iterator<Activation> iter = dao.listPendingRegistrations().iterator();
        assertTrue(iter.hasNext());
        Activation activation = iter.next();
        assertEquals ("second@email.com", activation.getEmail());
        assertEquals (Role.TEACHER_TO_BE, activation.getRole());
        assertEquals(true,activation.isRegistration());
    }

    @Test
    public void nonexistentToken () {
        assertNull (dao.checkToken ("first@email.com", "JUSTTESTING"));
    }

    @Test
    public void tokenRemoved () {
        dao.deleteToken("FIRST@Email.Com");
        assertNull (dao.checkToken("first@email.com", tokens[0]));
    }

    @Test
    public void renewedToken () {
        String token = dao.createToken("first@email.com", Role.ORGANIZER, true, true);
        assertEquals (Role.ORGANIZER, dao.checkToken("first@email.com", token));
        assertNull (dao.checkToken("first@email.com", tokens[0]));
    }

    // TODO: find a way to test token expiry
}
