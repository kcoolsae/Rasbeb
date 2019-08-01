/* Participations.java
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

import be.bebras.rasbeb.db.dao.LocalContestDAO;
import be.bebras.rasbeb.db.dao.ParticipationDAO;
import be.bebras.rasbeb.db.dao.QuestionSetDAO;
import be.bebras.rasbeb.db.data.*;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import util.URLs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for participation related stuff
 */
public class Participations extends Controller {

    /**
     * Shows the page with the start button. Intended for anonymous contests only
     */
    @InjectContext
    public static Result showStart(int id, int level) {

        Contest contest = DataAccess.getInjectedContext().getContestDAO().getContest(id);
        if (contest.getType() != ContestType.PUBLIC) {
            return badRequest();
        }
        Level lvl = DataAccess.getInjectedContext().getLevelDAO().getLevel(level);
        int minutes = DataAccess.getInjectedContext().getContestDAO().getDuration(id, level);
        return ok(views.html.participation.show.render(contest, lvl, minutes));
    }

    /**
     * Shows the page with the start button (for a local competition). Users must be logged in to see this page.
     */
    @InjectContext
    public static Result showLocalStart(int lcId) {
        if (CurrentUser.isLoggedOut()) {
            return redirect(routes.Authentication.loginForm()); // log in
        }
        LocalContest lc = DataAccess.getInjectedContext().getLocalContestDAO().getLocalContest(lcId);
        if (lc.getStatus() != LCStatus.OPEN) {
            // not open!
            return badRequest();
        }
        int minutes = DataAccess.getInjectedContext().getContestDAO().getDuration(lc.getContestId(), lc.getLevelId());
        return ok(views.html.participation.showLocal.render(lc, minutes));
    }


    /**
     * Starts (and activates) the competition. Intended for public contests only
     */
    @InjectContext
    public static Result start(int id, int level) {
        Contest contest = DataAccess.getInjectedContext().getContestDAO().getContest(id);
        if (contest.getType() == ContestType.PUBLIC || CurrentUser.hasRole(Role.ORGANIZER)) {
            createParticipation(id, level);
            return redirect(routes.Participations.showQuestion(1));
        } else {
            return badRequest(); // avoid illegal preview of questions
        }
    }

    /**
     * Starts (and activates) a local competition.
     */
    @InjectContext
    public static Result startLocal(int id) {
        if (CurrentUser.isLoggedOut()) {
            return redirect(routes.Authentication.loginForm()); // log in
        }
        LocalContestDAO dao = DataAccess.getInjectedContext().getLocalContestDAO();
        LocalContest lc = dao.getLocalContest(id);
        // check whether current user is allowed to take this local competition
        if (lc.getStatus() == LCStatus.OPEN && dao.hasPermission(lc.getId())) {
            createParticipation(lc.getContestId(), lc.getLevelId());
            return redirect(routes.Participations.showQuestion(1));
        } else {
            return badRequest();
        }

    }

    private static void createParticipation(int contestId, int level) {

        String str = session("part");
        if (str == null) { // avoids creating two participations for the same contest, use case: back button

            ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
            int pid = dao.createParticipation(contestId, level, DataAccess.getInjectedContext().getLang());
            session("part", Integer.toString(pid));
        }
        session("stamp", Long.toHexString(new Date().getTime()));
    }

    /**
     * Show page which allows taking over a running participation
     */
    @InjectContext
    public static Result takeOverStart(int lcId) {
        LocalContest lc = DataAccess.getInjectedContext().getLocalContestDAO().getLocalContest(lcId);
        if (lc.getStatus() == LCStatus.OPEN) {
            return ok(views.html.participation.takeover.render(lc));
        } else {
            return badRequest();
        }
    }

    /**
     * Take over a running participation
     */
    @InjectContext
    public static Result takeOver(int lcId) {
        LocalContest lc = DataAccess.getInjectedContext().getLocalContestDAO().getLocalContest(lcId);
        String str = session("part");
        if (lc.getStatus() != LCStatus.OPEN || str != null) {
            return badRequest();
        }

        ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
        Participation participation = dao.findParticipationWithStatus(lc.getContestId(), lc.getLevelId(), be.bebras.rasbeb.db.data.Status.DEFAULT);
        if (participation == null) {
            return badRequest();
        } else {
            session("part", Integer.toString(participation.getId()));
            session("stamp", Long.toHexString(new Date().getTime()));
            return redirect(routes.Participations.showQuestion(1));
        }
    }

    /**
     * Transfer object for {@link #showQuestion}.
     */
    public static class ShowQuestionArg {

        public ParticipationDetail detail;
        public Iterable<ShowQuestionDetail> nav;
        public QuestionInSet qis;
        public String questionURL;
        public int secondsLeft;
        public int totalSeconds;
    }

    public static class ShowQuestionDetail {
        public boolean current;
        public String styleClass;
        public int index;
        public String title;
    }

    private static Iterable<ShowQuestionDetail> toShowQuestionDetails(Iterable<ParticipationDAO.QuestionDetails> list, int currentIndex) {
        List<ShowQuestionDetail> result = new ArrayList<>();
        for (ParticipationDAO.QuestionDetails el : list) {
            ShowQuestionDetail detail = new ShowQuestionDetail();
            detail.index = el.indexInParticipation;
            detail.title = el.title;
            if (detail.index == currentIndex) {
                detail.current = true;
                detail.styleClass = el.answered ? "current answered" : "current";
            } else {
                detail.current = false;
                detail.styleClass = el.answered ? "answered" : "";
            }
            result.add(detail);
        }
        return result;
    }

    /**
     * Retrieve the current participation id.
     * Returns 0 if no participation is current and signals an error to the user. (Use case: using back button after time out.)
     */
    private static int getParticipationId() {
        String part = session("part");
        if (part != null) {
            try {
                return Integer.parseInt(part);
            } catch (NumberFormatException ex) {
                // error is reported below
            }
        }
        flash("error", "contest.forced.closed");
        return 0;
    }

    /**
     * Shows the question with given index in the current participation
     */
    @InjectContext
    public static Result showQuestion(int index) {
        int pid = getParticipationId();
        if (pid == 0) {
            return redirectToHome();
        }
        ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
        Participation part = dao.get(pid);

        if (part.getStatus() != be.bebras.rasbeb.db.data.Status.DEFAULT) {
            return redirectToClosed(part.getContestType(), pid);
        } else if (part.getSecondsLeft() < 0) {
            flash("error", "contest.forced.closed");
            return finished();
        }

        int count = dao.countDetails(pid);
        if (index > count) {
            index = 1; // TODO: redirect to special page?
        }
        QuestionSetDAO qsdao = DataAccess.getInjectedContext().getQuestionSetDAO();

        ShowQuestionArg arg = new ShowQuestionArg();
        arg.detail = dao.get(pid, index);
        arg.nav = toShowQuestionDetails(dao.listQuestionDetails(pid), index);
        arg.qis = qsdao.getQuestionInSet(part.getContestId(), part.getLevel(), arg.detail.getIndexInContest());
        arg.secondsLeft = part.getSecondsLeft();

        Question question = DataAccess.getInjectedContext().getQuestionDAO().getQuestion(arg.qis.getQuestionId());
        arg.questionURL = URLs.getQuestionURL(question, part.getLang());

        arg.totalSeconds = 60 * DataAccess.getInjectedContext().getContestDAO().getDuration(part.getContestId(), part.getLevel());

        if (question.isMultipleChoice()) {
            return ok(views.html.participation.questionMC.render(question, arg));
        } else {
            return ok(views.html.participation.questionOE.render(question, arg));
        }
    }

    // TODO: factor out common code for questions and feedback

    /**
     * Transfer object for {@link #showFeedback}.
     */
    public static class ShowFeedbackArg {
        public Participation part;
        public ParticipationDetail detail;
        public Iterable<ShowFeedbackDetail> nav;
        public QuestionInSet qis;
        public String questionURL;
        public String feedbackURL;
    }

    public static class ShowFeedbackDetail {
        public boolean current;
        public String styleClass;
        public int index;
        public String title;
    }

    private static Iterable<ShowFeedbackDetail> toShowFeedbackDetails(Iterable<ParticipationDAO.QuestionDetails> list, int currentIndex) {
        List<ShowFeedbackDetail> result = new ArrayList<>();
        for (ParticipationDAO.QuestionDetails el : list) {
            ShowFeedbackDetail detail = new ShowFeedbackDetail();
            detail.index = el.indexInParticipation;
            detail.title = el.title;
            if (detail.index == currentIndex) {
                detail.current = true;
                detail.styleClass = el.answered ? (el.marks < 0 ? "current wrong" : "current correct") : "current";
            } else {
                detail.current = false;
                detail.styleClass = el.answered ? (el.marks < 0 ? "wrong" : "correct") : "";
            }
            result.add(detail);
        }
        return result;
    }


    /**
     * Shows the question + feedback with the given index in the current participation
     */
    @InjectContext
    public static Result showFeedback(int index) {
        int pid = Integer.parseInt(session("feedback")); // TODO: errors
        ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
        int count = dao.countDetails(pid);
        if (index > count) {
            index = 1; // TODO: redirect to special page?
        }
        QuestionSetDAO qsdao = DataAccess.getInjectedContext().getQuestionSetDAO();

        ShowFeedbackArg arg = new ShowFeedbackArg();
        arg.part = dao.get(pid);
        arg.detail = dao.get(pid, index);
        arg.nav = toShowFeedbackDetails(dao.listQuestionDetails(pid), index);
        arg.qis = qsdao.getQuestionInSet(arg.part.getContestId(), arg.part.getLevel(), arg.detail.getIndexInContest());

        Question question = DataAccess.getInjectedContext().getQuestionDAO().getQuestion(arg.qis.getQuestionId());
        arg.questionURL = URLs.getQuestionURL(question, arg.part.getLang());
        arg.feedbackURL = URLs.getFeedbackURL(question, arg.part.getLang());

        return ok(views.html.participation.feedback.render(question, arg));
    }

    public static class AnswerData {
        public String answer;
    }

    /**
     * Registers an answer to a particular question
     */
    @InjectContext
    public static Result answer(int index) {
        // check whether sufficient time is left

        Form<AnswerData> form = new Form<>(AnswerData.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }

        String answer = form.get().answer;
        if (answer != null && answer.isEmpty()) {
            answer = null;
        }
        int pid = getParticipationId();
        if (pid == 0) {
            return redirectToHome();
        }
        ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
        Participation part = dao.get(pid);

        if (part.getStatus() != be.bebras.rasbeb.db.data.Status.DEFAULT) {
            return redirectToClosed(part.getContestType(), pid);  // participation was already closed
        } else if (part.getSecondsLeft() < -10) {
            flash("error", "contest.forced.closed");
            return finished(); // time is up, answer is no longer registered
        } else {
            dao.updateAnswer(pid, index, answer);
            if (part.getSecondsLeft() < 0) {
                flash("error", "contest.forced.just.closed");
                return finished(); // answer still counts, but time is up
            } else {
                return redirect(routes.Participations.showQuestion(index + 1));
            }
        }
    }

    /**
     * Show the warning page about being finished
     */
    @InjectContext
    public static Result showFinished() {
        int pid = getParticipationId();
        if (pid == 0) {
            return redirectToHome();
        }
        ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
        Participation part = dao.get(pid);


        return ok(views.html.participation.acknowledge.render(
                part.getSecondsLeft(),
                60 * DataAccess.getInjectedContext().getContestDAO()
                        .getDuration(part.getContestId(), part.getLevel())
        ));
    }

    /**
     * Close the current participation and compute the results
     */
    @InjectContext
    public static Result finished() {
        int pid = getParticipationId();
        if (pid == 0) {
            return redirectToHome();
        }
        ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
        dao.closeParticipation(pid);
        return redirectToClosed(dao.get(pid).getContestType(), pid);
    }

    /**
     * Redirect to the home page.
     */
    private static Result redirectToHome() {
        return redirect(routes.Application.index());
    }

    /**
     * Redirect to either 'closed' or 'localClosed', depending on the
     * contest type
     */
    private static Result redirectToClosed(ContestType contestType, int pid) {
        session().remove("part");
        if (contestType == ContestType.PUBLIC) {
            session("feedback", Integer.toString(pid));
            return redirect(routes.Participations.showClosed());
        } else {
            return redirect(routes.Participations.showLocalClosed());
        }
    }

    /**
     * Show the page that acknowledges the local contest is finished.
     */
    public static Result showLocalClosed() {
        return ok(views.html.participation.localClosed.render());
    }

    /**
     * Details shown by {@link #showClosed()}
     */
    public static class ShowClosedDetail {
        public int index;
        public String styleClass;
        public int marks;
        public int maximumMarks;
        public String title;
    }

    /**
     * Show the page that acknowledges the contest was finished
     */
    @InjectContext
    public static Result showClosed() {
        int pid = Integer.parseInt(session("feedback")); // TODO: errors
        ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
        Participation part = dao.get(pid);

        // check whether public (maybe redundant?)
        if (part.getContestType() != ContestType.PUBLIC) {
            return badRequest();
        }

        List<ShowClosedDetail> result = getClosedDetails(pid, dao);

        return ok(views.html.participation.closed.render(part.getTotalMarks(), part.getMaximumMarks(), result));
    }

    private static List<ShowClosedDetail> getClosedDetails(int pid, ParticipationDAO dao) {
        List<ShowClosedDetail> result = new ArrayList<>();
        for (ParticipationDAO.QuestionDetails el : dao.listQuestionDetails(pid)) {
            ShowClosedDetail detail = new ShowClosedDetail();
            detail.index = el.indexInParticipation;
            detail.marks = el.marks;
            detail.maximumMarks = el.maximumMarks;
            detail.title = el.title;
            detail.styleClass = el.answered ? (el.marks < 0 ? "wrong" : "correct") : "";
            result.add(detail);
        }
        return result;
    }

    /**
     * Show the warning page about going back to te home page
     */
    @InjectContext
    public static Result showTerminate() {
        int pid = Integer.parseInt(session("feedback")); // TODO: errors
        ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
        Participation part = dao.get(pid);
        if (part.getContestType() == ContestType.PUBLIC) {
            return ok(views.html.participation.terminate.render(part.getTotalMarks(), part.getMaximumMarks()));
        } else {
            return terminate();
        }
    }

    /**
     * Terminate the session and go back to the home page
     */
    public static Result terminate() {
        session().remove("feedback");
        return redirectToHome();
    }

    /**
     * Show an overview of the results of a local contest.
     */
    @InjectContext
    public static Result showOverview(int lcId) {
        LocalContest lc = DataAccess.getInjectedContext().getLocalContestDAO().getLocalContest(lcId);
        if (lc.getStatus() != LCStatus.CLOSED) {
            // results are not yet available
            return badRequest();
        }
        int contestId = lc.getContestId();
        int level = lc.getLevelId();
        ParticipationDAO dao = DataAccess.getInjectedContext().getParticipationDAO();
        Participation part = dao.findParticipationWithStatus(contestId, level, be.bebras.rasbeb.db.data.Status.CLOSED);
        if (part == null) {
            return badRequest();
        }

        int pid = part.getId();
        session("feedback", Integer.toString(pid));
        List<ShowClosedDetail> result = getClosedDetails(pid, dao);

        return ok(views.html.participation.overview.render(part.getTotalMarks(), part.getMaximumMarks(), result));
    }
}
