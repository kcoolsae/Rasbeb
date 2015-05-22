/* QuestionDAOTest.java
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

import be.bebras.rasbeb.db.DataFrozenException;
import be.bebras.rasbeb.db.Filter;
import be.bebras.rasbeb.db.KeyNotFoundException;
import be.bebras.rasbeb.db.dao.QuestionDAO;
import be.bebras.rasbeb.db.data.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Test for the JDBC implementation of {@link QuestionDAO}
 */
public class QuestionDAOTest extends DAOTest {

    private QuestionDAO dao;

    private int[] questionIds;

    @Before
    public void getDAOAndInitFixtures() {
        dao = context.getQuestionDAO();
        context.setPrivileges(Role.ORGANIZER);
        questionIds = Fixtures.createQuestions(context);
    }

    @Test
    public void existingMultipleChoiceQuestion() {
        Question question = dao.getQuestion(questionIds[0]);
        assertTrue (question.isMultipleChoice());

        assertNotNull(question.getMagicQ());
        assertNotNull(question.getMagicF());
        assertEquals(Status.DEFAULT, question.getStatus());
        assertEquals(questionIds[0], question.getId());
    }

    @Test
    public void existingOpenEndedQuestion() {
        Question question = dao.getQuestion(questionIds[3]);
        assertFalse(question.isMultipleChoice());

        assertNotNull(question.getMagicQ());
        assertNotNull(question.getMagicF());
        assertEquals(Status.DEFAULT, question.getStatus());
        assertEquals(questionIds[3], question.getId());
    }

    @Test
    public void i18nRecordsInitialized() {
        // check that there is at least one i18n record
        Iterator<QuestionI18n> iterator = dao.listQuestionI18n(questionIds[2]).iterator();
        assertEquals (true, iterator.hasNext());
    }

    @Test
    public void externalIdUpdated() {
        dao.updateExternalId(questionIds[0], "Q9999");
        Question question = dao.getQuestion(questionIds[0]);
        assertEquals("Q9999", question.getExternalId());
    }

    @Test(expected = KeyNotFoundException.class)
    public void cannotUpdateNonexistentQuestion() {
        // question -1 should not exist
        dao.updateExternalId(-1, "7842");
    }

    @Test(expected= KeyNotFoundException.class)
    public void noI18nForNonexistentQuestion() {
        // question -1 should not exist
        dao.updateQuestionI18n(-1, "en", "Title 3", "new answer");
    }

    @Test
    public void numberOfAnswersUpdated () {
        dao.updateNumberOfAnswers(questionIds[0], 6);
        Question question = dao.getQuestion(questionIds[0]);
        assertEquals(6, question.getNumberOfAnswers());
        for (QuestionI18n q: dao.listQuestionI18n(questionIds[0])) {
            assertNull (question.getCorrectAnswer());
        }
    }

    /**
     * Find the record for the given language in the given list.
     */
     private QuestionI18n find (Iterable<QuestionI18n> list, String lang) {
        for (QuestionI18n questionI18n : list) {
            if (lang.equals(questionI18n.getLang())) {
                return questionI18n;
            }
        }
        return null; // not found
    }

    @Test
    public void updateTitleAndAnswer () {
        dao.updateQuestionI18n(questionIds[2], "en", "Title 1", " new answer  ");
        Question question = dao.getQuestion(questionIds[2]);
        assertEquals ("Title 1", question.getTitle()); // "en" is current language!
        assertEquals ("NEW ANSWER", question.getCorrectAnswer());

        QuestionI18n questionI18n = find (dao.listQuestionI18n(questionIds[2]), "en");
        assertEquals("en", questionI18n.getLang());
        assertEquals("Title 1", questionI18n.getTitle());
        assertEquals("NEW ANSWER", questionI18n.getCorrectAnswer());
    }

    @Test(expected = KeyNotFoundException.class)
    public void noUpdateForNonexistentLanguage () {
        dao.updateQuestionI18n(questionIds[3], "??", "Title 3", "new");
    }

    @Test
    public void listAllQuestionsInOrder () {
        Filter<QuestionDAO.Field> filter = dao.createListQuestionsFilter();
        // not filtered
        Iterator<Question> iterator = dao.listQuestions(filter, QuestionDAO.Field.EXTERNAL_ID, false, 0, 10).iterator();

        assertEquals (questionIds[3], iterator.next().getId());
        assertEquals (questionIds[2], iterator.next().getId());
        assertEquals (questionIds[1], iterator.next().getId());
        assertEquals (questionIds[0], iterator.next().getId());

        assertFalse (iterator.hasNext());

    }

    @Test
    public void listLimitedQuestionsInOrder () {
        Filter<QuestionDAO.Field> filter = dao.createListQuestionsFilter();
        // not filtered
        Iterator<Question> iterator = dao.listQuestions(filter, QuestionDAO.Field.TITLE, true, 0, 3).iterator();

        assertEquals (questionIds[1], iterator.next().getId());
        assertEquals (questionIds[3], iterator.next().getId());
        assertEquals (questionIds[2], iterator.next().getId());

        assertFalse (iterator.hasNext());

    }

    @Test
    public void listFilteredQuestions () {
        Filter<QuestionDAO.Field> filter = dao.createListQuestionsFilter();
        filter.fieldContains(QuestionDAO.Field.TITLE, "bebras");
        Iterator<Question> iterator = dao.listQuestions(filter, QuestionDAO.Field.TITLE, true, 0, 10).iterator();

        assertEquals (questionIds[1], iterator.next().getId());
        assertEquals (questionIds[0], iterator.next().getId());

        assertFalse (iterator.hasNext());

    }

    @Test
    public void countFilteredQuestions () {
        Filter<QuestionDAO.Field> filter = dao.createListQuestionsFilter();
        filter.fieldContains(QuestionDAO.Field.EXTERNAL_ID, "02");
        long count = dao.countQuestions(filter);

        assertEquals (2L, count);

    }

    @Test
    public void testFreezeQuestion () {
        assertFalse (dao.freezeQuestion(questionIds[2], "en"));
        dao.updateQuestionI18n(questionIds[2], "en", "new title", "answer");
        assertTrue(dao.freezeQuestion(questionIds[2], "en"));
    }

    @Test(expected = DataFrozenException.class)
    public void cannotUpdateWhenFrozen1 () {
        dao.updateQuestionI18n(questionIds[0], "en", "new title", "answer");
        dao.freezeQuestion(questionIds[0], "en");
        dao.updateQuestionI18n(questionIds[0], "en", "new title", "other answer");
    }

    @Test(expected = DataFrozenException.class)
    public void cannotUpdateWhenFrozen2 () {
        dao.updateQuestionI18n(questionIds[0], "en", "new title", "answer");
        dao.freezeQuestion(questionIds[0], "en");
        dao.updateNumberOfAnswers(questionIds[0], 7);
    }

}
