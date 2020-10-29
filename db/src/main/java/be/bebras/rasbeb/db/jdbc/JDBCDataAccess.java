/* JDBCDataAccess.java
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

import be.bebras.rasbeb.db.DataAccessProvider;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

/**
 * Provides methods to obtain a data access provider that connects to a database.
 * <p>
 * To obtain a data access provider use one of two methods
 * <ul>
 * <li>{@link #createDataAccessProvider} in production or development code</li>
 * <li>{@link #createTestDataAccessProvider}, when testing</li>
 * </ul>
 * Note that only PostgreSQL databases are supported, and only for version 9.1 or higher.
 */
public final class JDBCDataAccess {

    /**
     * Create a new data access provider based on the given data source.
     */
    public static DataAccessProvider createDataAccessProvider(DataSource dataSource) {
        return new JDBCDataAccessProvider(false, dataSource);
    }

    private static DataAccessProvider TEST_DATA_ACCESS_PROVIDER;

    /**
     * Retrieve the data access provider for use in tests. This test database uses a fixed configuration, see
     * installation documentation. The object returned is a lazily created singleton.
     */
    public synchronized static DataAccessProvider getTestDataAccessProvider() {
        if (TEST_DATA_ACCESS_PROVIDER == null) {
            TEST_DATA_ACCESS_PROVIDER = createTestDataAccessProvider();
        }
        return TEST_DATA_ACCESS_PROVIDER;
    }


    private static DataAccessProvider createTestDataAccessProvider() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();

        dataSource.setServerNames(new String[] { "localhost" });
        dataSource.setPortNumbers(new int[] { 5432 });
        dataSource.setDatabaseName("rasbeb_test");
        dataSource.setUser("rasbeb_tester");
        dataSource.setPassword("test_password");

        return new JDBCDataAccessProvider(true, dataSource);
    }

}
