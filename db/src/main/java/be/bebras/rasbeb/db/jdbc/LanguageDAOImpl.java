/* LanguageDAOImpl.java
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

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link LanguageDAO} using JDBC. Only one object of this class is
 * created. Data is cached.
 */
class LanguageDAOImpl implements LanguageDAO {

    private List<Language> list;

    private Map<String, Language> map;

    public LanguageDAOImpl(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT lang,description FROM i18n ORDER BY lang")) {
             list = new ArrayList<>();
            map = new HashMap<>();
            while (rs.next()) {
                Language language = new Language(rs.getString(1), rs.getString(2));
                list.add(language);
                map.put (language.getLang(), language);
            }
        } catch (SQLException e) {
            throw AbstractDAOImpl.convert (e);
        }

    }

    @Override
    public Language getLanguage(String lang) {
        Language result = map.get(lang);
        if (result == null) {
            throw new KeyNotFoundException("Unsupported language", lang);
        } else {
            return result;
        }
    }

    @Override
    public Iterable<Language> listLanguages() {
        return list;
    }
}
