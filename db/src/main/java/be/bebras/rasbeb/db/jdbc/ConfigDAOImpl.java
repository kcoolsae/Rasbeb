/* ConfigDAOImpl.java
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
import be.bebras.rasbeb.db.data.Privilege;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link ConfigDAO}.
 */
class ConfigDAOImpl extends AbstractDAOImpl implements ConfigDAO {

    public ConfigDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public ConfigEntry getConfig(String key) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT val,description FROM config, config_i18n" +
                        " WHERE config.k = ? AND config.k = config_i18n.k AND config_i18n.lang = ?")) {
            stat.setString(1, key);
            stat.setString(2, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new ConfigEntry(key, rs.getString(1), rs.getString(2));
                } else {
                    throw new KeyNotFoundException("Config not found", key);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateConfig(String key, String value) {
        checkPrivilege(Privilege.UPDATE_CONFIG);
        try (PreparedStatement stat = prepareStatement(
                "UPDATE config SET val=?, who_modified=? WHERE k=?")) {
            stat.setString(1, value);
            stat.setInt(2, context.getUserId());
            stat.setString(3, key);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("Config not found", key);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Iterable<ConfigEntry> listConfigs() {
        try (PreparedStatement stat = prepareStatement(
                "SELECT config.k,val,description FROM config, config_i18n" +
                        " WHERE config.k = config_i18n.k AND config_i18n.lang = ?" +
                        " ORDER BY config.k")) {
            stat.setString(1, context.getLang());
            try (ResultSet rs = stat.executeQuery()) {
                List<ConfigEntry> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new ConfigEntry(rs.getString(1), rs.getString(2), rs.getString(3)));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void addConfig(String key, String value) {
        if (context.getProvider().isTest()) {

            try (PreparedStatement stat = prepareStatement(
                    "INSERT INTO config (k,val) VALUES (?,?)"
            )) {
                stat.setString(1, key);
                stat.setString(2, value);
                stat.executeUpdate();
            } catch (SQLException ex) {
                throw convert(ex);
            }


        } else {
            throw new IllegalStateException("ConfigDAO.addConfig only allowed during testing");
        }
    }

    @Override
    public void addConfigI18n(String key, String lang, String description) {
        if (context.getProvider().isTest()) {

            try (PreparedStatement stat = prepareStatement(
                    "INSERT INTO config_i18n (k,lang,description) VALUES (?,?,?)"
            )) {
                stat.setString(1, key);
                stat.setString(2, lang);
                stat.setString(3, description);
                stat.executeUpdate();

            } catch (SQLException ex) {
                throw convert(ex);
            }

        } else {
            throw new IllegalStateException("ConfigDAO.addConfig only allowed during testing");
        }

    }
}
