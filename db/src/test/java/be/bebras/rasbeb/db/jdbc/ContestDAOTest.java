/* ContestDAOTest.java
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
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Test for the JDBC implementation of {@link ContestDAO}
 */
public class ContestDAOTest extends DAOTest {

    private ContestDAO dao;

    private int[] contestIds;

    @Before
    public void getDAOAndInitFixtures() {
        dao = context.getContestDAO();
        context.setPrivileges(Role.ORGANIZER);
        contestIds = Fixtures.createContests(context);
    }

    @Test
    public void existingContest() {
        Contest contest = dao.getContest(contestIds[0]);
        assertEquals(contestIds[0], contest.getId());
        assertEquals(ContestType.OFFICIAL, contest.getType());
        assertEquals(Status.DEFAULT, contest.getStatus());
    }

    @Test
    public void i18nRecordsInitialized() {
        // check that there is at least one i18n record
        Iterator<ContestI18n> iterator = dao.listContestI18n(contestIds[1]).iterator();
        assertEquals(true, iterator.hasNext());
    }

    @Test
    public void levelRecordsInitialized() {
        Iterator<ContestLevel> iterator = dao.listContestLevels(contestIds[0]).iterator();
        assertEquals(true, iterator.hasNext());
    }

    @Test
    public void contestTypeUpdated() {
        dao.updateContestType(contestIds[0], ContestType.RESTRICTED);
        Contest contest = dao.getContest(contestIds[0]);
        assertEquals(ContestType.RESTRICTED, contest.getType());
    }

    @Test
    public void statusUpdated() {
        dao.updateStatus(contestIds[1], Status.ACTIVE);
        Contest contest = dao.getContest(contestIds[1]);
        assertEquals(Status.ACTIVE, contest.getStatus());
    }

    @Test(expected = KeyNotFoundException.class)
    public void cannotUpdateNonexistentContest() {
        // contest -1 should not exist
        dao.updateStatus(-1, Status.DEFAULT);
    }

    @Test(expected = KeyNotFoundException.class)
    public void noI18nForNonexistentContest() {
        // contest -1 should not exist
        dao.updateContestI18n(-1, "en", "Title 3");
    }

    /**
     * Find the record for the given language in the given list.
     */
    private ContestI18n find(Iterable<ContestI18n> list, String lang) {
        for (ContestI18n contestI18n : list) {
            if (lang.equals(contestI18n.getLang())) {
                return contestI18n;
            }
        }
        return null; // not found
    }

    @Test
    public void updateTitle() {
        dao.updateContestI18n(contestIds[1], "en", "Title 1");
        Contest contest = dao.getContest(contestIds[1]);
        assertEquals("Title 1", contest.getTitle()); // "en" is current language!

        ContestI18n contestI18n = find(dao.listContestI18n(contestIds[1]), "en");
        assertEquals("en", contestI18n.getLang());
        assertEquals("Title 1", contestI18n.getTitle());
    }

    @Test(expected = KeyNotFoundException.class)
    public void noUpdateForNonexistentLanguage() {
        dao.updateContestI18n(contestIds[0], "??", "Title 3");
    }

    @Test
    public void durationChanged() {
        dao.updateDuration(contestIds[1], 2, 60);
        Iterator<ContestLevel> iterator = dao.listContestLevels(contestIds[1]).iterator();
        iterator.next(); // level 1
        assertEquals(60, iterator.next().getMinutes()); // level 2
        assertEquals(60, dao.getDuration(contestIds[1], 2));
    }
}
