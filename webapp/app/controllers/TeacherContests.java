/* TeacherContests.java
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

package controllers;

import be.bebras.rasbeb.db.DataAccessContext;
import be.bebras.rasbeb.db.dao.ContestDAO;
import be.bebras.rasbeb.db.dao.LocalContestDAO;
import be.bebras.rasbeb.db.dao.TeacherSchoolClassDAO;
import be.bebras.rasbeb.db.data.ContestType;
import be.bebras.rasbeb.db.data.LCStatus;
import be.bebras.rasbeb.db.data.Level;
import be.bebras.rasbeb.db.data.LocalContest;
import be.bebras.rasbeb.db.jdbc.Statuses;
import db.DataAccess;
import db.InjectContext;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.teacherContests.*;

import javax.validation.Constraint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages contests for teachers.
 */
public class TeacherContests extends Controller {

    public static class ListInfo {
        public String name; // name of a school
        public Iterable<LocalContest> lcs; // local contests for that school
    }

    /**
     * Lists the non-public contests and levels that are available for use.
     */
    @InjectContext
    public static Result list() {
        LocalContestDAO dao = DataAccess.getInjectedContext().getLocalContestDAO();
        Iterable<TeacherSchoolClassDAO.TeacherSchoolInfo> infos = DataAccess.getInjectedContext().getTeacherSchoolClassDAO().listSchoolInfoShort();
        List<ListInfo> result = new ArrayList<>();
        // TODO: one database operation?
        for (TeacherSchoolClassDAO.TeacherSchoolInfo info : infos) {
            ListInfo li = new ListInfo();
            li.lcs = dao.listLocalContestsInSchool(info.schoolId);
            li.name = info.name;
            result.add(li);
        }
        return ok(list.render(
                result,
                DataAccess.getInjectedContext().getContestDAO().listPreviewableContests()
                )
        );
    }

    /**
     * Combines contest and level. Used in {@link #showNew}
     */
    public static class ContestLevel {
        public String title;
        public String levelName;
        public int contestId;
        public int levelId;
    }

    public static Iterable<ContestLevel> getOrganizableContestLevels() {
        return getLevelsAux(DataAccess.getInjectedContext().getContestDAO().listOrganizableContests());
    }

    public static Iterable<ContestLevel> getPreviewableContestLevels() {
        return getLevelsAux(DataAccess.getInjectedContext().getContestDAO().listPreviewableContests());
    }

    private static Iterable<ContestLevel> getLevelsAux(Iterable<ContestDAO.ContestAvailableLevels> list) {
        ArrayList<ContestLevel> result = new ArrayList<>();
        for (ContestDAO.ContestAvailableLevels a : list) {
            for (Level level : DataAccess.getLevels()) {
                int index = level.getLevel() - 1;
                if (index < a.available.size() && a.available.get(index)) {
                    ContestLevel cl = new ContestLevel();
                    cl.title = a.title;
                    cl.levelName = level.getName();
                    cl.contestId = a.contestId;
                    cl.levelId = level.getLevel();
                    result.add(cl);
                }
            }
        }
        return result;

    }

    /**
     * Contains some information about a particular contest and the levels it supports. Can be
     * used in a two step selection. (Contest - Level)
     */
    public static class ContestInfo {
        public String title;
        public int contestId;
        public String levels; // of the form "+-++", indexed by levelId-1

        public ContestInfo(ContestDAO.ContestAvailableLevels cals) {
            this.title = cals.title;
            this.contestId = cals.contestId;

            levels = "";
            for (Boolean available : cals.available) {
                levels += available ? "+" : "-";
            }
        }
    }

    private static Result showNewAux(Form<Data> form) {

        List<ContestInfo> list = new ArrayList<>();
        for (ContestDAO.ContestAvailableLevels cals : DataAccess.getInjectedContext().getContestDAO().listOrganizableContests()) {
            list.add(new ContestInfo(cals));
        }

        return ok(create.render(form,
                list,
                DataAccess.getInjectedContext().getTeacherSchoolClassDAO().listSchoolInfoShort()
        ));
    }

    /**
     * Show page that allows creation of a local competition
     */
    @InjectContext
    public static Result showNew() {
        return showNewAux(new Form<>(Data.class));
    }

    /**
     * Data to be used to create a new local competition
     */
    public static class Data {
        // constraint does not work on primitive type?
        @Constraints.Required
        public int contestId;

        // constraint does not work on primitive type?
        @Constraints.Required
        public int level;


        @Constraints.Required
        public String comment;
        public int schoolId;
    }

    /**
     * Create a local competition
     */
    @InjectContext
    public static Result createNew() {
        Form<Data> form = new Form<>(Data.class).bindFromRequest();
        if (form.hasErrors()) {
            return showNewAux(form);
        }
        Data data = form.get();
        if (data.contestId == 0) { // standard validation does not work?
            form.reject("contestId", "error.chooseone");
            return showNewAux(form);
        } else if (data.level == 0) { // standard validation does not work?
            form.reject("level", "error.chooseone");
            return showNewAux(form);
        } else {
            DataAccessContext context = DataAccess.getInjectedContext();
            context.getLocalContestDAO().createLocalContestInSchool(
                    data.contestId, data.level, context.getLang(), data.schoolId, data.comment
            );
            return redirect(routes.TeacherContests.list());
        }
    }

    /**
     * Shows the page which allows  permissions to be granted to students.
     */
    @InjectContext
    public static Result start(int lcId) {
        LocalContestDAO dao = DataAccess.getInjectedContext().getLocalContestDAO();
        LocalContest localContest = dao.getLocalContest(lcId);
        Map<String, List<LocalContestDAO.StudentPermission>> map = dao.listPermissions(lcId);
        return ok(lc.render(localContest, map));
    }

    public static class ManageData {
        public Map<Integer, Integer> boxes = new HashMap<>(); // obligatory!
        public String action;
    }

    /**
     * Manage permissions to the selected students
     */
    @InjectContext
    public static Result manage(int lcId) {
        ManageData data = new Form<>(ManageData.class).bindFromRequest().get();
        LocalContestDAO dao = DataAccess.getInjectedContext().getLocalContestDAO();

        if (data.action.equalsIgnoreCase("grant")) {
            for (Integer studentId : data.boxes.keySet()) {
                dao.grantPermission(lcId, studentId);  // TODO: can be done twice
            }
        } else if (data.action.equalsIgnoreCase("revoke")) {
            for (Integer studentId : data.boxes.keySet()) {
                dao.removePermission(lcId, studentId);
            }
        } else {
            return badRequest();
        }
        return redirect(routes.TeacherContests.start(lcId));
    }

    /**
     * Set/revoke permissions for all pupils in a given class.
     */
    @InjectContext
    public static Result grouped(int lcId, String classId) {
        String action = new DynamicForm().bindFromRequest().get("action");
        LocalContestDAO dao = DataAccess.getInjectedContext().getLocalContestDAO();
        if (action.equalsIgnoreCase("grant")) {
            dao.grantPermissionToClass(lcId, classId);
        } else if (action.equalsIgnoreCase("revoke")) {
            dao.removePermissionFromClass(lcId, classId);
        } else {
            return badRequest();
        }
        return redirect(routes.TeacherContests.start(lcId));
    }

    @InjectContext
    public static Result changeStatus(int lcId, int s) {
        LocalContestDAO dao = DataAccess.getInjectedContext().getLocalContestDAO();
        be.bebras.rasbeb.db.data.Status status = Statuses.fromInt(s);
        LocalContest localContest = dao.getLocalContest(lcId);
        switch (status) {
            case ACTIVE:
                if (localContest.getStatus() == LCStatus.CAN_BE_OPENED) {
                    dao.updateStatus(lcId, status);
                    return redirect(routes.TeacherContests.start(lcId));
                }
                break;
            case CLOSED:
                dao.updateStatus(lcId, status);
                return redirect(routes.TeacherContests.start(lcId));
            // TODO: remaining cases
            default:
        }
        return badRequest();
    }

}

