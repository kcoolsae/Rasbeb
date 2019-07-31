/* PasswordGenerator.java
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

package util;

import java.util.Random;

/**
 * Creates random 'pronounceable' passwords.
 */
public final class PasswordGenerator {

    private static final String[] VOWELS = {
            "a", "e", "o", "ou", "i"
    };
    private static final String[] CONSONANTS = {
            "b", "bl", "br", "c", "d", "dr", "f", "fl", "fr", "g", "gr",
            "l", "m", "n", "p", "pl", "pr", "r", "s", "st", "t", "tr", "v", "vr", "z"
    };
    private static final String[] TERMINALS = {
            "l", "s", "t", "x", "n", "f", "st", "ns", "nt"
    };

    private static final Random RG = new Random();

    /**
     * Generates a random 'pronounceable' password
     */
    public static String generate() {
        StringBuilder sb = new StringBuilder(10);

        if (RG.nextInt(3) == 0) {
            sb.append(VOWELS[RG.nextInt(VOWELS.length)]);
        }
        for (int i = 0; i < 3; i++) {
            sb.append(CONSONANTS[RG.nextInt(CONSONANTS.length)]);
            sb.append(VOWELS[RG.nextInt(VOWELS.length)]);
        }

        if (RG.nextInt(3) != 0) {
            sb.append(TERMINALS[RG.nextInt(TERMINALS.length)]);
        }
        return sb.toString();
    }

}
