/* TeacherSchool.java
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
 * Teacher/school association (enhanced with names)
 */
public class TeacherSchool {

    private int id;

    private int teacherId;
    private String teacherName;

    private int schoolId;
    private String schoolName;

    private int year;


    public int getId() {
        return id;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public int getYear() {
        return year;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public TeacherSchool(int id, int teacherId, String teacherName, int schoolId, String schoolName, int year) {
        this.id = id;
        this.teacherId = teacherId;
        this.schoolId = schoolId;
        this.year = year;
        this.teacherName = teacherName;
        this.schoolName = schoolName;
    }
}
