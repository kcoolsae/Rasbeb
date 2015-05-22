/* Registration.java
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
import be.bebras.rasbeb.db.data.Activation;
import be.bebras.rasbeb.db.data.Role;
import be.bebras.rasbeb.db.data.User;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import data.validation.ExtendedEmail;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import views.html.registration.*;

/**
 * Registration of new users
 */
public class Registration extends Controller {

    public static class EmailData {
        @ExtendedEmail
        public String email;

        public String computeAddress() {
            int pos = email.indexOf('@');
            pos = email.indexOf('#', pos + 1);
            if (pos >= 0) {
                return email.substring(0, pos);
            } else {
                return email;
            }
        }
    }

    /**
     * Check whether the current user is allowed to register another user
     */
    private static boolean userCanRegister () {
        // TODO: use injection for this type of checks (and Permission enum)
        String idString = session("id");
        if (idString == null) {
            return false;
        }
        Role role = Role.valueOf(session("role"));

        return role == Role.TEACHER || role == Role.ORGANIZER;
    }

    /**
     * Shows the page where the email address for registration of a new teacher  can be entered.
     */
    public static Result start() {

        if (userCanRegister()) {
            return ok(start.render(new Form<>(EmailData.class)));
        } else {
            return badRequest();
        }
    }

    /**
     * Sends an login to the user that wants to be registered
     */
    @InjectContext
    public static Result sendToken() {

        // minimal check for a valid login address
        Form<EmailData> f = new Form<>(EmailData.class).bindFromRequest();
        if (f.hasErrors()) {
            return ok(start.render(f));  // TODO: or list.render(...) when organizer?
        }
        EmailData emailData = f.get();
        String address = emailData.computeAddress();
        MailerAPI mailer = Play.application().plugin(MailerPlugin.class).email();
        mailer.setRecipient(address);
        mailer.setFrom(Messages.get("mail.noreply.address")); // in configuration?
        mailer.setCharset("UTF-8");

        // if a user with this login address already exists, then warn him by login
        User user = DataAccess.getInjectedContext().getUserDAO().findUserByEmail(emailData.email);
        if (user != null) {
            mailer.setSubject(Messages.get("mail.registration.user.exists"));
            mailer.send(views.txt.registration.mailUserExists.render(request().remoteAddress()).body().trim());

            // TODO: refactor
        } else {
            String token = DataAccess.getInjectedContext().getActivationDAO().createToken(emailData.email, Role.TEACHER, true, true);

            mailer.setSubject(Messages.get("mail.registration.send.token"));

            String baseURL = request().getHeader("Referer");
            int pos = baseURL.indexOf("/re"); // same for both /reset and /register ?!
            baseURL = baseURL.substring(0, pos);
            mailer.send(views.txt.registration.mailToken.render(baseURL, token).body().trim());
        }

        flash("info", "success.token.sent");
        if (CurrentUser.hasRole(Role.ORGANIZER)) {
            return redirect(routes.Registration.list());
        } else {
            return redirect(routes.Application.index());
        }
    }


    /**
     * Shows the registration form for the given token
     */
    public static Result next(String token) {
        Http.Cookie cookie = request().cookie(Play.langCookieName());
        if (cookie == null) {
            // shows language choice page
            return ok(language.render(token));
        } else {
            return ok(next.render(new Form<>(Authentication.UserData.class), token));
        }
    }

    /**
     * Choose language and proceed to registration page
     */
    public static Result chooseLanguage(String lang, String token) {
        response().setCookie(Play.langCookieName(), lang, 365 * 24 * 3600);
        // must be redirect for the cookie to take effect:
        return redirect(routes.Registration.next(token));
    }

    /**
     * Final part of the registration: creates the user
     */
    @InjectContext
    public static Result createTeacher(String token) {

        Form<Authentication.UserData> f = new Form<>(Authentication.UserData.class).bindFromRequest();

        if (f.hasErrors()) {
            return ok(next.render(f, token)); // let the user try again
        }

        Authentication.UserData ui = f.get();

        // check whether token is (still) valid
        DataAccessContext context = DataAccess.getInjectedContext();
        Role role = context.getActivationDAO().checkToken(ui.email, token);
        if (role == null) {
            flash("error", "error.registration.token");
        } else {
            int id = context.getUserDAO().createUser(ui.email, role, ui.name);
            context.getUserDAO().setPassword(id, ui.password);
            if (role == Role.STUDENT_TO_BE)
                flash("info", "success.registration.student");
            else
                flash("info", "success.registration.teacher");
        }
        return redirect(routes.Authentication.loginForm());
    }

    /**
     * List the pending registrations. Allow new registrations.
     */
    @InjectContext
    public static Result list () {
        // TODO: also allow deletion of old registrations

        Iterable<Activation> activations = DataAccess.getInjectedContext().getActivationDAO().listPendingRegistrations();

        if (userCanRegister()) {
            return ok(list.render(new Form<>(EmailData.class), activations));
        } else {
            return badRequest();
        }
    }


}
