/* Reset.java
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
import be.bebras.rasbeb.db.data.Role;
import be.bebras.rasbeb.db.data.User;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import data.validation.ExtendedEmail;
import db.DataAccess;
import db.InjectContext;
import play.Play;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.reset.*;

/**
 * Reset password of existing users.
 */
public class Reset extends Controller {

    // TODO: extract code in common with Registration

    /**
     * Show the 'reset password' page, (Step 1)
     */
    public static Result start() {
        return ok(start.render(new Form<>(Registration.EmailData.class)));
    }

    /**
     * Sends an login to the user that lost his password
     */
    @InjectContext
    public static Result sendToken() {

        // minimal check for a valid login address
        Form<Registration.EmailData> f = new Form<>(Registration.EmailData.class).bindFromRequest();
        if (f.hasErrors()) {
            return ok(start.render(f)); // let the user try again
        }

        Registration.EmailData emailData = f.get();
        String address = emailData.computeAddress();
        MailerAPI mailer = Play.application().plugin(MailerPlugin.class).email();
        mailer.setRecipient(address);
        mailer.setFrom(Messages.get("mail.noreply.address")); // in configuration?
        mailer.setCharset("UTF-8");

        // only send a mail if the user actually exists. We could also warn the owner
        // of the login address, but we do not want to be too intrusive
        User user = DataAccess.getInjectedContext().getUserDAO().findUserByEmail(emailData.email);
        if (user != null) {
            String token = DataAccess.getInjectedContext().getActivationDAO().createToken(emailData.email, Role.ANONYMOUS, false, false);

            mailer.setSubject(Messages.get("mail.reset.send.token"));

            String baseURL = request().getHeader("Referer");
            int pos = baseURL.indexOf("/re"); // same for both /reset and /register ?!
            baseURL = baseURL.substring(0, pos);
            mailer.send(views.txt.reset.mailToken.render(baseURL, token).body().trim());
        }

        return ok(tokenSent.render(address));
    }

    public static class NewPassword {

        @ExtendedEmail
        public String email;

        @Constraints.Required
        public String password;

        @Constraints.Required
        public String repeated;

        public String validate() {
            if (password.equals(repeated)) {
                return null;
            } else {
                return "error.passwords.differ";
            }
        }
    }

    /**
     * Shows the form for choosing a new password
     */
    public static Result next(String token) {
        return ok(next.render(new Form<>(NewPassword.class), token));
    }

    @InjectContext
    public static Result newPassword(String token) {

        Form<NewPassword> f = new Form<>(NewPassword.class).bindFromRequest();
        if (f.hasErrors()) {
            return ok(next.render(f, token)); // let the user try again
        }
        NewPassword np = f.get();

        // check whether token is (still) valid
        DataAccessContext context = DataAccess.getInjectedContext();
        Role role = context.getActivationDAO().checkToken(np.email, token);

        if (role == null) {
            flash("error", "error.reset.token");
        } else {
            User user = context.getUserDAO().findUserByEmail(np.email);
            context.getUserDAO().setPassword(user.getId(), np.password);
            context.getActivationDAO().deleteToken(np.email);
            flash("info", "success.changepass");
        }
        return redirect(routes.Authentication.loginForm());
    }
}
