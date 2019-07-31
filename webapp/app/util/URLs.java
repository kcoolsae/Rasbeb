/* URLs.java
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
 * along with the Rasbeb Web Application (file LICENSE in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package util;

import be.bebras.rasbeb.db.data.Question;
import play.Play;

/**
 * Utility class which computes the urls for questions and feedback pages
 */
public final class URLs {


    private static String getBaseURL() {
        String baseURL = Play.application().configuration().getString("rasbeb.questions.url");
        if (baseURL == null) {
            throw new RuntimeException("No questions url configured (rasbeb.questions.url)");
        } else {
            return baseURL;
        }
    }

    /**
     * Return the base url of the question page for the given question (without language dependent part)
     */
    public static String getQuestionBaseURL(Question question) {
        return getBaseURL() + question.getMagicQ();
    }

    /**
     * Return the base url of the feedback page for the given question (without language dependent part)
     */
    public static String getFeedbackBaseURL(Question question) {
        return getBaseURL() + question.getMagicF();
    }

    /**
     * Return the url of the question page of the given question in the given language
     */
    public static String getQuestionURL(Question question, String lang) {
        return getQuestionBaseURL(question) + "/" + lang + "/index.html";
    }

    /**
     * Return the url of the feedback page of the given question in the given language
     */
    public static String getFeedbackURL(Question question, String lang) {
        return getFeedbackBaseURL(question) + "/" + lang + "/index.html";
    }

}
