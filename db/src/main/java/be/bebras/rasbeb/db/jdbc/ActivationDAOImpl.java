/* ActivationDAOImpl.java
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

import be.bebras.rasbeb.db.dao.ActivationDAO;
import be.bebras.rasbeb.db.data.Activation;
import be.bebras.rasbeb.db.data.Role;
import be.bebras.rasbeb.db.util.SecurityUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link ActivationDAO}
 */
public class ActivationDAOImpl extends AbstractDAOImpl implements ActivationDAO {

    public ActivationDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public String createToken(String email, Role role, boolean longlived, boolean registration) {
        String token = SecurityUtils.getToken(48);
        String expiresClause = longlived? "now() + '5 days'" : "now() + '1 hour'";

        // we ignore the possible race condition in this 'UPSERT'
        try (PreparedStatement statUpdate = prepareStatement(
                "UPDATE activation SET token = ?, role = ?, registration = ?, expires = " + expiresClause +
                        " WHERE email = lower(trim (both from (?)))" )) {
            statUpdate.setString (1, token);
            statUpdate.setInt(2, Roles.toInt(role));
            statUpdate.setBoolean(3, registration);
            statUpdate.setString (4, email);
            if (statUpdate.executeUpdate() == 0) {
                try (PreparedStatement statInsert = prepareStatement (
                        "INSERT INTO activation(email,token,role,registration,expires)" +
                                " VALUES (lower(trim (both from (?))),?,?,?," +
                                expiresClause + ")" )) {
                    statInsert.setString(1, email);
                    statInsert.setString(2, token);
                    statInsert.setInt (3, Roles.toInt(role));
                    statInsert.setBoolean(4, registration);
                    statInsert.executeUpdate();
                }
            }
            return token;
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void deleteToken(String email) {
        try (PreparedStatement stat = prepareStatement(
                "DELETE FROM activation WHERE email = lower(trim (both from (?)))")) {
            stat.setString (1, email);
            stat.executeUpdate();
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Role checkToken (String email, String token) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT role FROM activation" +
                        " WHERE email = lower(trim (both from (?))) AND token = ?" +
                        " AND expires > now()" )) {
            stat.setString (1, email);
            stat.setString (2, token);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return Roles.fromInt(rs.getInt(1));
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<Activation> listPendingRegistrations() {
        try (Statement stat = createStatement();
             ResultSet rs = stat.executeQuery(
                "SELECT email,role,expires,registration FROM activation" +
                        " WHERE registration ORDER BY expires DESC" )) {
            List<Activation> result = new ArrayList<>();
            while (rs.next()) {
                result.add (new Activation(
                        rs.getString(1), Roles.fromInt(rs.getInt(2)),
                        rs.getTimestamp(3),
                        rs.getBoolean(4)
                ));
            }
            return result;
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }
}
