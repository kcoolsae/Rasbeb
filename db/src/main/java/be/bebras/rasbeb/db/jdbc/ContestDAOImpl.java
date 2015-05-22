/* ContestDAOImpl.java
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
import be.bebras.rasbeb.db.dao.ContestDAO;
import be.bebras.rasbeb.db.data.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link be.bebras.rasbeb.db.dao.ContestDAO}
 */
public class ContestDAOImpl extends AbstractDAOImpl implements ContestDAO {

    public ContestDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public Contest getContest(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT status, contest_type, title" +
                        " FROM contest, contest_i18n" +
                        " WHERE contest.id = ? AND contest.status <> 0" +
                        " AND contest.id = contest_i18n.id AND contest_i18n.lang = ?")) {
            stat.setInt(1, id);
            stat.setString(2, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new Contest(id, Statuses.fromInt(rs.getInt(1)),
                            ContestTypes.fromInt(rs.getInt(2)), rs.getString(3));
                } else {
                    throw new KeyNotFoundException("Contest not found", id);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public int createContest(ContestType type) {

        checkPrivilege(Privilege.UPDATE_CONTEST);

        // create record
        try (PreparedStatement stat = prepareStatementWithGeneratedId(
                "INSERT INTO contest (contest_type,who_created) VALUES (?,?)")) {
            stat.setInt(1, ContestTypes.toInt(type));
            stat.setInt(2, context.getUserId());
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
    public void updateContestType(int id, ContestType type) {
        checkPrivilege(Privilege.UPDATE_CONTEST);

        try (PreparedStatement stat = prepareStatement(
                "UPDATE contest SET contest_type = ?, who_modified = ? WHERE id = ? AND status <> 0")) {
            stat.setInt(1, ContestTypes.toInt(type));
            stat.setInt(2, context.getUserId());
            stat.setInt(3, id);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Contest not found (with status active)", id);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateStatus(int id, Status status) {
        checkPrivilege(Privilege.UPDATE_CONTEST);

        try (PreparedStatement stat = prepareStatement(
                "UPDATE contest SET status = ?, who_modified = ? WHERE id = ? AND status <> 0")) {
            stat.setInt(1, Statuses.toInt(status));
            stat.setInt(2, context.getUserId());
            stat.setInt(3, id);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Contest not found (with status active)", id);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<ContestI18n> listContestI18n(int id) {

        try (PreparedStatement stat = prepareStatement(
                "SELECT lang, title FROM contest_i18n" +
                        " WHERE id=? ORDER BY lang")) {
            stat.setInt(1, id); // TODO: check contest active?
            try (ResultSet rs = stat.executeQuery()) {
                List<ContestI18n> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new ContestI18n(id, rs.getString(1), rs.getString(2)));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateContestI18n(int id, String lang, String title) {
        checkPrivilege(Privilege.UPDATE_CONTEST);
        try (PreparedStatement stat = prepareStatement(
                "UPDATE contest_i18n SET title = trim(both from ?), who_modified=? WHERE id = ? AND lang = ?")) {
            stat.setString(1, title);
            stat.setInt(2, context.getUserId());
            stat.setInt(3, id); // TODO: check contest active?
            stat.setString(4, lang);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Contest_i18n not found", lang + "-" + id);
            }

        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public int getDuration(int id, int level) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT minutes FROM contest_level WHERE contest_id = ? AND lvl = ?"
        )) {
            stat.setInt(1, id);
            stat.setInt(2, level);
            try (ResultSet rs = stat.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateDuration(int id, int level, int minutes) {
        try (PreparedStatement stat = prepareStatement(
                "UPDATE contest_level SET minutes = ?, who_modified = ? WHERE contest_id = ? AND lvl = ?"
        )) {
            stat.setInt(1, minutes);
            stat.setInt(2, context.getUserId());
            stat.setInt(3, id);
            stat.setInt(4, level);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Contest/level not found ", id + ":" + level);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<ContestLevel> listContestLevels(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT lvl, minutes FROM contest_level" +
                        " WHERE contest_id=? ORDER BY lvl")) {
            stat.setInt(1, id); // TODO: check contest active?
            try (ResultSet rs = stat.executeQuery()) {
                List<ContestLevel> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new ContestLevel(id, rs.getInt(1), rs.getInt(2)));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public boolean freezeContest(int id, int level, String language) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT freeze_contest(?,?,?,?)")) {
            stat.setInt(1, id);
            stat.setInt(2, level);
            stat.setString(3, language);
            stat.setInt(4, context.getUserId());
            try (ResultSet rs = stat.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<ContestAvailableLevels> listAvailablePublicContests() {
        return getLevelsAux("AND contest_type=0 AND status=2");
    }

    @Override
    public Iterable<ContestAvailableLevels> listOrganizableContests() {
        return getLevelsAux("AND contest_type<>0 AND status <> 0");
    }

    @Override
    public Iterable<ContestAvailableLevels> listPreviewableContests() {
        return getLevelsAux("AND (contest_type=1 AND status <> 0) OR (contest_type=2 AND status=3)");
    }

    private Iterable<ContestAvailableLevels> getLevelsAux(String whereClause) {

        try (PreparedStatement stat = prepareStatement(
                "SELECT contest_id,title,lvl,minutes,contest_type,status " +
                        "FROM contest_level_details  " +
                        "WHERE frozen AND lang = ? " + whereClause +
                        " ORDER BY contest_id,lvl"
        )) {
            stat.setString(1, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                List<ContestAvailableLevels> list = new ArrayList<>();
                ContestAvailableLevels cal = new ContestAvailableLevels();
                cal.contestId = -1;
                while (rs.next()) {
                    int contestId = rs.getInt(1);
                    if (cal.contestId != contestId) {
                        cal = new ContestAvailableLevels();
                        cal.contestId = contestId;
                        cal.title = rs.getString(2);
                        cal.minutes = rs.getInt(4);
                        cal.type = ContestTypes.fromInt(rs.getInt(5));
                        cal.status = Statuses.fromInt(rs.getInt(6));
                        cal.available = new ArrayList<>();
                        list.add(cal);
                    }
                    int level = rs.getInt(3);
                    while (cal.available.size() < level) {
                        cal.available.add(false);
                    }
                    cal.available.set(level - 1, true);
                }
                return list;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }
}
