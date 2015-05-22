/* ParticipationDAOTestBis.java
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

import be.bebras.rasbeb.db.dao.ParticipationDAO;
import be.bebras.rasbeb.db.data.Role;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Other tests of {@link ParticipationDAO} which use student participations
 */
public class ParticipationDAOTestBis extends ActivationDAOTest {

    private ParticipationDAO dao;

    private int[] participationId;

    private int[] indices;

    private int[] contestIds;

    private int[] studentIds;

    private int[] schoolIds;

    @Before
    public void getDAOAndInitFixtures() {
        context.setPrivileges(Role.ORGANIZER);
        context.setUserId(3);
        dao = context.getParticipationDAO();


        int[] questionIds = Fixtures.createQuestions(context);
        contestIds = Fixtures.createContests(context);
        indices = Fixtures.createQuestionSets(context, contestIds[0], questionIds);

        studentIds = Fixtures.createStudents(context);

        schoolIds = Fixtures.createSchools(context);
        int[] classIds = Fixtures.createClasses(context, schoolIds);
        Fixtures.createStudentClasses(context, studentIds, classIds);

        context.setUserId(studentIds[0]);
        participationId = Fixtures.createParticipations(context, contestIds);
    }



    @Test
    public void listMarks() {
        Map<String,List<ParticipationDAO.StudentMarks>> map = dao.listStudentMarksForSchool(contestIds[0], 1, schoolIds[1]);
        List<ParticipationDAO.StudentMarks> list = map.get("3b");
        assertNotNull (list);
        assertEquals (1, list.size());
        ParticipationDAO.StudentMarks marks = list.get(0);
        assertEquals (studentIds[0], marks.studentId);
        assertEquals (10, marks.maximumMarks);
        assertEquals ("Doe, John", marks.name);
        assertEquals (4, marks.totalMarks);

    }
}
