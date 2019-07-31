/* Application.java
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
import be.bebras.rasbeb.db.dao.LocalContestDAO;
import be.bebras.rasbeb.db.data.LocalContest;
import be.bebras.rasbeb.db.data.Role;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.init.*;

public class Application extends Controller {


    /**
     * Dispatches the new user to either the language chooser, the home page,
     * the landing page for his specific role or the first question page if the user is currently
     * participating in a competition.
     */
    public static Result index() {
        flash().isDirty = true; // seems enough to carry over the flash

        Http.Cookie cookie = request().cookie(Play.langCookieName());
        if (cookie == null) {
            // no language chosen
            return redirect(routes.Language.init());
        }

        if (session("part") != null) {
            // currently taking part in a contest
            return redirect(routes.Participations.showQuestion(1));
        }

        if (session("feedback") != null) {
            // currently viewing feedback
            return redirect(routes.Participations.showFeedback(1));
        }

        if (CurrentUser.isLoggedOut()) {
            return redirect(routes.Application.home());
        }

        Role role = Role.valueOf(session("role"));
        switch (role) {
            case TEACHER:
                return redirect(routes.TeacherSchools.homeTeacher());
            case ORGANIZER:
                return redirect(routes.Application.homeOrganizer());
            case STUDENT:
                return redirect(routes.Application.homeStudent());
            default:
                return badRequest(); // TODO: other roles
        }

    }

    /**
     * Home page for a user that is not yet logged in. Shows the contest which can be taken
     * by anonymous users.
     */
    @InjectContext
    public static Result home() {
        ContestDAO dao = DataAccess.getInjectedContext().getContestDAO();
        Iterable<ContestDAO.ContestAvailableLevels> available = dao.listAvailablePublicContests();
        return ok(home.render(available));
    }


    /**
     * Home page for an organizer.
     */
    public static Result homeOrganizer() {
        if (!CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }
        return ok (homeOrganizer.render());
    }


    /**
     * Home page for a student.
     */
    @InjectContext
    public static Result homeStudent() {
        if (!CurrentUser.hasRole(Role.STUDENT)) {
            return badRequest();
        }
        LocalContestDAO dao = DataAccess.getInjectedContext().getLocalContestDAO();
        Iterable<LocalContest> available = dao.listPermittedLocalContests();
        return ok (homeStudent.render(available));
    }
}
