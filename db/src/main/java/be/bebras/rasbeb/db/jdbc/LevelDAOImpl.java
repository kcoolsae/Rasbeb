/* LevelDAOImpl.java
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
import be.bebras.rasbeb.db.dao.LevelDAO;
import be.bebras.rasbeb.db.data.Level;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link LevelDAO}. Data is cached. Only one DAO is constructed for each supported language.
 */
public class LevelDAOImpl implements LevelDAO {

    private Level[] levels = new Level[10]; //hardcoded, must be at least one more than the number of levels

    private List<Level> list = new ArrayList<>();

    /**
     * Create a new DAO of this type using the given connection. The connection
     * should be closed after creation.
     */
    public LevelDAOImpl(Connection connection, String lang) {
        try (PreparedStatement stat = connection.prepareStatement(
                "SELECT id,lang,name,description FROM lvl WHERE lang = ?")) {
            stat.setString(1, lang);
            try (ResultSet rs = stat.executeQuery()) {
                while (rs.next()) {
                    int index = rs.getInt(1);
                    levels[index] = new Level(index, rs.getString(2), rs.getString(3), rs.getString(4));
                }
            }
            for (Level level : levels) {
                if (level != null) {
                    list.add(level);
                }
            }
        } catch (SQLException ex) {
            throw AbstractDAOImpl.convert(ex);
        }
    }


    @Override
    public Level getLevel(int level) {
        if (level < 0 || level >= levels.length || levels[level] == null) {
            throw new KeyNotFoundException("No level with this value", level);
        } else {
            return levels[level];
        }
    }

    @Override
    public Iterable<Level> listLevels() {
        return list;
    }
}
