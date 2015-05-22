/* TeacherSchoolClassDAO.java
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

import be.bebras.rasbeb.db.data.ClassInfo;
import be.bebras.rasbeb.db.data.School;
import be.bebras.rasbeb.db.data.Student;
import be.bebras.rasbeb.db.data.TeacherSchool;

import java.util.List;
import java.util.Map;

/**
 * Provides some operations on teacher / school / class / pupil associations
 */
public interface TeacherSchoolClassDAO {

    /**
     * Associate a teacher with a school
     * @return id of the teacher-school association
     */
    public int createTeacherSchool (int teacherId, int schoolId);

    /**
     * Get the basic information about the teacher/school combination with the given id.
     */
    public TeacherSchool getTeacherSchool  (int teacherSchoolId);

    /**
     * Add a class for a school
     */
    public int createClassInSchool (int schoolId, String name);

    /**
     * Return information about a given class
     */
    public ClassInfo getClass (int id);   // TODO: probably not used

    /**
     * Retrieve the id of the class with the given name
     */
    public int findClassInSchool (int schoolId, String name); // TODO: should this be 'find' or 'get' or 'fetch'?

    /**
     * Associate the current user with a school
     */
    public int addSchool (int schoolId);

    /**
     * List all schools of the current teacher for the current year
     */
    public Iterable<School> listSchools();

    /**
     * Transfer object for use with {@link #listSchoolInfo(int)}
     */
    public static class TeacherSchoolInfo {

        public int schoolId;
        public int teacherId;
        public int teacherSchoolId;
        public String name; // either teacher or school name

        public List<ClassInfo> classList;
    }

    /**
     * List all teachers in a given school, and for each teacher, all classes. (For the year corresponding to the school)
     * Note: the names stored in the resulting list are those of the teachers,
     */
    //public Iterable<TeacherSchoolInfo> listTeacherInfo (int schoolId);

    // TODO? List all classes in a given school

    /**
     * List all schools for a given teacher, and for each school, all classes. (Only for the current year.)
     * Note: the names stored in the resulting list are those of the schools,
     */
    public Iterable<TeacherSchoolInfo> listSchoolInfo (int teacherId); // TODO: is this still needed in this form?

    /**
     * List all schools of the current teacher, for the current year.
     * Note: the names stored in the resulting list are those of the schools, The class lists are empty.
     */
    public Iterable<TeacherSchoolInfo> listSchoolInfoShort ();


    /**
     * Add the given student to the given class
     */
    public void addStudentToClass (int studentId, int classId);

    /**
     * Returns all class names and students  for a given school. Classes without students
     * are also included in the result (with an empty associated list of students). Traversing the keys of the map
     * returns them ordered alphabetically.
     */
    public Map<String,List<Student>> listClassStudentsInSchool (int schoolId);

    /**
     * Checks whether the given teacher/school id corresponds to the current user.
     */
    public boolean fitsCurrentUser (int teacherSchoolId);

}
