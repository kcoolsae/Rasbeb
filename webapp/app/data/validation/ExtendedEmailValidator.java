/* ExtendedEmailValidator.java
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

package data.validation;

import play.data.validation.Constraints.Validator;
import play.libs.F;

import javax.validation.ConstraintValidator;

/**
 * Validator for login addresses as used in the application. Simply needs at least one character before and after the @
 * and allows exactly one @. We expect the login server to do further validation whenever we send login.
 */

public class ExtendedEmailValidator extends Validator<String> implements ConstraintValidator<ExtendedEmail, String> {
    // based on http://stackoverflow.com/questions/8115106/how-to-create-a-custom-validator-in-play-framework-2-0
    // see also source of Constraints.EmailValidator

    /* Default error message */
    public static final  String message = "error.extendedemail";

    public boolean isValid(String string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        int pos = string.indexOf('@');
        return pos >= 1 && pos < string.length() - 1 && string.indexOf('@', pos + 1) < 0;

    }

    public F.Tuple<String, Object[]> getErrorMessageKey() {
        return F.Tuple(message, new Object[]{});
    }

    /**
     * Constructs a 'login' validator.
     */
    public static Validator<String> email() {
        return new ExtendedEmailValidator();
    }

    @Override
    public void initialize(ExtendedEmail constraintAnnotation) {
        // no initialization needed
    }
}
