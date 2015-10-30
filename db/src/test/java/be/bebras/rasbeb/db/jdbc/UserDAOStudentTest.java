/* UserDAOStudentTest.java
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
import be.bebras.rasbeb.db.data.Student;
import be.bebras.rasbeb.db.data.User;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Test for the student related methods of {@link UserDAO}
 */
public class UserDAOStudentTest extends DAOTest {

    private int[] studentIds;

    private UserDAO dao;


    @Before
    public void getDAOAndInitFixtures() {
        dao = context.getUserDAO();
        studentIds = Fixtures.createStudents(context);
    }

    @Test
    public void studentAsUser() {
        User user = dao.getUser(studentIds[0]);
        assertEquals(studentIds[0], user.getId());
        assertEquals(Status.DEFAULT, user.getStatus());
        assertEquals("Doe, John", user.getName());
        assertEquals("student1@gmail.com", user.getEmail());
        assertEquals(Role.STUDENT, user.getRole());
    }

    @Test
    public void existingStudent() {
        Student student = dao.getStudent(studentIds[1]);
        assertEquals(studentIds[1], student.getId());
        assertEquals(Status.DEFAULT, student.getStatus());
        assertEquals("Doe, Jane", student.getName());
        assertEquals("student2@gmail.com", student.getEmail());
        assertEquals(Role.STUDENT, student.getRole());

        assertEquals(false, student.isMale());
        assertEquals("initial2", student.getInitialPassword());
    }

    @Test
    public void canLoginWithInitialPassword() {

        User user = dao.getUser("student1@gmail.com", "initial1");
        assertNotNull(user);
        assertEquals(studentIds[0], user.getId());
    }

    @Test
    public void canLoginWithBebrasId() {
        User user = dao.getUser(studentIds[2]);
        user = dao.getUser(user.getBebrasId(), "initial3");
        assertNotNull(user);
        assertEquals(studentIds[2], user.getId());
    }

    @Test
    public void foundByEmail() {
        Student student = dao.findStudent("student1@gmail.com", "random");
        assertNotNull(student);
        assertEquals(studentIds[0], student.getId());
        assertEquals("Doe, John", student.getName());
    }

    @Test
    public void foundByBebrasId() {
        String bebrasId = dao.getStudent(studentIds[1]).getBebrasId();
        Student student = dao.findStudent(null, bebrasId);
        assertNotNull(student);
        assertEquals(studentIds[1], student.getId());
        assertEquals("Doe, Jane", student.getName());
    }

    @Test
    public void resetPassword() {
        dao.resetInitialPassword(studentIds[0], "newpass");
        User user = dao.getUser("student1@gmail.com", "newpass");
        assertNotNull(user);
        assertEquals(studentIds[0], user.getId());
        Student student = dao.getStudent(studentIds[0]);
        assertEquals("newpass", student.getInitialPassword());
    }
}
