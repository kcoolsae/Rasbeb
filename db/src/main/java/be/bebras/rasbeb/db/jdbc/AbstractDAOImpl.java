/* AbstractDAOImpl.java
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

import be.bebras.rasbeb.db.DataAccessException;
import be.bebras.rasbeb.db.InsufficientPrivilegesException;
import be.bebras.rasbeb.db.UniqueViolationException;
import be.bebras.rasbeb.db.data.Privilege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Common superclass of the data access objects in this package.
 */
class AbstractDAOImpl {

    protected JDBCDataAccessContext context;

    public AbstractDAOImpl(JDBCDataAccessContext context) {
        this.context = context;
    }

    /**
     * Convenience method that returns the connection from the context
     */
    protected Connection getConnection() {
        return context.getConnection();
    }

    /**
     * Convenience method for creating a  statement in the current context
     */
    protected Statement createStatement() throws SQLException {
        return context.getConnection().createStatement();
    }

    /**
     * Convenience method for creating a prepared statement in the current context
     */
    protected PreparedStatement prepareStatement(String sql) throws SQLException {
        return context.getConnection().prepareStatement(sql);
    }

    public static final String[] ID_COLUMN = new String[]{"id"};


    /**
     * Convenience method for creating a prepared statement in the current context with the
     * intention of retrieving a generated 'id'-key later.
     */
    protected PreparedStatement prepareStatementWithGeneratedId(String sql) throws SQLException {
        return context.getConnection().prepareStatement(sql, ID_COLUMN);
    }

    /**
     * Check that the current user has the indicated privilege.
     */
    protected void checkPrivilege(Privilege privilege) {
        if (!context.hasPrivilege(privilege)) {
            throw new InsufficientPrivilegesException("Database access privilege violation", privilege);
        }
    }

    /**
     * Convert an {@link SQLException} to a {@link DataAccessException} of an appropriate type (depending on the
     * status) and throw it.
     */
    public static DataAccessException convert (SQLException ex) {
        String status = ex.getSQLState();
        if (status.startsWith("23505")) {
            return new UniqueViolationException("Database error", ex);
        } else {
            return new DataAccessException("Database error", ex);
        }
    }
}
