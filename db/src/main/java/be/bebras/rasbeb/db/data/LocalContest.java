/* LocalContest.java
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
 * A local contest picks one specific contest to be organized by a specific teacher in a specific year
 */
public class LocalContest {

    private int id;

    private LCStatus status;

    private String title;

    private String comment;

    private String level;

    private int levelId;

    private String lang;

    private int contestId;

    private int schoolId;

    private boolean taken;

    public int getId() {
        return id;
    }

    public LCStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getComment() {
        return comment;
    }

    public String getLevel() {
        return level;
    }

    public int getLevelId() { return levelId; }

    public int getContestId() {
        return contestId;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public String getLang() {
        return lang;
    }

    /**
     * Whether this contest was already taken by the current user. Only filled in by certain queries.
     */
    public boolean isTaken() { return taken; }

    public LocalContest(int id, LCStatus status, String title, String comment, String level,
                        String lang, int contestId, int schoolId, int levelId, boolean taken) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.comment = comment;
        this.level = level;
        this.lang = lang;
        this.contestId = contestId;
        this.schoolId = schoolId;
        this.levelId = levelId;
        this.taken = taken;
    }
}
