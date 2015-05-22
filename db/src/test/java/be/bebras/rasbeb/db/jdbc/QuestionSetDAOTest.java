/* QuestionSetDAOTest.java
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
import be.bebras.rasbeb.db.dao.QuestionSetDAO;
import be.bebras.rasbeb.db.data.QuestionInSet;
import be.bebras.rasbeb.db.data.Role;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Test for the JDBC implementation of {@link be.bebras.rasbeb.db.dao.QuestionSetDAO}
 */
public class QuestionSetDAOTest extends DAOTest {

    private QuestionSetDAO dao;

    private int[] questionIds;

    private int contestId;

    private int[] indices;

    @Before
    public void getDAOAndInitFixtures() {
        context.setPrivileges(Role.ORGANIZER);
        dao = context.getQuestionSetDAO();

        questionIds = Fixtures.createQuestions(context);
        contestId = Fixtures.createContests(context)[0];
        indices = Fixtures.createQuestionSets(context, contestId, questionIds);
    }

    @Test
    public void existingMember() {

        QuestionInSet qis = dao.getQuestionInSet(contestId, 4, indices[3]);
        assertEquals(contestId, qis.getContestId());
        assertEquals(4, qis.getLevel());
        assertEquals(questionIds[3], qis.getQuestionId());
        assertEquals(6, qis.getMarksWhenCorrect());
        assertEquals(-4, qis.getMarksWhenWrong());
        assertEquals(indices[3], qis.getIndex());

    }


    @Test(expected = KeyNotFoundException.class)
    public void nonexistentMember() {
        // index 0 must not exist
        dao.getQuestionInSet(contestId, 4, 0);
    }

    @Test(expected = KeyNotFoundException.class)
    public void nonexistentMember2() {
        // no questions of level 3
        dao.getQuestionInSet(contestId, 3, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongMarks1() {
        dao.updateQuestionInSet(contestId, 3, indices[0], -3, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongMarks2() {
        dao.updateQuestionInSet(contestId, 3, indices[0], 3, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongMarks3() {
        dao.updateQuestionInSet(contestId, 1, indices[1], -2, -2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongMarks4() {
        dao.updateQuestionInSet(contestId, 1, indices[1], 2, 2);
    }

    @Test
    public void updateExisting() {
        dao.updateQuestionInSet(contestId, 1, indices[1], 5, -5);
        QuestionInSet qis = dao.getQuestionInSet(contestId, 1, indices[1]);
        assertEquals(5, qis.getMarksWhenCorrect());
        assertEquals(-5, qis.getMarksWhenWrong());
    }

    @Test(expected = KeyNotFoundException.class)
    public void updateNonexistent() {
        dao.updateQuestionInSet(contestId, 1, 0, 5, -5);
    }

    /**
     * Find the record with the given questionId in the given list
     */
    private QuestionInSet find(Iterable<QuestionInSet> list, int questionId) {
        for (QuestionInSet qis : list) {
            if (qis.getQuestionId() == questionId) {
                return qis;
            }
        }
        return null;
    }

    @Test
    public void listQuestionSet() {
        Iterable<QuestionInSet> list = dao.listQuestionsInSet(contestId, 1);
        assertNotNull(find(list, questionIds[0]));
        assertNotNull(find(list, questionIds[1]));
        assertNull(find(list, questionIds[2]));
        assertNull(find(list, questionIds[3]));
    }

    @Test
    public void listQuestionTitle() {
        Iterator<QuestionSetDAO.QuestionWithTitle> iterator = dao.listQuestionsInSetWithTitle(contestId, 1).iterator();
        assertTrue (iterator.hasNext());
        QuestionSetDAO.QuestionWithTitle qwt = iterator.next();
        assertEquals (1, qwt.index);
        assertEquals ("Hello Bebras", qwt.title);

        assertTrue (iterator.hasNext());
        qwt = iterator.next();
        assertEquals (2, qwt.index);
        assertEquals ("Bebras NG", qwt.title);

        assertFalse (iterator.hasNext());
    }

    private int[][][] expectedMarks = {
            {{3, -1}, {0, 0}, {0, 0}, {0, 0}},
            {{4, -2}, {0, 0}, {0, 0}, {0, 0}},
            {{0, 0}, {5, -3}, {0, 0}, {0, 0}},
            {{0, 0}, {0, 0}, {0, 0}, {6, -4}},
    };

    private void checkMarks(int[][] expected, Iterable<QuestionSetDAO.Marks> list) {
        int pos = 0;
        for (QuestionSetDAO.Marks marks : list) {
            assertEquals(expected[pos][0], marks.whenCorrect);
            assertEquals(expected[pos][1], marks.whenWrong);
            pos++;
        }
    }


    @Test
    public void listQuestionsWithMarks() {
        Iterator<QuestionSetDAO.QuestionWithMarks> iterator = dao.listQuestionsWithMarks(contestId).iterator();

        // for question 1
        QuestionSetDAO.QuestionWithMarks qwp = iterator.next();
        assertEquals("Q0001", qwp.externalId);
        assertEquals("Hello Bebras", qwp.title);
        assertEquals(contestId, qwp.contestId);
        assertEquals(questionIds[0], qwp.questionId);
        assertEquals(1, qwp.index);
        checkMarks(expectedMarks[0], qwp.marks);

        // for question 2
        qwp = iterator.next();
        assertEquals("Q0002", qwp.externalId);
        assertEquals(contestId, qwp.contestId);
        assertEquals(questionIds[1], qwp.questionId);
        assertEquals(2, qwp.index);
        checkMarks(expectedMarks[1], qwp.marks);

        // for question 3
        qwp = iterator.next();
        assertEquals("Q1202", qwp.externalId);
        assertEquals(contestId, qwp.contestId);
        assertEquals(questionIds[2], qwp.questionId);
        assertEquals(3, qwp.index);
        checkMarks(expectedMarks[2], qwp.marks);

        // for question 3
        qwp = iterator.next();
        assertEquals("Q1345", qwp.externalId);
        assertEquals(contestId, qwp.contestId);
        assertEquals(questionIds[3], qwp.questionId);
        assertEquals(4, qwp.index);
        checkMarks(expectedMarks[3], qwp.marks);

    }

    @Test
    public void swapQuestions () {
        dao.swapQuestionInSet(contestId, indices[1], indices[3]);

        QuestionInSet qis = dao.getQuestionInSet(contestId, 4, indices[1]);
        assertEquals(contestId, qis.getContestId());
        assertEquals(4, qis.getLevel());
        assertEquals(questionIds[3], qis.getQuestionId());
        assertEquals(6, qis.getMarksWhenCorrect());
        assertEquals(-4, qis.getMarksWhenWrong());
        assertEquals(indices[1], qis.getIndex());

        qis = dao.getQuestionInSet(contestId, 1, indices[3]);
        assertEquals(contestId, qis.getContestId());
        assertEquals(1, qis.getLevel());
        assertEquals(questionIds[1], qis.getQuestionId());
        assertEquals(4, qis.getMarksWhenCorrect());
        assertEquals(-2, qis.getMarksWhenWrong());
        assertEquals(indices[3], qis.getIndex());
    }
}
