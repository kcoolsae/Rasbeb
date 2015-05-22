/* SchoolDAOImpl.java
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

import be.bebras.rasbeb.db.Filter;
import be.bebras.rasbeb.db.KeyNotFoundException;
import be.bebras.rasbeb.db.dao.SchoolDAO;
import be.bebras.rasbeb.db.data.School;
import be.bebras.rasbeb.db.data.Status;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link SchoolDAO}.
 */
class SchoolDAOImpl extends AbstractDAOImpl implements SchoolDAO {

    public SchoolDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public School getSchool(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT name,street,zip,town,yr FROM school WHERE id = ? AND status <> 0")) {
            stat.setInt(1, id);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new School(id, Status.DEFAULT,
                            rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5));
                } else {
                    throw new KeyNotFoundException("School not found (with status active)", id);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public int createSchool(String name, String street, String zip, String town, int year) {
        try (PreparedStatement stat = prepareStatementWithGeneratedId(
                "INSERT INTO school (name,street,zip,town,yr,who_created)" +
                        " VALUES (trim(both from ?),trim(both from ?),?,trim(both from ?),?,?)")) {
            stat.setString(1, name);
            stat.setString(2, street);
            stat.setString(3, zip);
            stat.setString(4, town);
            stat.setInt(5,year);
            stat.setInt(6, context.getUserId());
            stat.executeUpdate();
            try (ResultSet rs = stat.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void updateSchool(int id, String name, String street, String zip, String town, int year) {
        try (PreparedStatement stat = prepareStatement(
                "UPDATE school SET name=trim(both from ?), street=trim(both from ?)," +
                        "zip=?, town=trim(both from ?), yr=?, who_modified=? " +
                        "WHERE id=? AND status <> 0")) {
            stat.setString(1, name);
            stat.setString(2, street);
            stat.setString(3, zip);
            stat.setString(4, town);
            stat.setInt(5, year);
            stat.setInt(6, context.getUserId());
            stat.setInt(7, id);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("School not found (with status active)", id);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

    }

    @Override
    public void deleteSchool(int id) {
        try (PreparedStatement stat = prepareStatement(
                "UPDATE school SET status=0, who_modified=? WHERE id=? AND status <> 0")) {
            stat.setInt(1, context.getUserId());
            stat.setInt(2, id);
            if (stat.executeUpdate() == 0) {
                throw new KeyNotFoundException("School not found (with status active)", id);
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Filter<Field> createListSchoolsFilter() {
        return new FilterImpl<>(Field.class);
    }

    @Override
    public Iterable<School> listSchools(Filter<Field> filter, Field orderBy, boolean ascending, int offset, int limit) {
        return listSchoolsAux (filter, orderBy, ascending, offset, limit,
                "SELECT id,name,street,zip,town,yr FROM school WHERE status <> 0"
                );
    }

    @Override
    public Iterable<School> listSchoolsOfCurrentYear(Filter<Field> filter, Field orderBy, boolean ascending, int offset, int limit) {
        return listSchoolsAux (filter, orderBy, ascending, offset, limit,
                "SELECT id,name,street,zip,town,school.yr FROM school JOIN globals USING(yr) WHERE status <> 0"
                );
    }

    private Iterable<School> listSchoolsAux(Filter<Field> filter, Field orderBy,
                            boolean ascending, int offset, int limit, String query) {
        // build select string
        StringBuilder builder = new StringBuilder(200);
        builder.append(query);
        ((FilterImpl<Field>) filter).appendILikeString(builder);
        builder.append(" ORDER BY ").append(orderBy.name());
        if (!ascending) {
            builder.append(" DESC");
        }
        builder.append(" LIMIT ").append(limit)
                .append(" OFFSET ").append(offset);

        // launch query
        try (PreparedStatement stat = prepareStatement(builder.toString());
             ResultSet rs = stat.executeQuery()) {
            List<School> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new School(rs.getInt(1), Status.DEFAULT,
                        rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6)));
            }
            return list;
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public long countSchools(Filter<Field> filter) {
        return countSchoolsAux(filter, "SELECT count(*) FROM school WHERE status <> 0");
    }

    @Override
    public long countSchoolsOfCurrentYear(Filter<Field> filter) {
        return countSchoolsAux(filter, "SELECT count(*) FROM school JOIN globals USING(yr) WHERE status <> 0");
    }


    private long countSchoolsAux(Filter<Field> filter, String query) {

        // build select string
        StringBuilder builder = new StringBuilder(200);
        builder.append(query);
        ((FilterImpl<Field>) filter).appendILikeString(builder);

        // launch query
        try (PreparedStatement stat = prepareStatement(builder.toString());
             ResultSet rs = stat.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException ex) {
            throw convert(ex);
        }

    }

}
