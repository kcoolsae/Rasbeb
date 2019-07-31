/* ListQuestions.java
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

package controllers;

import be.bebras.rasbeb.db.Filter;
import be.bebras.rasbeb.db.dao.QuestionDAO;
import be.bebras.rasbeb.db.data.Question;
import be.bebras.rasbeb.db.data.Role;
import bindings.Pager;
import bindings.QuestionsFilter;
import bindings.Sorter;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import static be.bebras.rasbeb.db.dao.QuestionDAO.Field.*;

/**
 * Handles actions for lists of questions
 * @see Questions
 */
public class ListQuestions extends Controller {

    /**
     * Show a list of all questions
     */
    @InjectContext
    public static Result list(QuestionsFilter sf, Sorter sorter, Pager pager) {
        if (! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }
        QuestionDAO dao = DataAccess.getInjectedContext().getQuestionDAO();
        Filter<QuestionDAO.Field> filter = dao.createListQuestionsFilter();
        filter.fieldContains(EXTERNAL_ID, sf.externalId());
        filter.fieldContains(TITLE, sf.title());

        Iterable<Question> list = dao.listQuestions(filter, valueOf(sorter.sortColumn()), sorter.ascending(),
                pager.offset(), pager.limit());
        int count = (int)dao.countQuestions(filter);
        return ok(views.html.questions.list.render(list, count, sf, sorter, pager));
    }

    public static class FilterSpec {
        public String externalId;
        public String title;
    }



    /**
     * Change the current filter (and reset paging)
     */
    public static Result filter (Sorter s, Pager p) {
        FilterSpec f = new Form<>(FilterSpec.class).bindFromRequest().get();
        return redirect(routes.ListQuestions.list(new QuestionsFilter(f.externalId, f.title), s, p.first()));
    }

    public static class Limit {

        public int limit;
    }

    /**
     *  Change the table size (and rerun query)
     */
    public static Result resize (QuestionsFilter f, Sorter s, int offset) {
        Form<Limit> form = new Form<>(Limit.class).bindFromRequest();
        int limit = form.hasErrors() ? 10 : form.get().limit;
        return redirect(routes.ListQuestions.list(f, s, new Pager(offset,limit)));
    }

    /*
     * HELPER FUNCTIONS FOR VIEWS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Printable version of the range of possible answers for a given question.
     */
    public static String answerRange(Question question) {
        // TODO: move this to Question class?
        if (question.isMultipleChoice()) {
            return "A-" + (char) ('A' - 1 + question.getNumberOfAnswers());
        } else {
            return "*";
        }
    }

}
