/* DataAccessContext.java
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

package be.bebras.rasbeb.db;

import be.bebras.rasbeb.db.dao.*;
import be.bebras.rasbeb.db.data.Role;

/**
 * Context for data access. Corresponds to a single 'unit of work'. Provides
 * <ul>
 * <li>transaction support, through methods {@code begin}, {@code commit}
 * and {@code rollback}</li>
 * <li>several getters to obtain DAOs that act in this context</li>
 * <li>a {@code close} method to properly finalize access to the the data.</li>
 * </ul>
 * A typical usage pattern is as follows:
 * <pre>
 *         try (DataAccessContext context = provider.getDAOContext(...) {
 *           context.begin ();
 *           ... getConfig one or more DAOs and do something with them ...
 *           context.commit();
 *         } finally {
 *           context.rollback();
 *         }
 * </pre>
 * Contexts are provided by singleton objects of type {@link DataAccessProvider}
 * (<code>provider</code> in the example above).
 * <p>
 * Typically, creating a data context corresponds to opening a database connection,
 * and closing the data context also closes the corresponding connection. All DAOs
 * returned then use the same open connection to communicate with the database.
 * <p>
 * For this particular application the data access context also contains two
 * values which are sometimes used implicitly by the the DAOs: a user id
 * and a language code.
 */
public interface DataAccessContext extends AutoCloseable {

    /**
     * Start a transaction for this context. Should be followed later by a call to
     * either {@code commit} or {@code rollback}.
     */
    public void begin();

    /**
     * Commit all data access operations in the current transaction.
     */
    public void commit();

    /**
     * Roll back all data access operations in the current transaction.
     */
    public void rollback();

    /**
     * Close this context.
     */
    public void close();

    /**
     * Change the privileges for this context to those of the given role. (Only allowed when testing.)
     */
    public void setPrivileges (Role role);

    /**
     * Change the user id for this context. (Only allowed when testing.)
     */
    public void setUserId (int userId);

    /**
     * User id associated with this context, or 0 if no user is associated with this context.
     */
    public int getUserId ();

    /**
     * Language code associated with this context.
     */
    public String getLang ();

    /**
     * Return the (unique) {@link ConfigDAO}-object for this context.
     */
    public ConfigDAO getConfigDAO();

    /**
     * Return the (unique) {@link SchoolDAO}-object for this context.
     */
    public SchoolDAO getSchoolDAO();

    /**
     * Return the (unique) {@link LanguageDAO}-object for this context.
     */
    public LanguageDAO getLanguageDAO();

    /**
     * Return the (unique) {@link QuestionDAO}-object for this context.
     */
    public QuestionDAO getQuestionDAO();

    /**
     * Return the (unique) {@link UserDAO}-object for this context.
     */
    public UserDAO getUserDAO();


    /**
     * Return the (unique) {@link ActivationDAO}-object for this context.
     */
    public ActivationDAO getActivationDAO();

    /**
     * Return the (unique) {@link LevelDAO}-object for this context.
     */
    public LevelDAO getLevelDAO();

    /**
     * Return the (unique) {@link ContestDAO}-object for this context.
     */
    public ContestDAO getContestDAO();

    /**
     * Return the (unique) {@link QuestionSetDAO}-object for this context.
     */
    public QuestionSetDAO getQuestionSetDAO();


    /**
     * Return the (unique) {@link ParticipationDAO}-object for this context.
     */
    public ParticipationDAO getParticipationDAO();
    
    /**
     * Return the (unique) {@link TeacherSchoolClassDAO}-object for this context.
     */
    public TeacherSchoolClassDAO getTeacherSchoolClassDAO();

    /**
     * Return the (unique) {@link LocalContestDAO}-object for this context.
     */
    public LocalContestDAO getLocalContestDAO();

}
