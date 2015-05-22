/* QuestionsTable.java
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

import bindings.Pager;
import bindings.QuestionsFilter;
import bindings.Sorter;
import play.mvc.Call;

/**
 * Manages a question data table
 */
public class QuestionsTable extends AbstractTable<QuestionsFilter> {

    public QuestionsTable(QuestionsFilter f, Sorter s, Pager p) {
        super (f,s,p);
    }

    @Override
    protected Call list(QuestionsFilter questionsFilter, Sorter s, Pager p) {
        return routes.ListQuestions.list(f,s,p);
    }

    @Override
    public Call resize() {
       return routes.ListQuestions.resize(f,s, p.offset());
    }

    @Override
    public Call action() {
        return routes.ListQuestions.filter(s,p);
    }
}
