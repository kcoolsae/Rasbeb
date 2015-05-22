/* ConfigDAO.java
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

import be.bebras.rasbeb.db.KeyNotFoundException;
import be.bebras.rasbeb.db.data.ConfigEntry;

/**
 * Allows access to configuration data. Behaves essentially as a map with keys and values of type string. Write access is
 * restricted to administrators. Note that the keys and descriptions cannot be altered.
 */
public interface ConfigDAO {

    /**
     * Returns the entry for an associated key.
     *
     * @throws KeyNotFoundException
     */
    public ConfigEntry getConfig(String key);

    /**
     * Changes the value for an associated key. The key must already exist in the database.
     *
     * @throws KeyNotFoundException
     */
    public void updateConfig(String key, String value);

    /**
     * Lists all configuration entries in the database.
     */
    public Iterable<ConfigEntry> listConfigs();

    /**
     * Add a configuration entry to the database. Only allowed when testing.
     */
    public void addConfig(String key, String value);

    /**
     * Add a configuration description to the database, for a given language. Only allowed when testing.
     */
    public void addConfigI18n(String key, String lang, String description);


}
