/* Questions.java
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

import be.bebras.rasbeb.db.dao.QuestionDAO;
import be.bebras.rasbeb.db.data.Language;
import be.bebras.rasbeb.db.data.Question;
import be.bebras.rasbeb.db.data.QuestionI18n;
import be.bebras.rasbeb.db.data.Role;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.URLs;
import util.Uploader;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import views.html.questions.*;

/**
 * Controller for actions on questions
 */
public class Questions extends Controller {

    /**
     * Data for {@link #updateExternalId}
     */
    public static class EIData {
        public String externalId;
    }

    /**
     * Update the external id for a question
     */
    @InjectContext
    public static Result updateExternalId(int id) {
        if (! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }
        QuestionDAO dao = DataAccess.getInjectedContext().getQuestionDAO();

        Form<EIData> f = new Form<>(EIData.class).bindFromRequest();

        if (!f.hasErrors()) {
            dao.updateExternalId(id, f.get().externalId);
        }
        return redirect(routes.Questions.preview(id));
    }

    /**
     * Data for {@link #updateNumberOfAnswers}
     */
    public static class NAData {
        public int numberOfAnswers;
    }

    /**
     * Update the number of answers for a question
     */
    @InjectContext
    public static Result updateNumberOfAnswers (int id) {
        if (! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }
        QuestionDAO dao = DataAccess.getInjectedContext().getQuestionDAO();

        Form<NAData> f = new Form<>(NAData.class).bindFromRequest();

        if (!f.hasErrors()) {
            dao.updateNumberOfAnswers(id, f.get().numberOfAnswers);
        }
        return redirect(routes.Questions.preview(id));
    }

    private static Map<String, Form<I18nData>> i18nListToMap(Iterable<QuestionI18n> list) {
        Map<String, Form<I18nData>> map = new HashMap<>();
        for (QuestionI18n q : list) {
            I18nData data = new I18nData();
            data.title = q.getTitle();
            data.correctAnswer = q.getCorrectAnswer();
            map.put(q.getLang(), new Form<>(I18nData.class).fill(data));
        }
        return map;
    }

    /**
     * Handle file upload for both questions and feedback.
     *
     * @param dao        The question dao which can be used to update the database (uploadedF and uploadedQ fields)
     * @param question   The corresponding question
     * @param lang       The corresponding language
     * @param fieldNameQ The name of the form field for the questions file
     * @param fieldNameF The name of the form field for the feedback file
     * @return whether no errors were encountered during upload
     */
    private static boolean uploadQuestionsAndFeedback(QuestionDAO dao, Question question, String lang, String fieldNameQ, String fieldNameF) {
        boolean uploadOK = true;
        int id = question.getId();

        Http.MultipartFormData formData = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = formData.getFile(fieldNameQ);
        if (part != null) {
            if (Uploader.upload(part.getFile(), question.getMagicQ(), lang, id)) {
                dao.setQuestionUploaded(id, lang);
            } else {
                uploadOK = false;
            }
        }
        part = formData.getFile(fieldNameF);
        if (part != null) {
            if (Uploader.upload(part.getFile(), question.getMagicF(), lang, id)) {
                dao.setFeedbackUploaded(id, lang);
            } else {
                uploadOK = false;
            }
        }
        return uploadOK;
    }

    /**
     * Transfer object for {@link #preview}
     */
    public static class PreviewArg {
        public Question question;
        public Form<NAData> fna;
        public Form<EIData> fei;
        public Map<String,Form<Questions.I18nData>> g;
        public Iterable<QuestionI18n> list;

        public String questionBaseURL;
        public String feedbackBaseURL;
    }

    /**
     * Allows a preview of the question and feedback pages, in all available languages.
     * Also allows editing of some of the corresponding information
     */
    @InjectContext
    public static Result preview(int id) {
        if (! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }
        // TODO: the above is possibly too restrictive
        // Teachers can only see questions that have been assigned to (public) competitions.

        QuestionDAO dao = DataAccess.getInjectedContext().getQuestionDAO();
        PreviewArg arg = new PreviewArg();

        arg.question = dao.getQuestion(id);
        arg.list = dao.listQuestionI18n(id);

        NAData na = new NAData();
        na.numberOfAnswers = arg.question.getNumberOfAnswers();
        arg.fna = new Form<>(NAData.class).fill (na);

        EIData ei = new EIData();
        ei.externalId = arg.question.getExternalId();
        arg.fei = new Form<>(EIData.class).fill (ei);

        arg.g = i18nListToMap(arg.list);

        arg.questionBaseURL = URLs.getQuestionBaseURL(arg.question);
        arg.feedbackBaseURL = URLs.getFeedbackBaseURL(arg.question);

        return ok(preview.render(arg));
    }

    /**
     * Language dependent data for question forms
     */
    public static class I18nData {

        @Constraints.Required
        public String title;

        public String correctAnswer;
    }


    @InjectContext
    public static Result updateI18n(int id, String lang) {
        if (! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }

        // handle normal fields
        Form<I18nData> f = new Form<>(I18nData.class).bindFromRequest();
        if (f.hasErrors()) {
            return TODO;
        }

        I18nData data = f.get();
        QuestionDAO dao = DataAccess.getInjectedContext().getQuestionDAO();
        Question question = dao.getQuestion(id);
        dao.updateQuestionI18n(id, lang, data.title, data.correctAnswer);

        // handle file uploads
        if (!uploadQuestionsAndFeedback(dao, question, lang, "question", "feedback")) {
            flash("error.question.upload");
        }
        return redirect(routes.Questions.preview(id));
    }

    /**
     * Data for {@link #createNew} forms
     */
    public static class Data {

        public String externalId;
        public int numberOfAnswers;

        // languages mapped to various fields

        @Valid // makes sure that the values in the map are also validated
        public Map<String, I18nData> i18n = new HashMap<>();

        // TODO: add validation

    }

    /**
     * Displays the form used to add a new question
     */
    @InjectContext
    public static Result showNew() {
        if (! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }

        // note: there is no use doing this in the constructor...
        Data initial = new Data();
        initial.numberOfAnswers = 4;

        return ok(create.render(new Form<>(Data.class).fill(initial)));
    }

    /**
     * Add a new question to the database
     */
    @InjectContext
    public static Result createNew() {
        if (! CurrentUser.hasRole(Role.ORGANIZER)) {
            return badRequest();
        }

        Form<Data> f = new Form<>(Data.class).bindFromRequest();
        if (f.hasErrors()) {
            return ok(create.render(f));
        }
        Data data = f.get();

        // handle non-internationalized fields
        QuestionDAO dao = DataAccess.getInjectedContext().getQuestionDAO();
        int id = dao.createQuestion(data.numberOfAnswers, data.externalId);
        Question question = dao.getQuestion(id);

        // handle internationalized fields
        for (Language l : DataAccess.getLanguages()) {
            String lang = l.getLang();
            I18nData idata = data.i18n.get(lang);
            if (idata != null) {
                if (idata.title != null) {
                    dao.updateQuestionI18n(id, lang, idata.title, idata.correctAnswer);
                }
            }
        }

        // handle file upload
        boolean uploadErrors = false;
        for (Language l : DataAccess.getLanguages()) {
            String lang = l.getLang();
            if (!uploadQuestionsAndFeedback(dao, question, lang, "i18n[" + lang + "].question", "i18n[" + lang + "].feedback")) {
                uploadErrors = true;
            }
        }
        if (uploadErrors) {
            flash("error.question.upload");
        }
        return redirect(routes.Questions.preview(id));
    }


}
