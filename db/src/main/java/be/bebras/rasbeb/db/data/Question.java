/* Question.java
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
 * Contains general information about a question.
 */
public class Question {

    protected int id;

    protected Status status;

    protected String externalId;

    protected String magicQ;

    protected String magicF;

    protected String title;

    protected String correctAnswer;

    private int numberOfAnswers;

    public Question(int id, Status status, String externalId,
                       String magicQ, String magicF, String title,
                       String correctAnswer, int numberOfAnswers) {
        this.id = id;
        this.status = status;
        this.externalId = externalId;
        this.magicQ = magicQ;
        this.magicF = magicF;
        this.title = title;
        this.correctAnswer = correctAnswer;
        this.numberOfAnswers = numberOfAnswers;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getMagicQ() {
        return magicQ;
    }

    public String getMagicF() {
        return magicF;
    }

    /**
     * The title of this question - in the current language.
     */
    public String getTitle() {
        return title;
    }

    /**
     * The correct answer to this question - in the current language. Always uppercase and trimmed. In case of a
     * multiple choice question the answer consists of a single letter A,B,C,...
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * The total number of answers allowed for this question, in case of a multiple choice question.
     * Otherwise 0.
     */
    public int getNumberOfAnswers() {
        return numberOfAnswers;
    }

    /**
     * Whether this is a multiple choice question
     */
    public boolean isMultipleChoice() {
        return numberOfAnswers > 0;
    }


}
