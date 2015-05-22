/* QuestionInSet.java
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

package be.bebras.rasbeb.db.data;

/**
 * A question which has been assigned to a question set of a contest and for which the scoring is
 * determined.
 *
 * A question set consists of all objects of this type with a fixed level and assigned to a fixed contest.
 */
public class QuestionInSet {
    
    private int contestId;

    private int questionId;

    private int index;
    
    private int marksWhenCorrect;
    
    private int marksWhenWrong;
    
    private int level;

    /**
     * The id of the corresponding {@link Contest} 
     */
    public int getContestId() {
        return contestId;
    }

    /**
     * The id of the corresponding {@link Question} 
     */
    public int getQuestionId() {
        return questionId;
    }

    /**
     * The 'sequence number' of the question within its set.
     * Sequence numbers are not consecutive and only used to ensure a fixed ordering when
     * listing the members of a set
     */
    public int getIndex() {
        return index;
    }

    /**
     * The number of marks that are earned when giving a correct answer to this question.
     * Always a positive number. Objects for which this value is zero are ignored.
     */
    public int getMarksWhenCorrect() {
        return marksWhenCorrect;
    }

    /**
     * The number of marks that are 'earned 'when giving a wrong answer to this question.
     * Always a negative number.
     */
    public int getMarksWhenWrong() {
        return marksWhenWrong;
    }

    /**
     * The level of the question set to which this question is assigned.
     */
    public int getLevel() {
        return level;
    }

    public QuestionInSet(int contestId, int level, int index, int questionId, int marksWhenCorrect, int marksWhenWrong) {
        this.contestId = contestId;
        this.questionId = questionId;
        this.index = index;
        this.marksWhenCorrect = marksWhenCorrect;
        this.marksWhenWrong = marksWhenWrong;
        this.level = level;
    }
}
