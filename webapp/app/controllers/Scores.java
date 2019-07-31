/* Scores.java
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

import be.bebras.rasbeb.db.dao.ParticipationDAO;
import be.bebras.rasbeb.db.data.Contest;
import be.bebras.rasbeb.db.data.ContestType;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import util.excel.SheetOfStudentMarks;

import java.util.List;
import java.util.Map;

/**
 * Displays the results of the contests
 */
@InjectContext
public class Scores extends Controller {

    /**
     * Displays the page were you can choose which results you want to see.
     */
    public static Result start() {

        // TODO: what if there are no contests to be shown?
        // TODO: also show official contests from previous years.
        return ok(views.html.results.start.render(
                new Form<>(Data.class),
                TeacherContests.getPreviewableContestLevels(),
                DataAccess.getInjectedContext().getTeacherSchoolClassDAO().listSchoolInfoShort(),
                null // new HashMap<String,List<ParticipationDAO.StudentMarks>>()
        ));
    }

    // TODO: common code with TeacherContests
    public static class Data {
        public String contest;
        public int schoolId;
        public String action;
    }

    public static Result show() {

        // TODO: this code is copied from TeacherContests.createNew
        Form<Data> form = new Form<>(Data.class).bindFromRequest();
        Data data = form.get();
        String[] parts = data.contest.split(":");
        if (parts.length != 2) {
            return badRequest();
        }

        int contest_id = Integer.parseInt (parts[0]);
        int level = Integer.parseInt (parts[1]);

        Contest contest = DataAccess.getInjectedContext().getContestDAO().getContest(contest_id);
        if (contest.getType() == ContestType.OFFICIAL && contest.getStatus() != be.bebras.rasbeb.db.data.Status.CLOSED) {
            return badRequest();
        }

        Map<String,List<ParticipationDAO.StudentMarks>> map
                = DataAccess.getInjectedContext().getParticipationDAO().listStudentMarksForSchool(contest_id, level, data.schoolId);

        if (data.action.equalsIgnoreCase("download")) {

            // TODO: factor out code in common with TeacherSchools#downloadStudents
            response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response().setHeader("Content-Disposition", "attachment; filename=scores.xlsx"); // TODO: better name
            response().setHeader(CACHE_CONTROL, "no-cache, must-revalidate");

            return  ok(SheetOfStudentMarks.getDownloadStream(map));
        }  else {

            return ok(views.html.results.start.render(
                    form,
                    TeacherContests.getPreviewableContestLevels(),
                    DataAccess.getInjectedContext().getTeacherSchoolClassDAO().listSchoolInfoShort(),
                    map
            ));
        }
    }
}
