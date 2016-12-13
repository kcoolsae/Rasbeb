/* LocalContestDAO.java
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

package be.bebras.rasbeb.db.dao;

import be.bebras.rasbeb.db.data.LocalContest;
import be.bebras.rasbeb.db.data.Status;

import java.util.List;
import java.util.Map;

/**
 * Manipulates local contests and permissions
 */
public interface LocalContestDAO {

    /**
     * Create a new local contest in the database, for the current language
     */
    public int createLocalContestInSchool (int contestId, int level, String lang, int schoolId, String comment);

    /**
     * Retrieve the local contest with the given id
     */
    public LocalContest getLocalContest(int id); // TODO: only when current user owns this local contest

    /**
     * List all local contests for the given school
     */
    public Iterable<LocalContest> listLocalContestsInSchool (int schoolId);


    /**
     * Grant permission for a certain user to a certain local contest. Also allowed when the permission
     * was already granted before.
     * TODO: Only allowed by a teacher that has the given student in one of his classes.
     */
    public void grantPermission (int lcId, int studentId);

    /**
     * Grant permissions to all members of a class (that have not yet been granted permission)
     */
    public void grantPermissionToClass (int lcId, String className);

    /**
     * Remove permission for a certain user to a certain local contest. Also allowed when no permission
     * was granted to that student.
     */
    public void removePermission  (int lcId, int studentId);

    /**
     * Remove permissions from an entire class
     */
    public void removePermissionFromClass (int lcId, String className);

    /**
     * Check whether a permission record is present for the given local contest and the current user.
     * Further checks need to be done to make sure that the user can participate in the contest.
     */
    public boolean hasPermission (int lcId);

    public static class StudentPermission {
        public int studentId;
        public String name;
        public String bebrasId;
        public boolean permitted;
    }

    /**
     * List all permissions for students that are eligible for taking part in the given contest.
     * @return map that maps class keys to students. Traversing the keys of the map
     * returns them ordered alphabetically.
     */
    public Map<String,List<StudentPermission>> listPermissions (int lcId);

    /**
     * Can the given contest be opened? It cannot when it refers to an official
     * contest which is itself not opened.
     */
    //public boolean canBeOpened (int lcId);

    /**
     * Is the contest officially closed. True if and only if the associated
     * competition is official and closed.
     */
    //public boolean isOfficiallyClosed (int lcId);

    /**
     * Change the status of a local contest.
     */
    public void updateStatus(int lcId, Status status);

    /**
     * List all local contests for which the current user has permission. Also indicates whether a
     * participation record exists for the current user.
     */
    public Iterable<LocalContest> listPermittedLocalContests ();



}
