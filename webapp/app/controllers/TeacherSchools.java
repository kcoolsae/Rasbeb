/* TeacherSchools.java
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
import be.bebras.rasbeb.db.dao.TeacherSchoolClassDAO;
import be.bebras.rasbeb.db.data.*;
import bindings.Pager;
import bindings.SchoolsFilter;
import bindings.Sorter;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.excel.SheetOfStudentInfo;
import util.excel.StudentInClassOrError;

import java.util.List;
import java.util.Map;

import views.html.teacherSchools.*;

/**
 * Teacher related actions
 */
public class TeacherSchools extends Controller {

    /**
     * Home page for a teacher. Shows related schools and classes
     */
    @InjectContext
    public static Result homeTeacher() {
        if (!CurrentUser.hasRole(Role.TEACHER)) {
            return badRequest();
        }

        TeacherSchoolClassDAO dao = DataAccess.getInjectedContext().getTeacherSchoolClassDAO();
        Iterable<TeacherSchoolClassDAO.TeacherSchoolInfo> list = dao.listSchoolInfo(DataAccess.getInjectedContext().getUserId());

        if (list.iterator().hasNext()) {
            return ok (homeTeacher.render(list));
        } else {
            flash().clear();
            flash ("warning", "warning.no.school");
            return redirect(routes.TeacherSchools.showWithDefaults());
        }
    }

    /**
     * Shows the page where the teacher can add a school.
     */
    @InjectContext
    public static Result show(SchoolsFilter sf, Sorter sorter, Pager pager) {
        if (!CurrentUser.hasRole(Role.TEACHER)) {
            return badRequest();
        }
        Iterable<School> list = DataAccess.getInjectedContext().getTeacherSchoolClassDAO().listSchools();
        return ok(show.render(Schools.createListArgs(sf, sorter, pager), list));
    }

    /**
     * Same as {@link #show} but with default arguments filled in.
     */
    @InjectContext
    public static Result showWithDefaults() {
        return show(new SchoolsFilter(null, null, null, null),
                new Sorter("NAME", true), new Pager(0, 10));
    }

    /**
     * Used when the list of schools is asked to be resized
     */
    @InjectContext
    public static Result resize(SchoolsFilter f, Sorter s, int offset) {
        Form<Schools.Limit> form = new Form<>(Schools.Limit.class).bindFromRequest();
        int limit = form.hasErrors() ? 10 : form.get().limit;
        return redirect(routes.TeacherSchools.show(f, s, new Pager(offset,limit)));
    }

    /**
     * Either perform a filter or add a school to a teacher
     */
    @InjectContext
    public static Result process(SchoolsFilter sf, Sorter s, Pager p) {
        Form<Schools.FilterData> form = new Form<>(Schools.FilterData.class).bindFromRequest();
        Schools.FilterData f = form.get();
        // System.err.printf("f.name=%s, f.zip=%s, f.select=%s\n", f.name, f.zip, f.select);
        if (f.select == null) {
            // apply filter
            return redirect(routes.TeacherSchools.show(new SchoolsFilter(f.name, f.street, f.zip, f.town), s, p.first()));
        } else {
            // add school
            // TODO: what if the school already exists?
            DataAccess.getInjectedContext().getTeacherSchoolClassDAO().addSchool(f.select);
            flash("info", "info.school.added");
            return redirect(routes.TeacherSchools.homeTeacher());
        }
    }

    /**
     * Add a class to the given school
     */
    @InjectContext
    public static Result addClasses(int schoolId) {

        String list = new DynamicForm().bindFromRequest().get("list");
        if (list != null && !list.trim().isEmpty()) {
            String[] classes = list.trim().split("\\s*,\\s*");      // whitespace surrounded comma's
            if (classes.length != 0 && !classes[0].trim().isEmpty()) {
                for (String cl : classes) {
                    // TODO: test that the combination belongs to the correct user
                    DataAccess.getInjectedContext().getTeacherSchoolClassDAO().createClassInSchool(schoolId, cl);
                }
            }
        }
        return redirect(routes.TeacherSchools.homeTeacher());
    }

        /**
         * Upload an excel file with students.
         */
    @InjectContext(inTransaction = false)
    public static Result uploadStudents(int id) {
        Http.MultipartFormData formData = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = formData.getFile("students");
        DataAccessContext context = DataAccess.getInjectedContext();
        if (part != null) {
            List<StudentInClassOrError> list=  SheetOfStudentInfo.read(part.getFile());
            TeacherSchool ts;
            int originalNumber = list.size();
            try {
                context.begin();

                TeacherSchoolClassDAO dao = context.getTeacherSchoolClassDAO();
                ts = dao.getTeacherSchool(id);
                Map<String, List<Student>> map = dao.listClassStudentsInSchool(ts.getSchoolId());
                SheetOfStudentInfo.checkList(list, map);

                context.commit();
            } finally {
                context.rollback();
            }

            int errorCount = SheetOfStudentInfo.countErrors(list);
            if (errorCount > 0) {
                 return ok(views.html.teacherSchools.notuploaded.render(list, ts));
            } else {
                int finalNumber = list.size();
                for (StudentInClassOrError data : list) {
                    if (! data.hasError()) {
                        try {
                            context.begin ();
                            SheetOfStudentInfo.registerStudent(context, ts.getSchoolId(), data);
                            context.commit();
                        } finally {
                            context.rollback();
                        }
                    }
                }
                // TODO: count and display errors produced by the above
                return ok(views.html.teacherSchools.uploaded.render(finalNumber, originalNumber - finalNumber, list, ts));
            }
        } else {
            flash ("error", "error.no.file.uploaded");
            return redirect (routes.TeacherSchools.homeTeacher());
        }
    }

    /**
     * Show a list of all students for a given class.
     */
    @InjectContext
    public static Result showStudents (int schoolId) {

        Map<String, List<Student>> map = DataAccess.getInjectedContext().getTeacherSchoolClassDAO().listClassStudentsInSchool(schoolId);
        School school = DataAccess.getInjectedContext().getSchoolDAO().getSchool(schoolId);
        return ok (views.html.teacherSchools.students.render(map, school));
    }

    /**
     * Download a list of students for all classes of a given school
     */
    @InjectContext
    public static Result downloadStudents (int schoolId) {
        TeacherSchoolClassDAO dao = DataAccess.getInjectedContext().getTeacherSchoolClassDAO();

        Map<String, List<Student>> map = dao.listClassStudentsInSchool(schoolId);
        response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response().setHeader("Content-Disposition", "attachment; filename=students.xlsx"); // TODO: better name (localized)
        response().setHeader(CACHE_CONTROL, "no-cache, must-revalidate");

        return  ok(SheetOfStudentInfo.getDownloadStream(map));

    }
}
