/* ParticipationDAOImpl.java
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
import be.bebras.rasbeb.db.dao.ParticipationDAO;
import be.bebras.rasbeb.db.data.Participation;
import be.bebras.rasbeb.db.data.ParticipationDetail;
import be.bebras.rasbeb.db.data.Status;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC based implementation of {@link be.bebras.rasbeb.db.dao.ParticipationDAO}
 */
public class ParticipationDAOImpl extends AbstractDAOImpl implements ParticipationDAO {

    public ParticipationDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public Participation get(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT participation.id,participation.status,user_id,contest_id,lvl,lang,initial_marks,maximum_marks,total_marks," +
                        "extract (epoch from (deadline-now())), contest_type " +
                        "FROM participation JOIN contest ON contest_id = contest.id " +
                        "WHERE participation.id = ? AND participation.status <> 0"
        )) {
            stat.setInt(1, id);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return getParticipation(rs);
                } else {
                    throw new KeyNotFoundException("Unknown participation", id);
                }
            }

        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    private Participation getParticipation(ResultSet rs) throws SQLException {
        return new Participation(
                rs.getInt(1),
                Statuses.fromInt(rs.getInt(2)),
                (Integer) rs.getObject(3),
                rs.getInt(4), rs.getInt(5), rs.getString(6),
                rs.getInt(7), rs.getInt(8), rs.getInt(9),
                rs.getInt(10),
                ContestTypes.fromInt(rs.getInt(11))
        );
    }

    @Override
    public Participation findParticipationWithStatus(int contestId, int level, Status status) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT participation.id,participation.status,user_id,contest_id,lvl," +
                        "participation.lang,initial_marks,maximum_marks,total_marks," +
                        "extract (epoch from (deadline-now())), contest_type " +
                        "FROM participation JOIN contest ON contest_id = contest.id " +
                        "WHERE user_id = ? AND contest_id = ? AND lvl = ? AND participation.status = ?"
        )) {
            stat.setInt(1, context.getUserId());
            stat.setInt(2, contestId);
            stat.setInt(3, level);
            stat.setInt(4, Statuses.toInt(status));
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return getParticipation(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public ParticipationDetail get(int id, int index) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT index_c, answer, marks FROM participation_detail" +
                        " WHERE p_id = ? AND index_p = ? "
        )) {
            stat.setInt(1, id);
            stat.setInt(2, index);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new ParticipationDetail(
                            id, rs.getInt(1), index,
                            rs.getString(2), (Integer) rs.getObject(3)
                    );
                } else {
                    throw new KeyNotFoundException("Unknown participation details", id + ":" + index);
                }
            }

        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public int createParticipation(int contestId, int level, String lang) {

        int id = context.getUserId();
        String sql;
        if (id == 0) {
            sql = "INSERT INTO participation (user_id, contest_id, lvl, lang, who_created) VALUES (?,?,?,?,?)";
        } else {
            sql = "INSERT INTO participation (user_id, contest_id, lvl, lang, who_created, seq) VALUES (?,?,?,?,?,0)";
        }

        try (PreparedStatement stat = prepareStatementWithGeneratedId(sql)) {
            stat.setInt(1, id);
            stat.setInt(2, contestId);
            stat.setInt(3, level);
            stat.setString(4, lang);
            stat.setInt(5, id);
            stat.executeUpdate();
            try (ResultSet rs = stat.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
        return id;
    }

    @Override
    public int countDetails(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT count(*) FROM participation_detail WHERE p_id = ?"
        )) {
            stat.setInt(1, id);
            try (ResultSet rs = stat.executeQuery()) {
                rs.next();
                return (int) (rs.getLong(1));
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<QuestionDetails> listQuestionDetails(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT title,index_p, question_in_participation.answer IS NOT NULL, marks, correct " +
                        "FROM question_in_participation,question_i18n " +
                        "WHERE p_id=? AND question_i18n.lang = ? " +
                        "AND question_i18n.id = question_in_participation.question_id " +
                        "ORDER BY index_p")) {
            stat.setInt(1, id);
            stat.setString (2, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                List<QuestionDetails> result = new ArrayList<>();
                while (rs.next()) {
                    QuestionDetails qd = new QuestionDetails();
                    qd.title = rs.getString(1);
                    qd.indexInParticipation = rs.getInt(2);
                    qd.answered = rs.getBoolean(3);
                    qd.marks = (Integer) rs.getObject(4);
                    qd.maximumMarks = rs.getInt(5);
                    result.add(qd);
                }
                return result;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateAnswer(int id, int index, String answer) {
        // TODO: not allowed when participation is CLOSED
        try (PreparedStatement stat = prepareStatement(
                "UPDATE participation_detail SET answer=upper(trim( both from ?)), who_modified=? " +
                        "WHERE p_id=? AND index_p=?")) {
            stat.setString(1, answer);
            stat.setInt(2, context.getUserId());
            stat.setInt(3, id);
            stat.setInt(4, index);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Participation details not found", id + ":" + index);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void closeParticipation(int id) {
        try (PreparedStatement stat = prepareStatement("SELECT close_participation(?,?)")) {
            stat.setInt(1, id);
            stat.setInt(2, context.getUserId());
            stat.executeQuery();
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Map<String, List<StudentMarks>> listStudentMarksForSchool(int contestId, int level, int schoolId) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT class.name, student.id, student.name, total_marks, maximum_marks " +
                        " FROM participation " +
                        "   JOIN student ON participation.user_id = student.id " +
                        "   JOIN student_class ON student_id = student.id " +
                        "   JOIN class ON class_id = class.id " +
                        "   JOIN school ON school_id = school.id " +
                        " WHERE participation.contest_id = ? AND participation.status = " + Statuses.toInt(Status.CLOSED) +
                        "   AND participation.lvl = ? AND school.id = ?" +
                        " ORDER BY class.name, student.name")) {

            stat.setInt(1, contestId);
            stat.setInt(2, level);
            stat.setInt(3, schoolId);
            Map<String, List<StudentMarks>> map = new LinkedHashMap<>();

            try (ResultSet rs = stat.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString(1);
                    List<StudentMarks> list = map.get(key);
                    if (list == null) {
                        list = new ArrayList<>();
                        map.put(key, list);
                    }
                    StudentMarks sp = new StudentMarks();
                    sp.studentId = rs.getInt(2);
                    sp.name = rs.getString(3);
                    sp.totalMarks = rs.getInt(4);
                    sp.maximumMarks = rs.getInt(5);
                    list.add(sp);
                }
                return map;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }
}
