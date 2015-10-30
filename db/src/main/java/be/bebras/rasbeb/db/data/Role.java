/* Role.java
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

package be.bebras.rasbeb.db.data;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static be.bebras.rasbeb.db.data.Privilege.*;

/**
 * Various user roles in the databases. Their meaning should be clear from their
 * names except possibly the following
 * <ul>
 *   <li>TEACHER_TO_BE: Registered as a teacher but not yet acknowledged by an organizer</li>
 *   <li>STUDENT_TO_BE: Registered as a student but not yet assigned to a class</li>
 * </ul>
 * Roles and sets of roles double as privileges.
 */
public enum Role {

    // roughly in order of strength (strongest first)
    ADMIN (UPDATE_CONFIG),
    ORGANIZER (UPDATE_QUESTION, UPDATE_CONTEST),
    TEACHER (),
    TEACHER_TO_BE (),
    STUDENT (),
    STUDENT_TO_BE (),
    ANONYMOUS  ();

    private Set<Privilege> privileges;

    Role(Privilege... privileges) {
        this.privileges = EnumSet.noneOf(Privilege.class);
        Collections.addAll(this.privileges, privileges);
    }

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

}
