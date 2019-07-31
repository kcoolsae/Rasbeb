/* Preview.java
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

import be.bebras.rasbeb.db.dao.ContestDAO;
import be.bebras.rasbeb.db.dao.QuestionSetDAO;
import be.bebras.rasbeb.db.data.*;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.URLs;
import views.html.preview.list;
import views.html.preview.preview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Preview questions
 */
public class Preview extends Controller {

    @InjectContext
    public static Result list () {
        if (! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }
        ContestDAO dao = DataAccess.getInjectedContext().getContestDAO();
        Iterable<ContestDAO.ContestAvailableLevels> available = dao.listOrganizableContests();
        return ok(list.render(available));
    }

    /**
     * Arguments for the page shown by showQuestion
     */
    public static class Arg {
        public QuestionInSet qis;
        public String questionURL;
        public String feedbackURL;
        public Iterable<Participations.ShowQuestionDetail> nav;
    }
    
    private static Iterable<Participations.ShowQuestionDetail> toShowQuestionDetails(Iterable<QuestionSetDAO.QuestionWithTitle> list, int currentIndex) {
        List<Participations.ShowQuestionDetail> result = new ArrayList<>();
        for (QuestionSetDAO.QuestionWithTitle el : list) {
            Participations.ShowQuestionDetail detail = new Participations.ShowQuestionDetail();
            detail.index = el.index;
            detail.title = el.title;
            if (detail.index == currentIndex) {
                detail.current = true;
                detail.styleClass = "current";
            } else {
                detail.current = false;
                detail.styleClass = "";
            }
            result.add(detail);
        }
        return result;
    }

    public static Result showFirstQuestion (int contestId, int level) {
        if (! CurrentUser.hasRole(Role.TEACHER) && ! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }

        Http.Cookie cookie = request().cookie(Play.langCookieName());
        String lang = cookie == null ? "en" : cookie.value();
        return redirect(routes.Preview.showQuestion(contestId, level, lang, 0));

    }


    @InjectContext
    public static Result showQuestion (int contestId, int level, String lang, int index) {
        // note: this is quite different from the method showQuestion or showFeedback in Participations,
        // because the index now refers to a question set and not to the participation. Indices
        // are also not guaranteed to be contiguous.

        if (CurrentUser.hasRole(Role.TEACHER)) {
            Contest contest = DataAccess.getInjectedContext().getContestDAO().getContest(contestId);
            if (contest.getType() == ContestType.OFFICIAL && contest.getStatus() != be.bebras.rasbeb.db.data.Status.CLOSED) {
                return badRequest();
            }
        } else if (! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }

        Arg arg = new Arg();
        QuestionSetDAO qsdao = DataAccess.getInjectedContext().getQuestionSetDAO();
        Iterable<QuestionSetDAO.QuestionWithTitle> list = qsdao.listQuestionsInSetWithTitle(contestId, level);
        if (index == 0) {
            Iterator<QuestionSetDAO.QuestionWithTitle> iterator = list.iterator();
            if (iterator.hasNext()) {
                index = iterator.next().index;
            }
        }
        arg.nav = toShowQuestionDetails(list, index);

        arg.qis = qsdao.getQuestionInSet(contestId, level, index);
        Question question = DataAccess.getInjectedContext().getQuestionDAO().getQuestion(arg.qis.getQuestionId());

        arg.questionURL = URLs.getQuestionURL(question, lang);
        arg.feedbackURL = URLs.getFeedbackURL(question, lang);


        return ok(preview.render(question, arg));
    }
}
