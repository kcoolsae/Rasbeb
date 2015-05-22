/* QuestionSetDAOImpl.java
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
import be.bebras.rasbeb.db.dao.QuestionSetDAO;
import be.bebras.rasbeb.db.data.QuestionInSet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link be.bebras.rasbeb.db.dao.QuestionSetDAO}
 */
public class QuestionSetDAOImpl extends AbstractDAOImpl implements QuestionSetDAO {

    public QuestionSetDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public QuestionInSet getQuestionInSet(int contestId, int level, int index) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT question_id,correct,wrong FROM question_in_set" +
                        " WHERE contest_id = ? AND lvl = ? AND id = ?"
        )) {
            stat.setInt(1, contestId);
            stat.setInt(2, level);
            stat.setInt(3, index);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new QuestionInSet(contestId, level, index,
                            rs.getInt(1), rs.getInt(2), rs.getInt(3));
                } else {
                    throw new KeyNotFoundException("Question not found in set",
                            contestId + "-" + level + "-" + index);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<QuestionInSet> listQuestionsInSet(int contestId, int level) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT id,question_id,correct,wrong FROM question_in_set" +
                        " WHERE contest_id = ? AND lvl = ? AND correct > 0 ORDER BY id"
        )) {
            stat.setInt(1, contestId);
            stat.setInt(2, level);
            try (ResultSet rs = stat.executeQuery()) {
                List<QuestionInSet> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new QuestionInSet(contestId, level,
                            rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)));
                }
                return result;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<QuestionWithTitle> listQuestionsInSetWithTitle(int contestId, int level) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT question_in_set.id,title FROM question_in_set " +
                        "JOIN question ON question_id = question.id " +
                        "JOIN question_i18n ON question.id = question_i18n.id " +
                        "WHERE contest_id = ? AND lvl = ? AND lang = ? AND correct > 0 ORDER BY id"
        )) {
            stat.setInt(1, contestId);
            stat.setInt(2, level);
            stat.setString(3, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                List<QuestionWithTitle> result = new ArrayList<>();
                while (rs.next()) {
                    QuestionWithTitle qwt = new QuestionWithTitle();
                    qwt.index = rs.getInt(1);
                    qwt.title = rs.getString(2);
                    result.add(qwt);
                }
                return result;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<QuestionWithMarks> listQuestionsWithMarks(int contestId) {

        // first create the list without marks
        List<QuestionWithMarks> result = new ArrayList<>();

        try (PreparedStatement stat = prepareStatement(
                "SELECT question.id, external_id, title, question_in_set.id" +
                        " FROM question, question_i18n, question_in_set" +
                        " WHERE contest_id = ? AND question.id = question_id" +
                        " AND question.id = question_i18n.id AND question_i18n.lang = ?" +
                        " AND question_in_set.lvl = 1" +
                        " ORDER BY question_in_set.id")) {
            stat.setInt(1, contestId);
            stat.setString(2, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                while (rs.next()) {
                    QuestionWithMarks qwp = new QuestionWithMarks();
                    qwp.contestId = contestId;
                    qwp.questionId = rs.getInt(1);
                    qwp.externalId = rs.getString(2);
                    qwp.title = rs.getString(3);
                    qwp.index = rs.getInt(4);
                    qwp.marks = new ArrayList<>(6);
                    result.add(qwp);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

        // now add the marks for the same list

        try (PreparedStatement stat = prepareStatement(
                "SELECT question_in_set.id, question_in_set.lvl, correct, wrong" +
                        " FROM question_in_set WHERE contest_id = ?" +
                        " ORDER BY question_in_set.id, question_in_set.lvl")) {
            stat.setInt(1, contestId);
            try (ResultSet rs = stat.executeQuery()) {
                int pos = -1;
                while (rs.next()) {
                    if (rs.getInt(2) == 1) {
                        pos++;
                    }
                    Marks marks = new Marks();
                    marks.whenCorrect = rs.getInt(3);
                    marks.whenWrong = rs.getInt(4);
                    result.get(pos).marks.add(marks);
                }
            }
            return result;
        } catch (SQLException ex) {
            throw convert(ex);
        }

    }

    @Override
    public int addQuestionInSet(int contestId, int questionId) {

        try (PreparedStatement stat = prepareStatement(
                "SELECT add_question_in_set (?,?,?) ")) {
            stat.setInt(1, contestId);
            stat.setInt(2, questionId);
            stat.setInt(3, context.getUserId());
            try (ResultSet rs = stat.executeQuery()) {
                rs.next(); // cannot go wrong?
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateQuestionInSet(int contestId, int level, int index, int marksWhenCorrect, int marksWhenWrong) {
        if (marksWhenCorrect < 0) {
            throw new IllegalArgumentException("Marks when correct cannot be negative: " + marksWhenCorrect);
        }
        if (marksWhenWrong > 0) {
            throw new IllegalArgumentException("Markss when wrong must be negative: " + marksWhenWrong);
        }

        try (PreparedStatement stat = prepareStatement(
                "UPDATE question_in_set SET correct = ?, wrong = ?, who_modified = ?" +
                        " WHERE contest_id = ? AND lvl = ? AND id = ?")) {
            stat.setInt(1, marksWhenCorrect);
            stat.setInt(2, marksWhenWrong);
            stat.setInt(3, context.getUserId());
            stat.setInt(4, contestId);
            stat.setInt(5, level);
            stat.setInt(6, index);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Question not found in set",
                        contestId + "-" + level + "-" + index);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void swapQuestionInSet(int contestId, int index1, int index2) {
        if (index1 == index2) {
            return;
        }
        try (PreparedStatement stat = prepareStatement(
                "UPDATE question_in_set SET id = ? - id " +
                "WHERE contest_id = ? AND (id = ? OR id = ?)")) {
            stat.setInt(1, index1+index2);
            stat.setInt(2, contestId);
            stat.setInt(3, index1);
            stat.setInt(4, index2);
            stat.executeUpdate();
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }
}
