/* Schools.java
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
import be.bebras.rasbeb.db.dao.SchoolDAO;
import be.bebras.rasbeb.db.data.School;
import bindings.Pager;
import bindings.SchoolsFilter;
import bindings.Sorter;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import static be.bebras.rasbeb.db.dao.SchoolDAO.Field.*;

/**
 * Controller for actions on schools.
 */
public class Schools extends Controller {

    /**
     * Gathers the arguments needed to show a schools data table. Used in {@link #list} but
     * also in
     */
    public static class ListArgs {
        public Iterable<School> list;
        public int count;
        public SchoolsFilter f;
        public Sorter s;
        public Pager p;
    }

    /**
     * Create a new {@link ListArgs} object filled from the current database context
     */
    public static ListArgs createListArgs (SchoolsFilter sf, Sorter sorter, Pager pager) {

        ListArgs args = new ListArgs();
        SchoolDAO dao = DataAccess.getInjectedContext().getSchoolDAO();
        Filter<SchoolDAO.Field> filter = dao.createListSchoolsFilter();
        filter.fieldContains(NAME, sf.name());
        filter.fieldContains(STREET, sf.street());
        filter.fieldStartsWith(ZIP, sf.zip());
        filter.fieldContains(TOWN, sf.town());

        args.list = dao.listSchoolsOfCurrentYear(filter, valueOf(sorter.sortColumn()), sorter.ascending(),
                pager.offset(), pager.limit());
        args.count = (int)dao.countSchoolsOfCurrentYear(filter);
        args.f = sf;
        args.p = pager;
        args.s = sorter;

        return args;
    }

    /**
     * Show a list of all schools
     */
    @InjectContext
    public static Result list(SchoolsFilter sf, Sorter sorter, Pager pager) {
        return ok(views.html.schools.list.render(createListArgs(sf, sorter, pager)));
    }

    /**
     * Show the details of a school (AJAX)
     */
    public static Result show (int id) {
        // TODO
        return ok(views.html.schools.details.render(id));
    }

    public static class FilterData {
        public String name;
        public String street;
        public String zip;
        public String town;

        public Integer select; // used in {@link TeacherSchools}
    }

    /**
     * Change the current filter (and reset paging)
     */
    public static Result filter (Sorter s, Pager p) {
        FilterData f = new Form<>(FilterData.class).bindFromRequest().get();
        return redirect(routes.Schools.list(new SchoolsFilter(f.name, f.street, f.zip, f.town), s, p.first()));
    }

    public static class Limit {

        public int limit;
    }

    /**
     *  Change the table size (and rerun query)
     */
    public static Result resize (SchoolsFilter f, Sorter s, int offset) {
        Form<Limit> form = new Form<>(Limit.class).bindFromRequest();
        int limit = form.hasErrors() ? 10 : form.get().limit;
        return redirect(routes.Schools.list(f, s, new Pager(offset,limit)));
    }


    public static Result showNew() {
        return TODO;
    }

}
