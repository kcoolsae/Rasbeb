/* DataAccess.java
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

package db;

import be.bebras.rasbeb.db.DataAccessContext;
import be.bebras.rasbeb.db.DataAccessProvider;
import be.bebras.rasbeb.db.data.Language;
import be.bebras.rasbeb.db.data.Level;
import be.bebras.rasbeb.db.data.Role;
import be.bebras.rasbeb.db.jdbc.JDBCDataAccess;
import play.mvc.Http;

import javax.sql.DataSource;

/**
 * Provides access to the current {@link DataAccessProvider} which was initialized at application start.
 * Also provides some shortcuts for database request that are required often
 */
public class DataAccess {

    private static DataAccessProvider provider;


    /**
     * The current data access provider.
     */
    public static DataAccessProvider getProvider() {
        return provider;
    }

    /**
     * Obtain a data access context from the current data access provider.
     */
    public static DataAccessContext getContext(int userId, String lang, Role role) {
        return provider.getDataAccessContext(userId, lang, role);
    }

    /**
     * Used during initialization to install the data access provider. Do not call this in your own
     * programs.
     */
    public static void setProviderFromDataSource(DataSource datasource) {
        if (provider == null) {
            DataAccess.provider = JDBCDataAccess.createDataAccessProvider(datasource);
        }
    }

    /**
     * Used during initialization to install the data access provider. Do not call this in your own
     * programs.
     */
    public static void setProviderForTesting() {
        if (provider == null) {
            DataAccess.provider = JDBCDataAccess.getTestDataAccessProvider();
        }
    }

    /**
     * Return the data access context that was injected by annotating the current action
     * with {@link InjectContext}.
     */
    public static DataAccessContext getInjectedContext() {
        return (DataAccessContext) Http.Context.current().args.get("data-access-context");
    }

    /**
     * Return the list of supported languages. Value is cached. Only works in actions where
     * a context has been injected with {@link InjectContext}.
     */
    public static Iterable<Language> getLanguages() {
        @SuppressWarnings("unchecked")
        Iterable<Language> result = (Iterable<Language>)Http.Context.current().args.get ("list-of-languages");
        if (result == null) {
            result = getInjectedContext().getLanguageDAO().listLanguages();
            Http.Context.current().args.put ("list-of-languages", result);
        }
        return result;
    }

    /**
     * Return the list of levels for the current language. Value is cached. Only works in actions where a context
     * has been injected with {@link InjectContext}.
     */
    public static Iterable<Level> getLevels() {
        @SuppressWarnings("unchecked")
        Iterable<Level> result = (Iterable<Level>)Http.Context.current().args.get ("list-of-levels");
        if (result == null) {
            result = getInjectedContext().getLevelDAO().listLevels();
            Http.Context.current().args.put ("list-of-levels", result);
        }
        return result;
    }

}
