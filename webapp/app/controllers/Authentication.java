/* Authentication.java
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
import be.bebras.rasbeb.db.dao.UserDAO;
import be.bebras.rasbeb.db.data.Role;
import be.bebras.rasbeb.db.data.User;
import data.Forms;
import data.validation.ExtendedEmail;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.init.*;
import views.html.profile.changepass;

import java.util.Date;

/**
 * Controls various aspects of authentication
 */
public class Authentication extends Controller {


    public static class UserData {

        @Required
        public String name;
        @ExtendedEmail
        public String email;
        @Required
        public String password;
        @Required
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
     * Shows the page which allows a first administrator to be setup
     */
    @InjectContext
    public static Result firstAdministratorForm() {
        if (DataAccess.getInjectedContext().getUserDAO().hasAdministrator()) {
            flash().clear(); // might persist
            flash("error", "error.first");
            return redirect(routes.Application.index());
        } else {
            return ok(first.render(new Form<>(UserData.class)));
        }
    }

    /**
     * Creates the first administrator account
     */
    @InjectContext
    public static Result firstAdministratorCreate() {
        DataAccessContext context = DataAccess.getInjectedContext();
        if (context.getUserDAO().hasAdministrator()) {
            return badRequest();
        } else {

            Form<UserData> f = new Form<>(UserData.class).bindFromRequest();

            if (f.hasErrors()) {
                return ok(first.render(f)); // let the user try again
            }

            UserData ui = f.get();
            int id = context.getUserDAO().createUser(ui.email, Role.ADMIN, ui.name);
            context.getUserDAO().setPassword(id, ui.password);
            flash("info", "success.first");
            return redirect(routes.Authentication.loginForm());
        }
    }

    public static class LoginData {
        @Required
        public String login;
        @Required
        public String password;
    }

    /**
     * Shows the login form.
     */
    public static Result loginForm() {
        return ok(login.render(new Form<>(LoginData.class)));
    }


    @InjectContext
    public static Result login() {
        Form<LoginData> f = new Form<>(LoginData.class).bindFromRequest();
        if (!f.hasErrors()) {

            LoginData sl = f.get();
            User user = DataAccess.getInjectedContext().getUserDAO().getUser(sl.login, sl.password);
            if (user != null) {
                setUserSession(user);
                return redirect (routes.Application.index());
            }
            // add global error
            Forms.addGlobalError(f, "error.login");
        }

        // if this point is reached then there was some error
        return ok(login.render(f));
    }

    private static void setUserSession(User user) {
        session("id", Integer.toString(user.getId()));
        session("role", user.getRole().name());
        session("bebrasId", user.getBebrasId());
        session("stamp", Long.toHexString(new Date().getTime()));
        //
        // added in 2018, to avoid pupils that log in ending up in an (anonymous) participation that was
        // started on the same computer but was not closed, We hope this has no adverse effects
        session().remove("part");
        session().remove("feedback");
    }

    public static Result logout() {
        session().clear();
        flash("info", "success.logout");
        return  redirect (routes.Authentication.loginForm());
    }
    
    public static class PasswordData {
        @Required
        public String oldpass;
        
        @Required
        public String password;
        
        @Required
        public String repeated;

        public String validate() {
            if (password.equals(repeated)) {
                return null;
            } else {
                return "error.passwords.differ";
            }
        }
    }
    
    public static Result changePasswordForm() {
        return ok(changepass.render(new Form<>(PasswordData.class)));
    }

    @InjectContext
    public static Result changePassword() {

        Form<PasswordData> f = new Form<>(PasswordData.class).bindFromRequest();
        if (! f.hasErrors()) {
            PasswordData cp = f.get();
            UserDAO dao = DataAccess.getInjectedContext().getUserDAO();
            if (!dao.isPassword(cp.oldpass)) {
                Forms.addError(f, "oldpass", "error.password");
            } else {
                dao.setPassword(cp.password);
                flash("info", "success.changepass");
                return redirect (routes.Application.index());
            }
        }

        // if this point is reached then there was some error
        return ok(changepass.render(f));
    }
    

    /**
     * Switch to another user.
     */
    @InjectContext
    public static Result switchTo(int userId) {
        if (CurrentUser.hasRole(Role.ORGANIZER)) {
            User user = DataAccess.getInjectedContext().getUserDAO().getUser(userId);
            setUserSession(user);
            return redirect(routes.Application.index());
        }  else {
            return badRequest();
        }
    }
}
