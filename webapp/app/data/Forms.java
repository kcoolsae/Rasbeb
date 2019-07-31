/* Forms.java
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

package data;

import play.data.Form;
import play.data.validation.ValidationError;

import java.util.*;

/**
 * Helper functions for forms management.
 */
public final class Forms {


    /**
     * Adds an error to the form.
     */
    public static <T> void addError(Form<T> form, String key, String message, Object... args) {
         addError(form, new ValidationError(key, message, Arrays.asList(args)));
    }

    /**
     * Adds an error to the form.
     */
    public static <T> void  addError(Form<T> form, String key, String message) {
         addError(form, new ValidationError(key, message));
    }

    /**
     * Adds a global error to the form.
     */
    public static <T> void addGlobalError(Form<T> form, String message, Object... args) {
         addError(form, new ValidationError("", message, Arrays.asList(args)));
    }

    /**
     *  Adds a global error to the form.
     */
    public static <T> void addGlobalError(Form<T> form, String message) {
         addError(form, new ValidationError("", message));
    }

    /**
     * Adds an error to the form.
     */
    public static <T> void addError (Form<T> form, ValidationError error) {

        String key = error.key();
        Map<String,List<ValidationError>> errors = form.errors();
        List<ValidationError> list = errors.get(key);
        if (list == null) {
            list = new ArrayList<>();
            errors.put (key, list);
        }
        list.add(error);

    }

}
