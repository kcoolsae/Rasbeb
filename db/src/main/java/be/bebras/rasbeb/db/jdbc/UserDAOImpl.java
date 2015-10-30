/* UserDAOImpl.java
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
import be.bebras.rasbeb.db.dao.UserDAO;
import be.bebras.rasbeb.db.data.Role;
import be.bebras.rasbeb.db.data.Status;
import be.bebras.rasbeb.db.data.Student;
import be.bebras.rasbeb.db.data.User;
import be.bebras.rasbeb.db.util.SecurityUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * JDBC implementation of {@link UserDAO}
 */
class UserDAOImpl extends AbstractDAOImpl implements UserDAO {


    public UserDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public User getUser(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT email, bebras_id, role, name FROM general_user WHERE id = ? AND status <> 0")) {
            stat.setInt(1, id);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new User(id, Status.DEFAULT,
                            rs.getString(1), rs.getString(2), Roles.fromInt(rs.getInt(3)),
                            rs.getString(4));
                } else {
                    throw new KeyNotFoundException("User not found (with status active)", id);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    private static final Random RG = new Random();

    @Override
    public int createUser(String email, Role role, String name) {
        // create record
        int id;
        try (PreparedStatement stat = prepareStatementWithGeneratedId(
                "INSERT INTO general_user (email,role,name,lang,who_created) VALUES " +
                        "( lower(trim( both from ?)),?,trim(both from ?),?,? )")) {
            stat.setString(1, email);
            stat.setInt(2, Roles.toInt(role));
            stat.setString(3, name);
            stat.setString(4, context.getLang());
            stat.setInt(5, context.getUserId());
            stat.executeUpdate();
            try (ResultSet rs = stat.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

        // create and store Bebras id - incorporates userid and role to make it unique
        //
        try (PreparedStatement stat = prepareStatement(
                "UPDATE general_user SET bebras_id = ?, who_modified=? WHERE id = ?")) {
            stat.setString(1, Integer.toString(1000000 + 200 * id + 10 * RG.nextInt(20) + Roles.toInt(role)));
            stat.setInt(2, context.getUserId());
            stat.setInt(3, id);
            stat.executeUpdate();

            context.getActivationDAO().deleteToken(email);
        } catch (SQLException ex) {
            throw convert(ex);
        }
        return id;
    }

    @Override
    public void setPassword(int id, String password) {
        String[] sh = SecurityUtils.saltAndHash(password);
        try (PreparedStatement stat = prepareStatement(
                "UPDATE general_user SET password_hash = ?, password_salt = ? WHERE id = ?")) {
            stat.setString(1, sh[1]);
            stat.setString(2, sh[0]);
            stat.setInt(3, id);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Unknown user", id);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void setPassword(String password) {
        setPassword(context.getUserId(), password);
    }

    /**
     * Code in common between {@link #findUserByBebrasId(String)} and {@link #findUserByEmail(String)}
     */
    private User findUser(String whereClause, String value) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT id, status, email, bebras_id, role, name FROM general_user " + whereClause)) {
            stat.setString(1, value);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt(1), Statuses.fromInt(rs.getInt(2)),
                            rs.getString(3), rs.getString(4), Roles.fromInt(rs.getInt(5)),
                            rs.getString(6));
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

    }

    @Override
    public User findUserByEmail(String email) {
        return findUser("WHERE email = lower(trim(both from ?))", email);
    }

    @Override
    public User findUserByBebrasId(String bebraslId) {
        return findUser("WHERE bebras_id = lower(trim(both from ?))", bebraslId);
    }

    @Override
    public User getUser(String login, String password) {


        String columnName = login.indexOf('@') >= 0 ? "email" : "bebras_id";


        try (PreparedStatement stat = prepareStatement(
                "SELECT id, email, bebras_id, role, name, password_hash, password_salt FROM general_user" +
                        " WHERE " + columnName + " = lower(trim(both from ?)) AND status <> 0")) {
            stat.setString(1, login);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString(6);
                    String salt = rs.getString(7);

                    if (SecurityUtils.isCorrectPassword(salt, hash, password)) {
                        User result = new User(rs.getInt(1), Status.DEFAULT,
                                rs.getString(2), rs.getString(3), Roles.fromInt(rs.getInt(4)),
                                rs.getString(5));
                        // delete activation
                        if (result.getEmail() != null && !result.getEmail().isEmpty()) {
                            context.getActivationDAO().deleteToken(result.getEmail());
                        }
                        return result;
                    } else {
                        return null;
                    }
                } else {
                    return null; // not found, but no reason given
                    // TODO? Defend against timing attacks?
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public boolean isPassword(String password) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT password_hash, password_salt FROM general_user" +
                        " WHERE id = ? AND status <> 0")) {
            stat.setInt(1, context.getUserId());
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString(1);
                    String salt = rs.getString(2);
                    return SecurityUtils.isCorrectPassword(salt, hash, password);
                } else {
                    throw new KeyNotFoundException("Unknown user", context.getUserId());
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public boolean hasAdministrator() {
        try (Statement stat = createStatement();
             ResultSet rs = stat.executeQuery("SELECT true FROM general_user" +
                     " WHERE status <> 0 AND role=" + Roles.toInt(Role.ADMIN) + " LIMIT 1")) {
            return rs.next();
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public int createStudent(String email, String name, boolean male, String initialPassword) {

        int id = createUser(email, Role.STUDENT, name);
        setPassword(id, initialPassword);
        try (PreparedStatement stat = prepareStatement(
                "INSERT INTO student_user(id,male,initial_password) VALUES (?,?,?)")) {
            stat.setInt(1, id);
            stat.setBoolean(2, male);
            stat.setString(3, initialPassword);
            stat.executeUpdate();
            return id;
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void resetInitialPassword(int id, String initialPassword) {
        setPassword(id, initialPassword);
        try (PreparedStatement stat = prepareStatement(
                "UPDATE student_user SET initial_password=? WHERE id=?")) {
            stat.setString(1, initialPassword);
            stat.setInt(2, id);
            stat.executeUpdate();
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Student getStudent(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT email, bebras_id, name, male, initial_password " +
                        "FROM student " +
                        "WHERE id = ? AND status <> 0")) {
            stat.setInt(1, id);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new Student(id, Status.DEFAULT,
                            rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getBoolean(4), rs.getString(5));
                } else {
                    throw new KeyNotFoundException("Student not found", id);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Student findStudent(String email, String bebrasId) {
        String whereClause;
        String arg;
        if (email != null) {
            whereClause = "WHERE email = lower(trim(both from ?))";
            arg = email;
        } else if (bebrasId != null) {
            whereClause = "WHERE bebras_id = lower(trim(both from ?))";
            arg = bebrasId;
        } else {
            throw new IllegalArgumentException("email and bebrasId cannot both be null");
        }

        try (PreparedStatement stat = prepareStatement(
                "SELECT id, email, bebras_id, name, male, initial_password FROM student " +
                        whereClause + " AND status <> 0"
        )) {
            stat.setString (1, arg);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new Student(rs.getInt(1), Status.DEFAULT,
                            rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getBoolean(5), rs.getString(6));
                } else {
                    return null;
                }
            }

        } catch (SQLException ex) {
            throw convert(ex);
        }

    }

}
