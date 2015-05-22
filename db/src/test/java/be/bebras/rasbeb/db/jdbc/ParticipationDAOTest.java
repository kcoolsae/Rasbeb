/* ParticipationDAOTest.java
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
import be.bebras.rasbeb.db.data.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Test for the JDBC implementation of {@link ParticipationDAO}
 */
public class ParticipationDAOTest extends ActivationDAOTest {

    private ParticipationDAO dao;

    private int[] participationId;

    private int[] indices;

    private int[] contestIds;

    @Before
    public void getDAOAndInitFixtures() {
        context.setPrivileges(Role.ORGANIZER);
        context.setUserId(3);
        dao = context.getParticipationDAO();


        int[] questionIds = Fixtures.createQuestions(context);
        contestIds = Fixtures.createContests(context);
        indices = Fixtures.createQuestionSets(context, contestIds[0], questionIds);

        participationId = Fixtures.createParticipations(context, contestIds);

    }

    @Test
    public void existingParticipation () {
        Participation participation = dao.get(participationId[0]);
        assertEquals(3, participation.getUserId());
        assertEquals(contestIds[0], participation.getContestId());
        assertEquals(2, participation.getLevel());
        assertEquals (ContestType.OFFICIAL, participation.getContestType());
    }

    @Test
    public void existingParticipationBis () {
        Participation participation = dao.get(participationId[1]);
        assertEquals(4, participation.getUserId());
        assertEquals(contestIds[0], participation.getContestId());
        assertEquals(1, participation.getLevel());
        assertEquals (3, participation.getInitialMarks());
        assertEquals (10, participation.getMaximumMarks());
    }

    @Test
    public void existingParticipationTer() {
        Participation participation = dao.get(participationId[2]);
        assertEquals(5, participation.getUserId());
        assertEquals(contestIds[1], participation.getContestId());
        assertEquals(ContestType.PUBLIC, participation.getContestType());
    }

    @Test
    public void participationDetails() {
        ParticipationDetail detail = dao.get(participationId[1], 2);
        assertEquals (participationId[1], detail.getParticipationId());
        assertEquals (2, detail.getIndexInParticipation());
        assertEquals (indices[1], detail.getIndexInContest());
        assertNull(detail.getAnswer());
        assertNull(detail.getMarks());
    }

    @Test
    public void questionsInParticipation() {
        dao.updateAnswer(participationId[1], 2, "answer");

        Iterator<ParticipationDAO.QuestionDetails> iterator = dao.listQuestionDetails(participationId[1]).iterator();
        ParticipationDAO.QuestionDetails qip = iterator.next();
        assertEquals("Hello Bebras", qip.title);
        assertEquals(1, qip.indexInParticipation);
        assertFalse (qip.answered);
        assertNull (qip.marks);
        qip = iterator.next();
        assertEquals("Bebras NG", qip.title);
        assertEquals(2, qip.indexInParticipation);
        assertTrue (qip.answered);
        assertNull (qip.marks);

        assertFalse(iterator.hasNext());
    }

    @Test
    public void updateAnswers() {
        dao.updateAnswer(participationId[1], 2, " ANswer ");
        ParticipationDetail detail = dao.get(participationId[1], 2);
        assertEquals ("ANSWER", detail.getAnswer());

        dao.updateAnswer(participationId[1], 2, null);
        detail = dao.get(participationId[1], 2);
        assertNull(detail.getAnswer());
    }

    @Test
    public void findParticipation() {
        context.setUserId(3);
        Participation part = dao.findClosedParticipation(contestIds[0], 2);
        assertNull (part);
        dao.closeParticipation(participationId[0]);
        part = dao.findClosedParticipation(contestIds[0], 2);
        assertNotNull(part);
        assertEquals (participationId[0], part.getId());
        part = dao.findClosedParticipation(contestIds[0], 3);
        assertNull(part);
        context.setUserId(2);
        part = dao.findClosedParticipation(contestIds[0], 2);
        assertNull(part);
    }
}
