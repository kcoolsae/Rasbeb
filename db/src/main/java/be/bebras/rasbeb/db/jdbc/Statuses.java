/* Statuses.java
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

import be.bebras.rasbeb.db.data.Status;

import java.util.EnumMap;

/**
 * Translates statuses to shorts as stored in the database (and conversely)
 */
public final class Statuses {
    // class uses exactly the same structure as {@link Roles} and {@link ContestTypes}

    // TODO: factor out common code

    // not using value() because this may change while the database does not
    // note that anonymous is 0
    private static final Status[] STATUSES = {
            Status.DELETED, Status.DEFAULT, Status.ACTIVE, Status.CLOSED
    };

    private static final EnumMap<Status, Integer> STATUS_MAP;

    static {
        STATUS_MAP = new EnumMap<>(Status.class);
        for (int index = 0; index < STATUSES.length; index++) {
            STATUS_MAP.put(STATUSES[index], index);
        }
    }

    public static int toInt(Status status) {
        return STATUS_MAP.get(status);
    }

    public static Status fromInt(int index) {
        return STATUSES[index];
    }

}
