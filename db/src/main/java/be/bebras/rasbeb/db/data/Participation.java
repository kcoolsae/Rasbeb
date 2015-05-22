/* Participation.java
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

package be.bebras.rasbeb.db.data;

/**
 * Registers the fact that someone has taken / is taking part in a certain contest.
 * @see ParticipationDetail
 */
public class Participation {

    private int id;

    private Status status; // CLOSED indicates that the contest is finished for this participation

    private int userId;

    private int contestId;

    private ContestType contestType;

    private int level;

    private String lang;

    private int maximumMarks;

    private int initialMarks;

    private int totalMarks;

    private int secondsLeft;

    // TODO: time taken?

    /**
     * Unique id for this participation
     */
    public int getId() {
        return id;
    }

    /**
     * Status of this participation. Special value is {@link Status#CLOSED} to indicate that
     * the participation is terminated and that marks have been computed and can be consulted
     */
    public Status getStatus() {
        return status;
    }

    /**
     * The user participating in this contest. Either refers to a registered pupil or can be 0
     * to indicate an anonymous participation
     */
    public int getUserId() {
        return userId;
    }

    /**
     * The corresponding contest.
     */
    public int getContestId() {
        return contestId;
    }

    /**
     * The level at which the contest is taken
     */
    public int getLevel() {
        return level;
    }

    /**
     * The language for the question pages in this participation.
     * Note that switching languages during a contest may have strange
     * results. Titles and interface will change but not the question pages themselves. (Question pages
     * might not be available for all languages,)
     */
    public String getLang() {
        return lang;
    }

    /**
     * The number of seconds left in which to submit answers to the questions.
     * Initialized when the participation is created.
     */
    public int getSecondsLeft() {
        return secondsLeft;
    }

    /**
     * The number of marks 'earned' by giving no answer at all to this contest.
     */
    public int getInitialMarks() {
        return initialMarks;
    }

    /**
     * The maximum number of marks that can be gained with this contest.
     */
    public int getMaximumMarks() {
        return maximumMarks;
    }

    /**
     * Marks earned during this participation. Computed when the participation is closed.
     */
    public int getTotalMarks() {
        return totalMarks;
    }

    /**
     * The type of contest for which this is a participation.
     */
    public ContestType getContestType() {
        return contestType;
    }

    public Participation(int id, Status status, int userId, int contestId, int level, String lang,
                         int initialMarks, int maximumMarks, int totalMarks, int secondsLeft,
                         ContestType contestType) {
        this.id = id;
        this.status = status;
        this.userId = userId;
        this.contestId = contestId;
        this.level = level;
        this.lang = lang;
        this.initialMarks = initialMarks;
        this.maximumMarks = maximumMarks;
        this.totalMarks = totalMarks;
        this.secondsLeft = secondsLeft;
        this.contestType = contestType;
    }
}
