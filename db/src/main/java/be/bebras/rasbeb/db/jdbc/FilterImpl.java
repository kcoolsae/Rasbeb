/* FilterImpl.java
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

import be.bebras.rasbeb.db.Filter;

import java.util.EnumMap;
import java.util.Map;

/**
 * Implementation of the {@link Filter} class for use with JDBC.
 */
public class FilterImpl<F extends Enum<F>> implements Filter<F> {

    private EnumMap<F, String> map;

    /**
     * Escape the given string and append it to the current builder.
     */
    private void appendEscapedString(StringBuilder builder, String string) {
        // see org.postgresql.core.Utils, but added escape for % and _

        // With standard_conforming_strings on, escape only single-quotes.
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            //noinspection StatementWithEmptyBody
            if (ch == '\0') {
                // silently ignore - although this should not happen
            } else if (ch == '\'' || ch == '%' || ch == '_')
                builder.append('\'');
            builder.append(ch);
        }
    }

    public FilterImpl(Class<F> clazz) {
        map = new EnumMap<>(clazz);
    }

    @Override
    public void fieldContains(F field, String string) {
        if (string != null) {
            string = string.trim();
            if (!string.isEmpty()) {
                StringBuilder builder = new StringBuilder(string.length() + 4);
                builder.append('%');
                appendEscapedString(builder, string);
                builder.append('%');
                map.put(field, builder.toString());
            }
        }
    }

    @Override
    public void fieldStartsWith(F field, String string) {
        if (string != null) {
            string = string.trim();
            if (!string.isEmpty()) {
                StringBuilder builder = new StringBuilder(string.length() + 4);
                appendEscapedString(builder, string);
                builder.append('%');
                map.put(field, builder.toString());
            }
        }
    }

    @Override
    public void fieldEndsWith(F field, String string) {
        if (string != null) {
            string = string.trim();
            if (!string.isEmpty()) {
                StringBuilder builder = new StringBuilder(string.length() + 4);
                builder.append('%');
                appendEscapedString(builder, string);
                map.put(field, builder.toString());
            }
        }
    }

    /**
     * Append to the given builder the 'ILIKE'-string that corresponds to this filter.
     */
    void appendILikeString(StringBuilder builder) {
        for (Map.Entry<F, String> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                builder.append(" AND ")
                        .append(entry.getKey().name())
                        .append(" ILIKE '")
                        .append(entry.getValue())
                        .append('\'');
            }
        }
    }
}
