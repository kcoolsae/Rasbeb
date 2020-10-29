/* LocalContestDAOTest.java
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


import be.bebras.rasbeb.db.dao.LocalContestDAO;
import be.bebras.rasbeb.db.data.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests LocalContestDAO
 */
public class LocalContestDAOTest extends DAOTest {

    private LocalContestDAO dao;

    private int[] contestIds;

    private int[] teacherIds;

    private int[] schoolIds;

    private int[] tsIds;

    private int[] lcIds;

    private int[] studentIds;

    @Before
    public void getDAOAndInitFixtures() {
        dao = context.getLocalContestDAO();
        context.setPrivileges(Role.ORGANIZER);
        contestIds = Fixtures.createContests(context);
        teacherIds = Fixtures.createTeachers(context);
        schoolIds = Fixtures.createSchools(context);
        //tsIds = Fixtures.createTeacherSchools(context, teacherIds, schoolIds);
        lcIds = Fixtures.createLocalContests(context, contestIds, schoolIds);
        studentIds = Fixtures.createStudents(context);
    }

    @Test
    public void get() {
        assertLC1 (dao.getLocalContest(lcIds[1]));
    }

    @Test
    public void list() {
        Iterator<LocalContest> iter = dao.listLocalContestsInSchool(schoolIds[1]).iterator();
        iter.next(); // skip first result (Never)
        LocalContest lc = iter.next();
        assertEquals(lc.getComment(), "Next thursday");
        assertEquals(lc.getContestId(), contestIds[0]);
        assertEquals(lc.getId(), lcIds[0]);
        assertEquals(lc.getLang(), "en" );
        assertEquals(lc.getLevel(), "Wooki");
        assertEquals(lc.getTitle(), "English title 0");
        assertEquals(lc.getSchoolId(), schoolIds[1]);

        lc = iter.next();
        assertLC1(lc);

        assertFalse (iter.hasNext());

    }

    private void assertLC1(LocalContest lc) {
        assertEquals(lc.getComment(), "Next friday");
        assertEquals(lc.getContestId(), contestIds[1]);
        assertEquals(lc.getId(), lcIds[1]);
        assertEquals(lc.getLang(), "en" );
        assertEquals(lc.getLevel(), "Jedi");
        assertEquals(lc.getTitle(), "English title 1");
        assertEquals(lc.getSchoolId(), schoolIds[1]);
    }

    @Test
    public void canGrantTwice() {
        // this does not crash
        dao.grantPermission(lcIds[0], studentIds[0]);
        dao.grantPermission(lcIds[0], studentIds[0]);

    }

    /* TODO: these tests no longer compile. Should create
       a new context for every student id

    @Test
    public void permissions() {

        dao.grantPermission(lcIds[0], studentIds[0]);
        assertEquals (true, dao.hasPermission(lcIds[0], studentIds[0]));
        assertEquals (false, dao.hasPermission(lcIds[0], studentIds[1]));
        assertEquals (false, dao.hasPermission(lcIds[1], studentIds[0]));
        dao.removePermission(lcIds[0], studentIds[0]);
        assertEquals (false, dao.hasPermission(lcIds[0], studentIds[0]));

    }

    @Test
    public void grantAll () {
        int[] classIds = Fixtures.createClasses(context, schoolIds);
        Fixtures.createStudentClasses(context, studentIds, classIds);
        dao.grantPermissionToClass(lcIds[3], "3b");
        assertEquals (true, dao.hasPermission(lcIds[3], studentIds[0]));
        assertEquals (true, dao.hasPermission(lcIds[3], studentIds[1]));
        assertEquals (false, dao.hasPermission(lcIds[3], studentIds[2]));
        dao.removePermissionFromClass(lcIds[3], "3b");
        assertEquals (false, dao.hasPermission(lcIds[3], studentIds[0]));
        assertEquals (false, dao.hasPermission(lcIds[3], studentIds[1]));
        assertEquals (false, dao.hasPermission(lcIds[3], studentIds[2]));

    }

    */

    @Test
    public void listPermissions() {
        int[] classIds = Fixtures.createClasses(context, schoolIds);
        Fixtures.createStudentClasses(context, studentIds, classIds);

        dao.grantPermission(lcIds[3], studentIds[1]);

        Map<String,List<LocalContestDAO.StudentPermission>> map = dao.listPermissions(lcIds[3]);
        List<LocalContestDAO.StudentPermission> list = map.get("3b");
        assertEquals(2, list.size());
        LocalContestDAO.StudentPermission sp = list.get(0);
        assertEquals ("Doe, Jane", sp.name);
        assertEquals (studentIds[1], sp.studentId);
        assertEquals (true, sp.permitted);

        sp = list.get(1);
        assertEquals ("Doe, John", sp.name);
        assertEquals (studentIds[0], sp.studentId);
        assertEquals (false, sp.permitted);
    }
    
    private boolean canBeOpened (int id) {
        return dao.getLocalContest(lcIds[id]).getStatus() == LCStatus.CAN_BE_OPENED;
        
    }

    @Test
    public void canBeOpened() {
        assertEquals (false, canBeOpened(0));
        assertEquals (true, canBeOpened(1));
        assertEquals (true, canBeOpened(2));
        assertEquals (false, canBeOpened(3));

        context.getContestDAO().updateStatus(contestIds[0], Status.ACTIVE);

        assertEquals (true, canBeOpened(0));
        assertEquals (true, canBeOpened(1));
        assertEquals (true, canBeOpened(2));
        assertEquals (true, canBeOpened(3));
    }

    @Test
    public void listPermittedLocalContests() {
        dao.grantPermission(lcIds[3], studentIds[1]);
        context.setUserId(studentIds[1]);
        Iterator<LocalContest> iterator = dao.listPermittedLocalContests().iterator();
        assertEquals(true, iterator.hasNext());
        assertEquals("Never", iterator.next().getComment());
        assertEquals(false, iterator.hasNext());

        // TODO: add a participation
    }

}
