/* TeacherSchoolClassDAOTest.java
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
import be.bebras.rasbeb.db.UniqueViolationException;
import be.bebras.rasbeb.db.dao.TeacherSchoolClassDAO;
import be.bebras.rasbeb.db.dao.TeacherSchoolClassDAO.TeacherSchoolInfo;

import be.bebras.rasbeb.db.data.ClassInfo;
import be.bebras.rasbeb.db.data.School;
import be.bebras.rasbeb.db.data.Student;
import be.bebras.rasbeb.db.data.TeacherSchool;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test for the JDBC implementation of {@link TeacherSchoolClassDAO}
 */
public class TeacherSchoolClassDAOTest extends DAOTest {

    private TeacherSchoolClassDAO dao;

    private int[] schoolIds;

    private int[] teacherIds;

    private int[] tsIds;

    private int[] classIds;

    private int[] studentIds;

    @Before
    public void getDAOAndInitFixtures() {
        dao = context.getTeacherSchoolClassDAO();
        schoolIds = Fixtures.createSchools(context);
        teacherIds = Fixtures.createTeachers(context);
        tsIds = Fixtures.createTeacherSchools(context, teacherIds, schoolIds);
        classIds = Fixtures.createClasses(context, schoolIds);
        studentIds = Fixtures.createStudents(context);
        Fixtures.createStudentClasses(context, studentIds, classIds);
    }

    @Test
    public void getTeacherSchool() {
        TeacherSchool ts = dao.getTeacherSchool(tsIds[1]);
        assertEquals (tsIds[1], ts.getId());
        assertEquals (teacherIds[1],ts.getTeacherId());
        assertEquals ("Alice", ts.getTeacherName());
        assertEquals (schoolIds[3],ts.getSchoolId());
        assertEquals("Royal institute", ts.getSchoolName());
        assertEquals (2014,ts.getYear());
    }


    @Test
    public void listSchoolInfo() {
        Iterator<TeacherSchoolInfo> iterator = dao.listSchoolInfo(teacherIds[1]).iterator();

        // first record
        TeacherSchoolInfo tsi = iterator.next();


        assertEquals(teacherIds[1], tsi.teacherId);
        assertEquals(schoolIds[1], tsi.schoolId);
        assertEquals(tsIds[2], tsi.teacherSchoolId);
        assertEquals ("School of Theology", tsi.name);  // regression test

        assertEquals(2, tsi.classList.size());
        assertEquals("3a", tsi.classList.get(0).getName());
        assertEquals(classIds[0], tsi.classList.get(0).getId());
        assertEquals("3b", tsi.classList.get(1).getName());
        assertEquals(classIds[1], tsi.classList.get(1).getId());

        // second record
        tsi = iterator.next();

        assertEquals(teacherIds[1], tsi.teacherId);
        assertEquals(schoolIds[3], tsi.schoolId);
        assertEquals(tsIds[1], tsi.teacherSchoolId);


        assertEquals(4, tsi.classList.size());
        assertEquals("4a-ec", tsi.classList.get(2).getName());
        assertEquals(classIds[2], tsi.classList.get(3).getId());

        // no more records

        assertFalse(iterator.hasNext());

    }

    @Test
    public void addSchoolForCurrentTeacher() {
        context.setUserId(teacherIds[0]);
        int tsid = dao.addSchool(schoolIds[3]);

        Iterator<TeacherSchoolInfo> iterator = dao.listSchoolInfo(teacherIds[0]).iterator();
        iterator.next(); // skip first

        TeacherSchoolInfo tsi = iterator.next();
        assertEquals(schoolIds[3], tsi.schoolId);
        // no more records

        assertFalse(iterator.hasNext());
    }

    @Test
    public void listSchoolsOfCurrentTeacher () {
        context.setUserId(teacherIds[2]);
        Iterator<School> iterator = dao.listSchools().iterator();

        School school = iterator.next();
        assertEquals (schoolIds[2], school.getId());
        assertEquals ("1200", school.getZip());

        school = iterator.next();
        assertEquals (schoolIds[3], school.getId());
        assertEquals ("1700", school.getZip());

        assertFalse (iterator.hasNext());
    }

    @Test
    public void listSchoolInfoShort () {
        context.setUserId(teacherIds[2]);
        Iterator<TeacherSchoolInfo> iterator = dao.listSchoolInfoShort().iterator();

        TeacherSchoolInfo info = iterator.next();

        assertEquals (teacherIds[2], info.teacherId);
        assertEquals (schoolIds[2], info.schoolId);
        assertEquals (tsIds[3], info.teacherSchoolId);
        assertEquals ("School of Arts", info.name);

        info = iterator.next();
        assertEquals (teacherIds[2], info.teacherId);
        assertEquals (schoolIds[3], info.schoolId);
        assertEquals (tsIds[4], info.teacherSchoolId);

        assertFalse (iterator.hasNext());

    }


    @Test
    public void teacherSchoolWithoutClasses() {
        Iterator<TeacherSchoolInfo> iterator = dao.listSchoolInfo(teacherIds[4]).iterator();

        // first record
        TeacherSchoolInfo tsi = iterator.next();

        assertEquals(teacherIds[4], tsi.teacherId);
        assertEquals(schoolIds[4], tsi.schoolId);
        assertEquals(tsIds[8], tsi.teacherSchoolId);

        assertEquals(0, tsi.classList.size());

    }

    /*

    @Test
    public void listTeacherInfo() {
        Iterator<TeacherSchoolInfo> iterator = dao.listTeacherInfo(schoolIds[1]).iterator();

        // first record
        TeacherSchoolInfo tsi = iterator.next();

        assertEquals(teacherIds[0], tsi.teacherId);
        assertEquals(schoolIds[1], tsi.schoolId);
        assertEquals(tsIds[0], tsi.teacherSchoolId);
        assertEquals ("Bob", tsi.name);              // regression test

        assertEquals(2, tsi.classList.size());

        // second record
        tsi = iterator.next();

        assertEquals(teacherIds[1], tsi.teacherId);
        assertEquals(schoolIds[1], tsi.schoolId);
        assertEquals(tsIds[2], tsi.teacherSchoolId);

        assertEquals(1, tsi.classList.size());

        // third record
        tsi = iterator.next();

        assertEquals(teacherIds[3], tsi.teacherId);
        assertEquals(schoolIds[1], tsi.schoolId);
        assertEquals(tsIds[5], tsi.teacherSchoolId);
        assertEquals(0, tsi.classList.size());

        // no more records

        assertFalse(iterator.hasNext());

    }
        */

    @Test
    public void listClassStudents() {

        Map<String,List<Student>> map = dao.listClassStudentsInSchool (schoolIds[1]);

        List<Student> class3a = map.get ("3a");
        assertNotNull (class3a);
        assertEquals (0, class3a.size());

        List<Student> class3b = map.get ("3b");
        assertNotNull (class3b);
        assertEquals (2, class3b.size());

        Student janeDoe = class3b.get(0);
        assertEquals ("Doe, Jane", janeDoe.getName());
        assertEquals ("initial2", janeDoe.getInitialPassword());

    }

    @Test
    public void fits() {
        context.setUserId(teacherIds[2]);
        assertTrue (dao.fitsCurrentUser(tsIds[3]));
        assertFalse (dao.fitsCurrentUser(tsIds[2]));
    }

    @Test
    public void findClass() {
        assertEquals (classIds[5], dao.findClassInSchool(schoolIds[3], "3b"));
    }

    @Test
    public void getClassInfo() {
        ClassInfo ci = dao.getClass(classIds[5]);
        assertEquals (classIds[5], ci.getId());
        assertEquals (schoolIds[3], ci.getSchoolId());
        assertEquals ("3b", ci.getName());
    }

    @Test(expected = KeyNotFoundException.class)
    public void classNotFound() {
        dao.findClassInSchool(schoolIds[3], "3a");
    }

    @Test(expected = UniqueViolationException.class)
    public void cannotAddStudentToTwoClasses() {
        dao.addStudentToClass(studentIds[0], classIds[3]);
    }
}
