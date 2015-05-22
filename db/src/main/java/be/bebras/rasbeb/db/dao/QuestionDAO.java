/* QuestionDAO.java
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

package be.bebras.rasbeb.db.dao;

import be.bebras.rasbeb.db.Filter;
import be.bebras.rasbeb.db.data.Question;
import be.bebras.rasbeb.db.data.QuestionI18n;

/**
 * Provides operations on questions.
 */
public interface QuestionDAO {
    
    public static enum Field {
        EXTERNAL_ID, TITLE
    }

    /**
     * The question with the given id.
     */
    public Question getQuestion (int id);

    /**
     * Create a new question in the database. (Also generates the magic numbers.)
     * @param numberOfAnswers Number of possible answers to the question when it is multiple choice, or 0
     *                        to indicate that it is an open-ended question.
     */
    public int createQuestion (int numberOfAnswers, String externalId);

    /**
     * Update the external identifier of a question.
     */
    public void updateExternalId (int id, String externalId);

    /**
     * Update the number of answers for a question. Also resets
     * the answers to null. Not allowed when the question is 'frozen' for any
     * of its languages.
     */
    public void updateNumberOfAnswers (int id, int numberOfAnswers);

    /**
     * Try to freeze the question for a given language. Only possible when the answer for the given
     * language is already registered
     * @return whether freeze succeeded.
     */
    public boolean freezeQuestion (int id, String language);

    /**
     * List the language dependent information for a question (ordered by language).
     */
    public Iterable<QuestionI18n> listQuestionI18n (int id);

    /**
     * Update language dependent information for a question. Not allowed when the question is 'frozen'.
     */
    public void updateQuestionI18n (int id, String lang, String title, String correctAnswer);

    /**
     * Indicate that the question pages have been uploaded
     */
    public void setQuestionUploaded (int id, String lang);

    /**
     * Indicate that the feedback pages have been uploaded
     */
    public void setFeedbackUploaded (int id, String lang);

    /**
     * Create an empty filter for use with {@link #listQuestions}. The filter can be refined by calling the appropriate
     * methods from {@link Filter}.
     */
    public Filter<Field> createListQuestionsFilter();

    /**
     * Return a (partial) list of questions.
     * @param filter Filters the generated list
     * @param orderBy Field according to which the result should be sorted
     * @param ascending Whether the result should be in ascending or in descending order
     * @param offset Sequence number of the first result that should be retrieved (used in paging)
     * @param limit Maximum number of results that should be retrieved (used in paging)
     */
    public Iterable<Question> listQuestions (Filter<Field> filter, Field orderBy, boolean ascending, int offset, int limit);

    /**
     * Count the total number of questions satisfying a given filter, i.e., the number of questions that would be returned
     * by {@link #listQuestions} if there were no offset and limit. Can be used in paging.
     */
    public long countQuestions (Filter<Field> filter);

    // TODO: list/count questions filtered
    // TODO: delete a question?
    // TODO: trim

}
