/* JDBCDataAccessProvider.java
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

import be.bebras.rasbeb.db.DataAccessContext;
import be.bebras.rasbeb.db.DataAccessProvider;
import be.bebras.rasbeb.db.dao.LanguageDAO;
import be.bebras.rasbeb.db.dao.LevelDAO;
import be.bebras.rasbeb.db.data.Role;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a data access provider based on JDBC.
 */
class JDBCDataAccessProvider implements DataAccessProvider {


    JDBCDataAccessProvider(boolean testDatabase, DataSource dataSource) {
        this.testDatabase = testDatabase;
        this.dataSource = dataSource;
    }

    private boolean testDatabase;

    private DataSource dataSource;

    /**
     * Is this a database used for testing? If so, some additional operations are allowed.
     */
    public boolean isTest() {
        return testDatabase;
    }


    @Override
    public DataAccessContext getDataAccessContext(int userId, String lang, Role role) {
        try {
            return new JDBCDataAccessContext(this, userId, lang, role, dataSource.getConnection());
        } catch (SQLException ex) {
            throw AbstractDAOImpl.convert( ex);
        }
    }

    @Override
    public void close() {
        // currently: do nothing
    }

    // singleton DAOs
    private LanguageDAO languageDAO;

    public synchronized LanguageDAO getLanguageDAO() {
        if (languageDAO == null) {
            try {
                languageDAO = new LanguageDAOImpl(dataSource.getConnection());
            } catch (SQLException ex) {
                throw AbstractDAOImpl.convert( ex);
            }
        }
        return languageDAO;
    }

    private Map<String,LevelDAO> levelDAOMap = new HashMap<>();

    public synchronized LevelDAO getLevelDAO(String lang) {
        LevelDAO levelDAO = levelDAOMap.get(lang);
         if (levelDAO == null) {
             try {
                 levelDAO = new LevelDAOImpl (dataSource.getConnection(), lang);
                 levelDAOMap.put (lang, levelDAO);
             } catch (SQLException ex) {
                 throw AbstractDAOImpl.convert(ex);
             }
         }
        return levelDAO;
    }
}
