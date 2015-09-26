/* ContestDAO.java
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

import be.bebras.rasbeb.db.data.*;

import java.util.List;

/**
 * Provides operations on contests.
 */
public interface ContestDAO {

    /**
     * The contest with the given id.
     */
    public Contest getContest(int id);

    /**
     * Create a new contest in the database.
     */
    public int createContest(ContestType type);

    /**
     * Update the contest type
     */
    public void updateContestType(int id, ContestType type);

    /**
     * Update the status.
     */
    public void updateStatus (int id, Status status);

    /**
     * List the language dependent information for a contest (ordered by language).
     */
    public Iterable<ContestI18n> listContestI18n(int id);

    /**
     * Update language dependent information for a contest
     */
    public void updateContestI18n(int id, String lang, String title);


    /**
     * Total duration of a contest at a given level
     */
    public int getDuration(int id, int level);

    /**
     * Update the contest duration for the given level
     */
    public void updateDuration (int id, int level, int minutes);

    /**
     * List the level-dependent information
     */
    public Iterable<ContestLevel> listContestLevels (int id);

    public static class ContestAvailableLevels {
        public int contestId;
        public String title;
        public int minutes;
        public ContestType type;
        public Status status;
        public List<Boolean> available; // indexed by level-1
    }

    /**
     * List the public contests that are available for participation (i.e., that are frozen). Only
     * those from the current language are listed.
     */
    public Iterable<ContestAvailableLevels> listAvailablePublicContests ();

    /**
     * List the non-public contests that are frozen and therefore for which a local contest can
     * be constructed.  Only
     * those from the current language are listed. Listed most recent first.
     */
    public Iterable<ContestAvailableLevels> listOrganizableContests();

    /**
     * List the contests that are previewable by teachers. Listed oldest first.
     */
    public Iterable<ContestAvailableLevels> listPreviewableContests ();

    /**
     * Try to freeze the contest for a given level and language. Only possible when all the corresponding
     * questions were frozen for that language
     * @return whether freeze succeeded.
     */
    public boolean freezeContest(int id, int level, String language);

    // TODO: disable certain updates when (part of) a contest is frozen

}
