/* QuestionSetDAO.java
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

import be.bebras.rasbeb.db.data.QuestionInSet;

import java.util.List;

/**
 * Provides operations on question sets.
 */
public interface QuestionSetDAO {

    /**
     * The question set member with given index in the given contest at the given level
     */
    public QuestionInSet getQuestionInSet(int contestId, int level, int index);

    /**
     * List all members of a particular question set, i.e., in a particular contest at a
     * particular level. The result is ordered by index. Members with marksWhenCorrect=0 are
     * not listed.
     */
    public Iterable<QuestionInSet> listQuestionsInSet(int contestId, int level);


    /**
     * Add members for the given question, at every possible level.
     *
     * @return The index of the new members
     */
    public int addQuestionInSet(int contestId, int questionId);

    /**
     * Update the scoring for a question set member
     */
    public void updateQuestionInSet(int contestId, int level, int index,
                                    int marksWhenCorrect, int marksWhenWrong);


    /**
     * Swap the indices for the given questions, at every possible level
     */
    public void swapQuestionInSet (int contestId, int index1, int index2);

    /**
     * Small transfer object that contains the scoring information for a single question. Used in {@link #listQuestionsWithMarks(int)}
     */
    public static class Marks {
        public int whenCorrect;
        public int whenWrong;
    }

    /**
     * Transfer object that extends {@link QuestionInSet} with some language dependent information.
     */
    public static class QuestionWithTitle {
        public int index; // index in a question set
        public String title; // title of a question
    }

    /**
     * List all members of a particular question set, i.e., in a particular contest at a
     * particular level. The result is ordered by index. Members with marksWhenCorrect=0 are
     * not listed. The titles of the questions are taken from the current language.
     */
    public Iterable<QuestionWithTitle> listQuestionsInSetWithTitle (int contestId, int level);

    /**
     * Transfer object that combines information about a question in a contest and the various marks that can be obtained
     * or lost for this question at the various levels.  Used in {@link #listQuestionsWithMarks(int)}
     */
    public static class QuestionWithMarks {
        public int contestId;
        public int questionId;
        public int index;
        public String externalId;
        public String title;
        public List<Marks> marks; // indexed by level-1
    }

    /**
     * Provide the question set details for a given contest. The result is ordered
     * by index.
     */
    public Iterable<QuestionWithMarks> listQuestionsWithMarks(int contestId);


}
