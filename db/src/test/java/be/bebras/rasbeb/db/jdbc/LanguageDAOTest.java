/* LanguageDAOTest.java
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

import be.bebras.rasbeb.db.KeyNotFoundException;
import be.bebras.rasbeb.db.dao.LanguageDAO;
import be.bebras.rasbeb.db.data.Language;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
  * Test for the JDBC implementation of {@link LanguageDAO}
  */
public class LanguageDAOTest extends DAOTest {

    private LanguageDAO dao;

    @Before
    public void getDAO () {
        dao = context.getLanguageDAO();
    }

    @Test
    public void thereIsAtLeastOneLanguage () {
        Iterator<Language> iterator = dao.listLanguages().iterator();
        assertEquals (true, iterator.hasNext());
        Language language = iterator.next();
        dao.getLanguage(language.getLang()); // should not yield an exception
    }

    @Test(expected = KeyNotFoundException.class)
    public void unknownLanguage () {
        dao.getLanguage("??");
    }

}
