/* ConfigDAOTest.java
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
import be.bebras.rasbeb.db.dao.ConfigDAO;
import be.bebras.rasbeb.db.data.ConfigEntry;
import be.bebras.rasbeb.db.data.Role;
import org.junit.*;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Test for the JDBC implementation of {@link ConfigDAO}
 */
public class ConfigDAOTest extends DAOTest {

    private ConfigDAO dao;

    @Before
    public void getDAOAndInitFixtures () {
        dao = context.getConfigDAO();

        dao.addConfig("key1", "value1");
        dao.addConfigI18n("key1", "en", "Key1 (english)");
    }


    @Test
    public void existingEntryFound () {
        ConfigEntry entry = dao.getConfig("key1");
        assertEquals ("key1", entry.getKey());
        assertEquals("value1", entry.getValue());
        assertEquals ("Key1 (english)", entry.getDescription());
    }

    @Test(expected = KeyNotFoundException.class)
    public void nonexistentEntryNotFound () {
        dao.getConfig("key2");
    }

    @Test
    public void existingEntryChanged () {
        context.setPrivileges(Role.ADMIN);
        dao.updateConfig("key1", "value2");
        ConfigEntry entry = dao.getConfig("key1");
        assertEquals ("key1", entry.getKey());
        assertEquals("value2", entry.getValue());
        assertEquals ("Key1 (english)", entry.getDescription());
    }

    @Test(expected = KeyNotFoundException.class)
    public void nonexistentEntryNotChanged () {
        context.setPrivileges(Role.ADMIN);
        dao.updateConfig("key2", "value2");
    }

    @Test
    public void allEntriesFound () {
        dao.addConfig("key2", "value2");
        dao.addConfigI18n("key2", "en", "Key2 (english)");

        Iterable<ConfigEntry> list = dao.listConfigs();
        Iterator<ConfigEntry> iterator = list.iterator();

        ConfigEntry entry = iterator.next();
        assertEquals ("key1", entry.getKey());
        assertEquals("value1", entry.getValue());
        assertEquals ("Key1 (english)", entry.getDescription());

        entry = iterator.next();
        assertEquals ("key2", entry.getKey());
        assertEquals("value2", entry.getValue());
        assertEquals ("Key2 (english)", entry.getDescription());

        assertEquals (false, iterator.hasNext());
    }

}
