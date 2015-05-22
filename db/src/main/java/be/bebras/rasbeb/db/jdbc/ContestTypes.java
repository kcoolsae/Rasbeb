/* ContestTypes.java
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

import be.bebras.rasbeb.db.data.ContestType;

import java.util.EnumMap;

/**
 * Translates contestTypes to shorts as stored in the database (and conversely)
 */
public final class ContestTypes {
    // class uses exactly the same structure as {@link Statuses} and {@link Roles}

    // not using value() because this may change while the database does not
    // note that anonymous is 0
    private static final ContestType[] CONTESTTYPES = {
        ContestType.PUBLIC, ContestType.RESTRICTED, ContestType.OFFICIAL
    };

    private static final EnumMap<ContestType, Integer> CONTESTTYPE_MAP;

    static {
        CONTESTTYPE_MAP = new EnumMap<>(ContestType.class);
        for (int index = 0; index < CONTESTTYPES.length; index++) {
            CONTESTTYPE_MAP.put(CONTESTTYPES[index], index);
        }
    }

    public static int toInt(ContestType contestType) {
        return CONTESTTYPE_MAP.get(contestType);
    }

    public static ContestType fromInt(int index) {
        return CONTESTTYPES[index];
    }

}
