/* Roles.java
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

package be.bebras.rasbeb.db.jdbc;

import be.bebras.rasbeb.db.data.Role;

import java.util.EnumMap;

/**
 * Translates roles to shorts as stored in the database (and conversely)
 */
public final class Roles {
    // class uses exactly the same structure as {@link Statuses} and {@link ContestTypes}

    // not using value() because this may change while the database does not
    // note that anonymous is 0
    private static final Role[] ROLES = {
            Role.ANONYMOUS, Role.ADMIN, Role.ORGANIZER, Role.TEACHER, Role.TEACHER_TO_BE, Role.STUDENT, Role.STUDENT_TO_BE
    };

    private static final EnumMap<Role, Integer> ROLE_MAP;

    static {
        ROLE_MAP = new EnumMap<>(Role.class);
        for (int index = 0; index < ROLES.length; index++) {
            ROLE_MAP.put(ROLES[index], index);
        }
    }

    public static int toInt(Role role) {
        return ROLE_MAP.get(role);
    }

    public static Role fromInt(int index) {
        return ROLES[index];
    }

}
