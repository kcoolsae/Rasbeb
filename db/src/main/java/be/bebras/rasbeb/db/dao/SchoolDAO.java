/* SchoolDAO.java
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

import be.bebras.rasbeb.db.Filter;
import be.bebras.rasbeb.db.data.School;

/**
 * Provide basic CRUD operations on schools.
 */
public interface SchoolDAO {

    public enum Field {
        NAME, STREET, ZIP, TOWN
    }

    /**
     * The school with the given id.
     */
    public School getSchool (int id);

    /**
     * Create a new school in the database. No checks for uniqueness are done,
     * @return the id of the newly created school
     */
    public int createSchool (String name, String street, String zip, String town, int year);

    /**
     * Change school data for the school with the given id
     */
    public void updateSchool (int id, String name, String street, String zip, String town, int year);

    /**
     * Delete (deactivate) a school. Not allowed when there are still active dependencies
     * to that school.
     */
    public void deleteSchool (int id);

    /**
     * Create an empty filter for use with {@link #listSchools}. The filter can be refined by calling the appropriate
     * methods from {@link Filter}.
     */
    public Filter<Field> createListSchoolsFilter();

    /**
     * Return a (partial) list of schools.
     * @param filter Filters the generated list
     * @param orderBy Field according to which the result should be sorted
     * @param ascending Whether the result should be in ascending or in descending order
     * @param offset Sequence number of the first result that should be retrieved (used in paging)
     * @param limit Maximum number of results that should be retrieved (used in paging)
     * @see #listSchoolsOfCurrentYear
     */
    public Iterable<School> listSchools (Filter<Field> filter, Field orderBy, boolean ascending, int offset, int limit);

    /**
     * Count the total number of schools satisfying a given filter, i.e., the number of schools that would be returned
     * by {@link #listSchools} if there were no offset and limit. Can be used in paging.
     */
    public long countSchools (Filter<Field> filter);

    /**
     * Return a (partial) list of schools for the current year
     * @param filter Filters the generated list
     * @param orderBy Field according to which the result should be sorted
     * @param ascending Whether the result should be in ascending or in descending order
     * @param offset Sequence number of the first result that should be retrieved (used in paging)
     * @param limit Maximum number of results that should be retrieved (used in paging)
     */
    public Iterable<School> listSchoolsOfCurrentYear (Filter<Field> filter, Field orderBy, boolean ascending, int offset, int limit);

    /**
     * Count the total number of schools satisfying a given filter, i.e., the number of schools that would be returned
     * by {@link #listSchoolsOfCurrentYear} if there were no offset and limit. Can be used in paging.
     */
    public long countSchoolsOfCurrentYear (Filter<Field> filter);


    // TODO: manipulation of inactive schools
    // TODO: trim
}
