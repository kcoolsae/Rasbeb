/* Contests.java
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
import be.bebras.rasbeb.db.Filter;
import be.bebras.rasbeb.db.dao.ContestDAO;
import be.bebras.rasbeb.db.dao.QuestionDAO;
import be.bebras.rasbeb.db.dao.QuestionSetDAO;
import be.bebras.rasbeb.db.data.*;
import bindings.Pager;
import bindings.QuestionsFilter;
import bindings.Sorter;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.html.contests.*;

import static be.bebras.rasbeb.db.dao.QuestionDAO.Field.EXTERNAL_ID;
import static be.bebras.rasbeb.db.dao.QuestionDAO.Field.TITLE;
import static be.bebras.rasbeb.db.dao.QuestionDAO.Field.valueOf;

/**
 * Controller for contest related stuff
 */
public class Contests extends Controller {

    public static class I18nData {
        @Constraints.Required
        public String title;
    }

    public static class Data {
        public String type;
        public Map<String, I18nData> i18n = new HashMap<>();
    }

    @InjectContext
    public static Result showNew() {
        Data data = new Data();
        data.type = ContestType.RESTRICTED.name();
        Form<Data> f = new Form<>(Data.class).fill(data);

        return ok(create.render(f));
    }

    @InjectContext
    public static Result createNew() {

        Form<Data> f = new Form<>(Data.class).bindFromRequest();
        if (f.hasErrors()) {
            return ok(views.html.contests.create.render(f));
        }

        Data data = f.get();
        ContestDAO dao = DataAccess.getInjectedContext().getContestDAO();
        int id = dao.createContest(ContestType.valueOf(data.type));

        for (be.bebras.rasbeb.db.data.Language l : DataAccess.getLanguages()) {
            String lang = l.getLang();
            I18nData i18n = data.i18n.get(lang);
            if (i18n != null) {
                dao.updateContestI18n(id, lang, i18n.title);
            }
        }

        return redirect(routes.Contests.showWithDefaults(id));

    }

    private static Form<Data> contestToForm(Contest contest, Iterable<ContestI18n> list) {
        Data data = new Data();
        data.type = contest.getType().name();
        for (ContestI18n q : list) {
            I18nData i = new I18nData();
            i.title = q.getTitle();
            data.i18n.put(q.getLang(), i);
        }
        return new Form<>(Data.class).fill(data);
    }


    /**
     * Shows a contest and gives an overview of the corresponding contest sets. Allows contests to
     * be added
     */
    @InjectContext
    public static Result show(int id, QuestionsFilter f, Sorter s, Pager p) {

        ContestDAO dao = DataAccess.getInjectedContext().getContestDAO();
        Contest contest = dao.getContest(id);
        Iterable<ContestI18n> list = dao.listContestI18n(id);

        // TODO: factor out code in common with ListQuestions
        QuestionDAO qdao = DataAccess.getInjectedContext().getQuestionDAO();
        Filter<QuestionDAO.Field> filter = qdao.createListQuestionsFilter();
        filter.fieldContains(EXTERNAL_ID, f.externalId());
        filter.fieldContains(TITLE, f.title());

        Iterable<Question> qlist = qdao.listQuestions(filter, valueOf(s.sortColumn()), s.ascending(),
                p.offset(), p.limit());
        int count = (int)qdao.countQuestions(filter);

        QuestionSetDAO qsdao = DataAccess.getInjectedContext().getQuestionSetDAO();
        Iterable<QuestionSetDAO.QuestionWithMarks> qslist = qsdao.listQuestionsWithMarks(id);


        return ok( views.html.contests.show.render(contest, contestToForm(contest, list),
                qlist, count, qslist,
                f, s, p) );
    }

    /**
     * Same as {@link #show} but with default arguments filled in.
     */
    @InjectContext
    public static Result showWithDefaults(int id) {
        return show(id, new QuestionsFilter(null, null), new Sorter("TITLE", true), new Pager(0, 10));
    }

    /**
     * Used when the list of questions is asked to be resized
     */
    @InjectContext
    public static Result resize(int id, QuestionsFilter f, Sorter s, int offset) {
        Form<ListQuestions.Limit> form = new Form<>(ListQuestions.Limit.class).bindFromRequest();
        int limit = form.hasErrors() ? 10 : form.get().limit;
        return redirect(routes.Contests.show(id, f, s, new Pager(offset, limit)).url() + "#list");
    }

    public static class FilterSpec {
        public String externalId;
        public String title;
        public Integer select;

    }

    /**
     * Either perform a filter or add a question to a question set
     */
    @InjectContext
    public static Result process (int id, QuestionsFilter filter, Sorter s, Pager p) {
        Form<FilterSpec> form = new Form<>(FilterSpec.class).bindFromRequest();
        FilterSpec f = form.get();
        if (f.select == null) {
            // apply filter
             return redirect(routes.Contests.show(id, new QuestionsFilter(f.externalId, f.title), s, p.first()));
        } else {

            // add question
            int qid = f.select;
            QuestionSetDAO dao = DataAccess.getInjectedContext().getQuestionSetDAO();
            dao.addQuestionInSet(id, qid);
            return redirect(routes.Contests.show(id, filter, s, p).url()+"#list");
        }
    }


    /**
     * Updates the general information for a contest
     */
    public static Result update(int id) {
        return TODO;
    }

    /**
     * Used in {@link #adjustMarks}
     */
    public static class MarksData {
        public Map<String,Marks> marks = new HashMap<>();

    }

    public static class Marks {
        public Integer correct;
        public Integer wrong;
    }


    /**
     * Adjust the marks for the question sets of the given contest
     */
    @InjectContext
    public static Result adjustMarks(int id) {

        // TODO: error messages for invalid marks (negative correct, positive wrong...)

        MarksData marksData = new Form<>(MarksData.class).bindFromRequest().get();

        QuestionSetDAO dao = DataAccess.getInjectedContext().getQuestionSetDAO();
        for (Map.Entry<String, Marks> entry : marksData.marks.entrySet()) {

            String key = entry.getKey();
            if (entry.getValue() != null) {

                int pos = key.indexOf(':');
                int index = Integer.parseInt(key.substring(0, pos));
                int level = Integer.parseInt(key.substring(pos + 1));
                level++; // do not forget this

                Marks pd = entry.getValue();

                int correct = pd.correct == null ? 0 : pd.correct;
                int wrong = pd.wrong == null ? 0 : pd.wrong;

                dao.updateQuestionInSet(id, level, index, correct, wrong);

            }
        }
        return redirect (routes.Contests.showWithDefaults(id));
    }

    /**
     * Show the page which allows rearranging of questions.
     */
    @InjectContext
    public static Result showForSorting (int id) {
        DataAccessContext context = DataAccess.getInjectedContext();
        Iterable<QuestionSetDAO.QuestionWithMarks> qlist = context.getQuestionSetDAO().listQuestionsWithMarks(id);
        List<String> slist = new ArrayList<>();
        int oldIndex = -1;
        for (QuestionSetDAO.QuestionWithMarks qwm : qlist) {
            int index = qwm.index;
            slist.add (oldIndex+":"+index);
            oldIndex = index;
        }
        slist.add(null);
        slist.set(0, null); // first one is invalid

        return ok(views.html.contests.showforsorting.render(
                context.getContestDAO().getContest(id),
                qlist, slist
        ));
    }

    public static class Swaps {
        public String swaps; // coded as index1:index2

        public int[] getIndices() {
            int pos = swaps.indexOf(':');
            int index1 = Integer.parseInt(swaps.substring(0, pos));
            int index2 = Integer.parseInt(swaps.substring(pos + 1));
            return new int[]{index1, index2};
        }
    }

    /**
     * Swap the position of the two given questions.
     */
    @InjectContext
    public static  Result swap (int id) {

        Swaps swapsData = new Form<>(Swaps.class).bindFromRequest().get();
        int[] indices = swapsData.getIndices();
        DataAccess.getInjectedContext().getQuestionSetDAO().swapQuestionInSet(id, indices[0],indices[1]);
        return redirect(routes.Contests.showForSorting (id));
    }
}
