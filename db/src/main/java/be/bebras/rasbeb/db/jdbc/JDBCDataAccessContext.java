/* JDBCDataAccessContext.java
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

import be.bebras.rasbeb.db.DataAccessContext;
import be.bebras.rasbeb.db.DataAccessException;
import be.bebras.rasbeb.db.dao.*;
import be.bebras.rasbeb.db.data.Privilege;
import be.bebras.rasbeb.db.data.Role;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

/**
 * Implementation of a data access context based on JDBC.
 */
public class JDBCDataAccessContext implements DataAccessContext {

    private JDBCDataAccessProvider provider;

    private int userId;

    private String lang;

    private Set<Privilege> privileges;

    private Connection connection;

    public JDBCDataAccessContext(JDBCDataAccessProvider provider,
                                 int userId, String lang, Role role, Connection connection) {
        this.provider = provider;

        this.userId = userId;
        this.lang = lang;
        this.privileges = role.getPrivileges();

        this.connection = connection;
    }

    public JDBCDataAccessProvider getProvider() {
        return provider;
    }

    public int getUserId() {
        return userId;
    }

    public String getLang() {
        return lang;
    }

    public Connection getConnection() {
        return connection;
    }

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

    public boolean hasPrivilege(Privilege privilege) {
        return privileges.contains(privilege);
    }

    public void setPrivileges(Role role) {
        if (provider.isTest()) {
            this.privileges = role.getPrivileges();
        } else {
            throw new IllegalStateException("DataAccessContext.setPrivileges only allowed when testing");
        }
    }

    public void setUserId(int userId) {
        if (provider.isTest()) {
            this.userId = userId;
        } else {
            throw new IllegalStateException("DataAccessContext.setId only allowed when testing");
        }
    }

    @Override
    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new DataAccessException("Auto commit could not be disabled", ex);
        }
    }

    @Override
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException ex) {
            throw new DataAccessException("Commit error", ex);
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException("Rollback error", ex);
        }
    }

    @Override
    public void close() {

        try {
            connection.setAutoCommit(true); // back to the default - in case of connection pooling
            connection.close();
        } catch (SQLException ex) {
            throw new DataAccessException("Close error", ex);
        }
    }

    // -------------------------------------------------------------
    // DAOs
    // -------------------------------------------------------------

    private ConfigDAO configDAO;

    @Override
    public ConfigDAO getConfigDAO() {
        if (configDAO == null) {
            configDAO = new ConfigDAOImpl(this);
        }
        return configDAO;
    }

    private SchoolDAO schoolDAO;

    @Override
    public SchoolDAO getSchoolDAO() {
        if (schoolDAO == null) {
            schoolDAO = new SchoolDAOImpl(this);
        }
        return schoolDAO;
    }

    @Override
    public LanguageDAO getLanguageDAO() {
        return getProvider().getLanguageDAO();
    }

    private QuestionDAO questionDAO;

    @Override
    public QuestionDAO getQuestionDAO() {
        if (questionDAO == null) {
            questionDAO = new QuestionDAOImpl(this);
        }
        return questionDAO;
    }


    private UserDAO userDAO;

    @Override
    public UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new UserDAOImpl(this);
        }
        return userDAO;
    }


    private ActivationDAO ActivationDAO;

    @Override
    public ActivationDAO getActivationDAO() {
        if (ActivationDAO == null) {
            ActivationDAO = new ActivationDAOImpl(this);
        }
        return ActivationDAO;
    }

    @Override
    public LevelDAO getLevelDAO() {
        return getProvider().getLevelDAO(lang);
    }

    private ContestDAO contestDAO;

    @Override
    public ContestDAO getContestDAO() {
        if (contestDAO == null) {
            contestDAO = new ContestDAOImpl(this);
        }
        return contestDAO;
    }

    private QuestionSetDAO questionSetDAO;

    @Override
    public QuestionSetDAO getQuestionSetDAO() {
        if (questionSetDAO == null) {
            questionSetDAO = new QuestionSetDAOImpl(this);
        }
        return questionSetDAO;
    }

    private ParticipationDAO participationDAO;

    @Override
    public ParticipationDAO getParticipationDAO() {
        if (participationDAO == null) {
            participationDAO = new ParticipationDAOImpl(this);
        }
        return participationDAO;
    }


    private TeacherSchoolClassDAO teacherSchoolClassDAO;

    @Override
    public TeacherSchoolClassDAO getTeacherSchoolClassDAO() {
        if (teacherSchoolClassDAO == null) {
            teacherSchoolClassDAO = new TeacherSchoolClassDAOImpl(this);
        }
        return teacherSchoolClassDAO;
    }

    private LocalContestDAO localContestDAO;

    @Override
    public LocalContestDAO getLocalContestDAO() {
        if (localContestDAO == null) {
            localContestDAO = new LocalContestDAOImpl(this);
        }
        return localContestDAO;
    }
}
