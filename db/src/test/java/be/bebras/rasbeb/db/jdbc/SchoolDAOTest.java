/* SchoolDAOTest.java
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

import be.bebras.rasbeb.db.Filter;
import be.bebras.rasbeb.db.KeyNotFoundException;
import be.bebras.rasbeb.db.dao.SchoolDAO;
import be.bebras.rasbeb.db.data.School;
import be.bebras.rasbeb.db.data.Status;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Test for the JDBC implementation of {@link SchoolDAO}
 */
public class SchoolDAOTest extends DAOTest {

    private SchoolDAO dao;

    private int[] schoolIds;

    @Before
    public void getDAOAndInitFixtures() {
        dao = context.getSchoolDAO();
        schoolIds = Fixtures.createSchools(context);
    }

    @Test
    public void existingSchool() {
        School school = dao.getSchool(schoolIds[2]);
        assertEquals("School of Arts", school.getName());
        assertEquals("Paris Avenue", school.getStreet());
        assertEquals("1200", school.getZip());
        assertEquals("Jerusalem", school.getTown());

        assertEquals(Status.DEFAULT, school.getStatus());
        assertEquals(schoolIds[2], school.getId());
    }

    @Test(expected = KeyNotFoundException.class)
    public void deletedSchoolNotFound() {
        dao.deleteSchool(schoolIds[3]);
        dao.getSchool(schoolIds[3]);
    }

    @Test
    public void schoolUpdated() {
        dao.updateSchool(schoolIds[1], "School of science", "Street of science", "0000", "Some town", 2015);
        School school = dao.getSchool(schoolIds[1]);
        assertEquals("School of science", school.getName());
        assertEquals("Street of science", school.getStreet());
        assertEquals("0000", school.getZip());
        assertEquals("Some town", school.getTown());
        assertEquals(2015, school.getYear());
    }

    @Test(expected = KeyNotFoundException.class)
    public void cannotUpdateNonexistentSchool() {
        // school -1 should not exist
        dao.updateSchool(-1, "School of science", "Street of science", "0000", "Some town", 2013);
    }

    @Test
    public void listCurrentSchoolsInOrder1() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        // no filters
        Iterator<School> iterator = dao.listSchoolsOfCurrentYear(filter, SchoolDAO.Field.NAME, true, 0, 10).iterator();

        assertEquals (schoolIds[3], iterator.next().getId());
        assertEquals (schoolIds[4], iterator.next().getId());
        assertEquals (schoolIds[2], iterator.next().getId());
        assertEquals (schoolIds[1], iterator.next().getId());

        assertEquals (false, iterator.hasNext());
    }

    @Test
    public void listCurrentSchoolsInOrder2() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        // no filters
        Iterator<School> iterator = dao.listSchoolsOfCurrentYear(filter, SchoolDAO.Field.ZIP, false, 0, 2).iterator();

        assertEquals (schoolIds[3], iterator.next().getId());
        assertEquals (schoolIds[1], iterator.next().getId());

        assertEquals (false, iterator.hasNext());
    }

    @Test
    public void listCurrentSchoolsInOrder3() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        // no filters
        Iterator<School> iterator = dao.listSchoolsOfCurrentYear(filter, SchoolDAO.Field.TOWN, true, 1, 1).iterator();

        assertEquals (schoolIds[3], iterator.next().getId());

        assertEquals (false, iterator.hasNext());
    }

    @Test
    public void countCurrentSchools() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        // no filters
        assertEquals (4, dao.countSchoolsOfCurrentYear(filter));
    }

    @Test
    public void listFilteredCurrentSchools1() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        filter.fieldContains(SchoolDAO.Field.NAME, "school");

        Iterator<School> iterator = dao.listSchoolsOfCurrentYear(filter, SchoolDAO.Field.NAME, true, 0, 10).iterator();

        assertEquals (schoolIds[2], iterator.next().getId());
        assertEquals (schoolIds[1], iterator.next().getId());

        assertEquals (false, iterator.hasNext());
    }


    @Test
    public void listFilteredCurrentSchools2() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        filter.fieldStartsWith(SchoolDAO.Field.ZIP, "12");

        Iterator<School> iterator = dao.listSchoolsOfCurrentYear(filter, SchoolDAO.Field.NAME, false, 0, 10).iterator();

        assertEquals (schoolIds[1], iterator.next().getId());
        assertEquals (schoolIds[2], iterator.next().getId());

        assertEquals (false, iterator.hasNext());
    }

    @Test
    public void listFilteredCurrentSchools3() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        filter.fieldEndsWith(SchoolDAO.Field.TOWN, "TOWN");

        Iterator<School> iterator = dao.listSchoolsOfCurrentYear(filter, SchoolDAO.Field.NAME, true, 0, 10).iterator();

        assertEquals (schoolIds[1], iterator.next().getId());

        assertEquals (false, iterator.hasNext());
    }

    @Test
    public void listFilteredSchools() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        filter.fieldStartsWith(SchoolDAO.Field.ZIP, "17");

        Iterator<School> iterator = dao.listSchools(filter, SchoolDAO.Field.NAME, false, 0, 10).iterator();

        assertEquals (schoolIds[3], iterator.next().getId());
        assertEquals (schoolIds[0], iterator.next().getId());

        assertEquals (false, iterator.hasNext());
    }

    @Test
    public void countFilteredCurrentSchools1() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        filter.fieldContains(SchoolDAO.Field.STREET, "street");
        filter.fieldStartsWith(SchoolDAO.Field.TOWN, "china");
        assertEquals (1, dao.countSchoolsOfCurrentYear(filter));
    }

    @Test
    public void countFilteredCurrentSchools3() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        filter.fieldStartsWith(SchoolDAO.Field.STREET, "street");
        filter.fieldStartsWith(SchoolDAO.Field.TOWN, "town");
        assertEquals (0, dao.countSchoolsOfCurrentYear(filter));
    }

    @Test
    public void countFilteredSchools() {
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        filter.fieldContains(SchoolDAO.Field.STREET, "street");
        filter.fieldContains(SchoolDAO.Field.TOWN, "town");
        assertEquals (2, dao.countSchoolsOfCurrentYear(filter));
    }
}
