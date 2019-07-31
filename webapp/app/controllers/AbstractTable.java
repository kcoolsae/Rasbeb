/* AbstractTable.java
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

package controllers;

import bindings.Pager;
import bindings.Sorter;
import play.mvc.Call;

public abstract class AbstractTable<F> extends Table {

    protected F f;

    protected Sorter s;

    protected Pager p;

    protected AbstractTable(F f, Sorter s, Pager p) {
        this.f = f;
        this.s = s;
        this.p = p;
    }

    /*
     * Returns a route to a 'list'-method. This method is used
     * in default implementations of {@link #previous}, {{@link #next} and
     * {@link #sort}.
     */
    protected abstract Call list(F f, Sorter s, Pager p);

    // TODO: do resize through list?

    @Override
    public Call previous() {
        return list(f,s,p.previous());
    }

    @Override
    public Call next() {
        return list(f,s,p.next());
    }

    @Override
    public Call sort(String field) {
        return list(f,s.forColumn(field),p);
    }

    @Override
    public Pager pager() {
        return p;
    }

    @Override
    public Sorter sorter() {
        return s;
    }

    /**
     * Return the filter for this table
     */
    public F filter() {
        return f;
    }
}
