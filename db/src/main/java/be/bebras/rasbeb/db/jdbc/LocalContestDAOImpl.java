/* LocalContestDAOImpl.java
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
import be.bebras.rasbeb.db.dao.LocalContestDAO;
import be.bebras.rasbeb.db.data.ContestType;
import be.bebras.rasbeb.db.data.LCStatus;
import be.bebras.rasbeb.db.data.LocalContest;
import be.bebras.rasbeb.db.data.Status;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of {@link be.bebras.rasbeb.db.dao.LocalContestDAO}
 */
public class LocalContestDAOImpl extends AbstractDAOImpl implements LocalContestDAO {

    public LocalContestDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public int createLocalContestInSchool(int contestId, int level, String lang, int schoolId, String comment) {
        try (PreparedStatement stat = prepareStatementWithGeneratedId(
                "INSERT INTO local_contest (contest_id,lvl,school_id,lang,comment,who_created) " +
                        "VALUES (?,?,?,?,?,?)")) {
            stat.setInt(1, contestId);
            stat.setInt(2, level);
            stat.setInt(3, schoolId);
            stat.setString(4, lang);
            stat.setString(5, comment);
            stat.setInt(6, context.getUserId());

            stat.executeUpdate();
            try (ResultSet rs = stat.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public LocalContest getLocalContest(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT id, status, title, comment, name, local_lang, contest_id, school_id, " +
                        "contest_type, parent_status, level_id, NULL " +
                        "FROM local_contest_i18n WHERE id = ? AND lang = ?")) {
            stat.setInt(1, id);
            stat.setString(2, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return fromResult(rs);
                } else {
                    throw new KeyNotFoundException("Local contest not found", id);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    /**
     * Common code between {@link #getLocalContest(int)} and {@link #listPermittedLocalContests}
     */
    private static LocalContest fromResult(ResultSet rs) throws SQLException {

        ContestType type = ContestTypes.fromInt(rs.getInt(9));
        Status status = Statuses.fromInt(rs.getInt(2));
        Status parentStatus = Statuses.fromInt(rs.getInt(10));

        LCStatus lcStatus = null;
        switch (status) {
            case DELETED:
                lcStatus = LCStatus.DELETED;
                break;
            case DEFAULT: // TODO: be more restrictive also for ContestType.RESTRICTED
                if (type == ContestType.OFFICIAL && parentStatus != Status.ACTIVE) {
                    lcStatus = LCStatus.BLOCKED;
                } else {
                    lcStatus = LCStatus.CAN_BE_OPENED;
                }
                break;
            case ACTIVE:
                lcStatus = LCStatus.OPEN;
                break;
            case CLOSED:
                if (type == ContestType.OFFICIAL && parentStatus != Status.CLOSED && parentStatus != Status.DELETED) {
                    lcStatus = LCStatus.PENDING;
                } else {
                    lcStatus = LCStatus.CLOSED;
                }
                break;
        }

        int p = rs.getInt(12);
        Status participationStatus = rs.wasNull() ? null : Statuses.fromInt(p);

        return new LocalContest(
                rs.getInt(1), lcStatus,
                rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                rs.getInt(7), rs.getInt(8), rs.getInt(11),
                participationStatus
        );
    }

    /**
     * Common code between {@link #listLocalContestsInSchool} and {@link #listPermittedLocalContests()}
     */
    private static Iterable<LocalContest> listFromResult(ResultSet rs) throws SQLException {
        List<LocalContest> list = new ArrayList<>();
        while (rs.next()) {
            list.add(fromResult(rs));
        }
        return list;
    }

    @Override
    public Iterable<LocalContest> listLocalContestsInSchool(int schoolId) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT id, status, title, comment, name, local_lang, contest_id, school_id, contest_type, " +
                        "parent_status, level_id, NULL " +
                        "FROM local_contest_i18n " +
                        "WHERE school_id = ? AND lang = ? ORDER BY title, comment")) {
            stat.setInt(1, schoolId);
            stat.setString(2, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                return listFromResult(rs);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void grantPermission(int lcId, int studentId) {
        try (PreparedStatement stat = prepareStatement(
                "INSERT INTO contest_permission(lc_id,userId,who_created) VALUES (?,?,?)")) {
            stat.setInt(1, lcId);
            stat.setInt(2, studentId);
            stat.setInt(3, context.getUserId());
            stat.executeUpdate();
        } catch (SQLException ex) {
            if (!ex.getSQLState().startsWith("23505")) {
                // ignore unique violations!
                throw convert(ex);
            }
        }
    }

    @Override
    public void removePermission(int lcId, int studentId) {
        try (PreparedStatement stat = prepareStatement(
                "DELETE FROM contest_permission WHERE lc_id=? AND userId=?")) {
            stat.setInt(1, lcId);
            stat.setInt(2, studentId);
            stat.executeUpdate();
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public boolean hasPermission(int lcId, int studentId) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT 1 FROM contest_permission WHERE lc_id=? AND userId=?")) {
            stat.setInt(1, lcId);
            stat.setInt(2, studentId);
            try (ResultSet rs = stat.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Map<String, List<StudentPermission>> listPermissions(int lcId) {

        try (PreparedStatement stat = prepareStatement(
                "SELECT class.name, student.id, student.name, bebras_id, userId " +
                        " FROM local_contest" +
                        " JOIN class USING(school_id) " +
                        " JOIN student_class ON class_id = class.id " +
                        " JOIN student ON student_id = student.id " +
                        " LEFT JOIN contest_permission " +
                        "     ON lc_id = local_contest.id AND userId = student.id " +
                        " WHERE local_contest.id =? AND student.status <> 0" +
                        " ORDER BY class.name, student.name")) {

            stat.setInt(1, lcId);
            Map<String, List<StudentPermission>> map = new LinkedHashMap<>();

            try (ResultSet rs = stat.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString(1);
                    List<StudentPermission> list = map.get(key);
                    if (list == null) {
                        list = new ArrayList<>();
                        map.put(key, list);
                    }
                    StudentPermission sp = new StudentPermission();
                    sp.studentId = rs.getInt(2);
                    sp.name = rs.getString(3);
                    sp.bebrasId = rs.getString(4);
                    sp.permitted = rs.getObject(5) != null;
                    list.add(sp);
                }
                return map;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void grantPermissionToClass(int lcId, String className) {
        try (PreparedStatement stat = prepareStatement("SELECT grant_permission_to_class(?,?,?)")) {
            stat.setInt(1, lcId);
            stat.setString(2, className);
            stat.setInt(3, context.getUserId());
            stat.executeQuery();
        } catch (SQLException ex) {
            throw convert(ex);
        }

    }

    @Override
    public void removePermissionFromClass(int lcId, String className) {
        try (PreparedStatement stat = prepareStatement("SELECT revoke_permission_from_class(?,?)")) {
            stat.setInt(1, lcId);
            stat.setString(2, className);
            stat.executeQuery();
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateStatus(int lcId, Status status) {
        // TODO: do not allow this when official and not open
        try (PreparedStatement stat = prepareStatement(
                "UPDATE local_contest SET status = ?, who_modified = ? WHERE id = ?")) {
            stat.setInt(1, Statuses.toInt(status));
            stat.setInt(2, context.getUserId());
            stat.setInt(3, lcId);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Unknown local competition", lcId);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<LocalContest> listPermittedLocalContests() {
        // TODO: regression test: also used participations of others
        try (PreparedStatement stat = prepareStatement(
                "SELECT local_contest_i18n.id, local_contest_i18n.status, title, comment, name, " +
                        " local_lang, local_contest_i18n.contest_id, school_id, " +
                        "contest_type, parent_status, level_id, participation.status " +
                        "FROM local_contest_i18n " +
                        "JOIN contest_permission ON id = contest_permission.lc_id " +
                        "LEFT JOIN participation ON local_contest_i18n.contest_id = participation.contest_id " +
                        "AND participation.user_id = userId " +
                        "WHERE userId = ? AND local_contest_i18n.lang = ? " +
                        "ORDER BY title, comment"
        )) {
            stat.setInt(1, context.getUserId());
            stat.setString(2, context.getLang());
            try (ResultSet rs = stat.executeQuery(
            )) {
                return listFromResult(rs);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

    }
}
