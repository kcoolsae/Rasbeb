/* QuestionDAOImpl.java
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

import be.bebras.rasbeb.db.DataFrozenException;
import be.bebras.rasbeb.db.Filter;
import be.bebras.rasbeb.db.KeyNotFoundException;
import be.bebras.rasbeb.db.dao.QuestionDAO;
import be.bebras.rasbeb.db.data.Privilege;
import be.bebras.rasbeb.db.data.Question;
import be.bebras.rasbeb.db.data.QuestionI18n;
import be.bebras.rasbeb.db.data.Status;
import be.bebras.rasbeb.db.util.SecurityUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link QuestionDAO}
 */
public class QuestionDAOImpl extends AbstractDAOImpl implements QuestionDAO {

    public QuestionDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    private Question fromResult(ResultSet rs) throws SQLException {
        return new Question(rs.getInt(1), Status.DEFAULT,
                rs.getString(2), rs.getString(3), rs.getString(4),
                rs.getString(6), rs.getString(7), rs.getInt(5));
    }

    @Override
    public Question getQuestion(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT question.id, external_id, magic_q, magic_f, answer_count, " +
                        "title, correct_answer " +
                        "FROM question, question_i18n " +
                        "WHERE question.id = ? AND question.status <> 0 " +
                        "AND question.id = question_i18n.id AND question_i18n.lang = ?")) {
            stat.setInt(1, id);
            stat.setString(2, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return fromResult(rs);
                } else {
                    throw new KeyNotFoundException("Question not found", id);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public int createQuestion(int numberOfAnswers, String externalId) {

        checkPrivilege(Privilege.UPDATE_QUESTION);

        // create record
        int id;
        try (PreparedStatement stat = prepareStatementWithGeneratedId(
                "INSERT INTO question (answer_count, external_id, who_created) VALUES (?,trim(both from ?),?)")) {
            stat.setInt(1, numberOfAnswers);
            stat.setString(2, externalId);
            stat.setInt(3, context.getUserId());
            stat.executeUpdate();
            try (ResultSet rs = stat.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

        // set magic numbers
        String magicQ = SecurityUtils.getMagicNumber(20, 2 * id);
        String magicF = SecurityUtils.getMagicNumber(20, 2 * id + 1);
        try (PreparedStatement stat = prepareStatement(
                "UPDATE question SET magic_q = ?, magic_f = ?, who_modified = ? WHERE id = ?")) {
            stat.setString(1, magicQ);
            stat.setString(2, magicF);
            stat.setInt(3, context.getUserId());
            stat.setInt(4, id);
            stat.executeUpdate();
        } catch (SQLException ex) {
            throw convert(ex);
        }
        return id;
    }

    private void checkNotPartlyFrozen(int id) {
        // check whether question is frozen
        try (PreparedStatement stat = prepareStatement(
                "SELECT count(*) FROM question_i18n WHERE id=? AND frozen")) {
            stat.setInt(1, id);
            try (ResultSet rs = stat.executeQuery()) {
                rs.next();
                if (rs.getLong(1) > 0) {
                    throw new DataFrozenException("Update not allowed because question is (partly) frozen");
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    private void checkNotFrozen(int id, String lang) {
        // check whether question is frozen
        try (PreparedStatement stat = prepareStatement(
                "SELECT count(*) FROM question_i18n WHERE id=? AND lang=? AND frozen")) {
            stat.setInt(1, id);
            stat.setString(2, lang);
            try (ResultSet rs = stat.executeQuery()) {
                rs.next();
                if (rs.getLong(1) > 0) {
                    throw new DataFrozenException("Update not allowed because question is frozen");
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateExternalId(int id, String externalId) {
        checkPrivilege(Privilege.UPDATE_QUESTION);
        try (PreparedStatement stat = prepareStatement(
                "UPDATE question SET external_id = trim(both from ?), who_modified = ? WHERE id = ? AND status <> 0")) {
            stat.setString(1, externalId);
            stat.setInt(2, context.getUserId());
            stat.setInt(3, id);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Question not found (with status active)", id);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateNumberOfAnswers(int id, int numberOfAnswers) {
        checkPrivilege(Privilege.UPDATE_QUESTION);
        checkNotPartlyFrozen(id);

        // update answer count
        try (PreparedStatement stat = prepareStatement(
                "UPDATE question SET answer_count=?, who_modified=? WHERE id = ? AND status <> 0")) {
            stat.setInt(1, numberOfAnswers);
            stat.setInt(2, context.getUserId());
            stat.setInt(3, id);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Question not found (with status active)", id);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

        // clear answers
        try (PreparedStatement stat = prepareStatement(
                "UPDATE question_i18n SET correct_answer=NULL, who_modified=? WHERE id = ?")) {
            stat.setInt(1, context.getUserId());
            stat.setInt(2, id);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Question_i18n not found", id);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

    }

    @Override
    public boolean freezeQuestion(int id, String language) {
        try (PreparedStatement stat = prepareStatement("SELECT freeze_question(?,?,?)")) {
            stat.setInt(1, id);
            stat.setString(2, language);
            stat.setInt(3, context.getUserId());
            try (ResultSet rs = stat.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }


    @Override
    public Iterable<QuestionI18n> listQuestionI18n(int id) {

        try (PreparedStatement stat = prepareStatement(
                "SELECT lang, title, correct_answer, uploaded_q, uploaded_f, frozen FROM question_i18n" +
                        " WHERE id=? ORDER BY lang")) {
            stat.setInt(1, id); // TODO: check question active?
            try (ResultSet rs = stat.executeQuery()) {
                List<QuestionI18n> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new QuestionI18n(id, rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getBoolean(4), rs.getBoolean(5), rs.getBoolean(6)));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateQuestionI18n(int id, String lang, String title, String correctAnswer) {
        checkPrivilege(Privilege.UPDATE_QUESTION);
        checkNotFrozen(id, lang);

        // update
        try (PreparedStatement stat = prepareStatement(
                "UPDATE question_i18n " +
                        "SET title = trim(both from ?), correct_answer = upper(trim(both from ?)), who_modified = ? " +
                        "WHERE id = ? AND lang = ?")) {
            stat.setString(1, title);
            stat.setString(2, correctAnswer);
            stat.setInt(3, context.getUserId());
            stat.setInt(4, id);
            stat.setString(5, lang);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Question_i18n not found (not frozen)", lang + "-" + id);
            }

        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void setQuestionUploaded(int id, String lang) {
        checkPrivilege(Privilege.UPDATE_QUESTION);
        try (PreparedStatement stat = prepareStatement(
                "UPDATE question_i18n SET uploaded_q = TRUE, who_modified=? WHERE id = ? AND lang = ?")) {
            stat.setInt(1, context.getUserId());
            stat.setInt(2, id);
            stat.setString(3, lang);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Question_i18n not found", lang + "-" + id);
            }

        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void setFeedbackUploaded(int id, String lang) {
        checkPrivilege(Privilege.UPDATE_QUESTION);
        try (PreparedStatement stat = prepareStatement(
                "UPDATE question_i18n SET uploaded_f = TRUE, who_modified = ? WHERE id = ? AND lang = ?")) {
            stat.setInt(1, context.getUserId());
            stat.setInt(2, id);
            stat.setString(3, lang);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Question_i18n not found", lang + "-" + id);
            }

        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Filter<Field> createListQuestionsFilter() {
        return new FilterImpl<>(Field.class);
    }

    @Override
    public Iterable<Question> listQuestions(Filter<Field> filter, Field orderBy, boolean ascending, int offset, int limit) {

        // build select string
        StringBuilder builder = new StringBuilder(400);
        builder.append(
                "SELECT question.id, external_id, magic_q, magic_f, answer_count, " +
                        "title, correct_answer " +
                        "FROM question, question_i18n " +
                        "WHERE question.status <> 0 " +
                        "AND question.id = question_i18n.id AND question_i18n.lang = ? ");
        ((FilterImpl<Field>) filter).appendILikeString(builder);
        builder.append(" ORDER BY ").append(orderBy.name());
        if (!ascending) {
            builder.append(" DESC");
        }
        builder.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);

        // launch query
        try (PreparedStatement stat = prepareStatement(builder.toString())) {
            stat.setString(1, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                List<Question> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(fromResult(rs));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }


    @Override
    public long countQuestions(Filter<Field> filter) {
        // build select string
        StringBuilder builder = new StringBuilder(200);
        builder.append("SELECT count(*) FROM question, question_i18n" +
                " WHERE question.status <> 0" +
                " AND question.id = question_i18n.id AND question_i18n.lang = ? ");
        ((FilterImpl<Field>) filter).appendILikeString(builder);

        // launch query
        try (PreparedStatement stat = prepareStatement(builder.toString())) {
            stat.setString(1, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

    }

}
