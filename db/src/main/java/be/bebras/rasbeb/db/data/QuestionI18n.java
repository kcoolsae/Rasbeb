/* QuestionI18n.java
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
 * Language dependent question information
 */
public class QuestionI18n {

    private int id;

    private String lang;

    private String title;

    private String correctAnswer;

    private boolean uploadedQ;

    private boolean uploadedF;

    private boolean frozen;

    public int getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    /**
     * The title of this question.
     * @see Question#title
     */
    public String getTitle() {
        return title;
    }

    public boolean isUploadedQ() {
        return uploadedQ;
    }

    public boolean isUploadedF() {
        return uploadedF;
    }

    /**
     * A question can only be used as part of a question set if it is 'frozen', and in that
     * case only minimal changes to that question are possible. A question can be frozen (for a given language) only
     * if the answer to that question is available and both question and feedback pages haven been uploaded.
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * The correct answer for the question in the current language.
     * @see Question#correctAnswer
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public QuestionI18n(int id, String lang, String title, String correctAnswer, boolean uploadedQ, boolean uploadedF, boolean frozen) {
        this.id = id;
        this.lang = lang;
        this.title = title;
        this.correctAnswer = correctAnswer;
        this.uploadedQ = uploadedQ;
        this.uploadedF = uploadedF;
        this.frozen = frozen;
    }
}
