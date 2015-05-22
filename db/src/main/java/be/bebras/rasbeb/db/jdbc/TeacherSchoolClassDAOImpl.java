/* TeacherSchoolClassDAOImpl.java
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
import be.bebras.rasbeb.db.dao.TeacherSchoolClassDAO;
import be.bebras.rasbeb.db.data.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of {@link TeacherSchoolClassDAO}. Data is cached. Only one DAO is constructed for each supported language.
 */
public class TeacherSchoolClassDAOImpl extends AbstractDAOImpl implements TeacherSchoolClassDAO {

    public TeacherSchoolClassDAOImpl(JDBCDataAccessContext context) {
        super(context);
    }

    @Override
    public int createTeacherSchool(int teacherId, int schoolId) {
        try (PreparedStatement stat = prepareStatementWithGeneratedId(
                "INSERT INTO teacher_school (teacher_id, school_id, who_created) VALUES (?,?,?)")) {
            stat.setInt(1, teacherId);
            stat.setInt(2, schoolId);
            stat.setInt(3, context.getUserId());
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
    public TeacherSchool getTeacherSchool(int teacherSchoolId) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT teacher_id, general_user.name, school_id, school.name, school.yr " +
                        "FROM teacher_school JOIN general_user ON (general_user.id = teacher_id) " +
                        " JOIN school ON (school.id = school_id) " +
                        " WHERE teacher_school.id = ?"
        )) {
            stat.setInt(1, teacherSchoolId);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new TeacherSchool(teacherSchoolId,
                            rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getInt(5));
                } else {
                    throw new KeyNotFoundException("Teacher/school not found", teacherSchoolId);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public int addSchool(int schoolId) {
        try (PreparedStatement stat = prepareStatementWithGeneratedId(
                "INSERT INTO teacher_school(teacher_id, school_id, who_created) VALUES (?,?,?)")) {
            stat.setInt(1, context.getUserId());
            stat.setInt(2, schoolId);
            stat.setInt(3, context.getUserId());
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
    public Iterable<School> listSchools() {
        try (PreparedStatement stat = prepareStatement(
                "SELECT  school.id,name,street,zip,town,school.yr " +
                        "FROM teacher_school" +
                        "  JOIN school ON teacher_school.school_id = school.id " +
                        "  JOIN globals ON globals.yr = school.yr " +
                        "  WHERE teacher_school.teacher_id = ? " +
                        "ORDER BY teacher_school.school_id")) {
            stat.setInt(1, context.getUserId());
            try (ResultSet rs = stat.executeQuery()) {
                List<School> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new School(rs.getInt(1), Status.DEFAULT,
                            rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6)));
                }
                return list;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public int createClassInSchool(int schoolId, String name) {
        try (PreparedStatement stat = prepareStatementWithGeneratedId(
                "INSERT INTO class (school_id, name,who_created) VALUES (?,trim(both from ?),?)")) {
            stat.setInt(1, schoolId);
            stat.setString(2, name);
            stat.setInt(3, context.getUserId());
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
    public ClassInfo getClass(int id) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT school_id, name FROM class WHERE id = ?"
        )) {
            stat.setInt(1, id);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return new ClassInfo(id, rs.getInt(1), rs.getString(2));
                } else {
                    throw new KeyNotFoundException("Class not found", id);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public int findClassInSchool(int schoolId, String name) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT class.id FROM class WHERE school_id = ? AND name = trim(both from ?)"
        )) {
            stat.setInt(1, schoolId);
            stat.setString(2, name);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new KeyNotFoundException("Class not found: ", schoolId + ":" + name);
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    /*
    @Override
    public Iterable<TeacherSchoolInfo> listTeacherInfo(int schoolId) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT teacher_id, school_id, teacher_school.id, general_user.name, class.id, class.name " +
                        "FROM teacher_school" +
                        "  JOIN general_user ON teacher_school.teacher_id = general_user.id " +
                        "  LEFT JOIN class ON teacher_school.id = class.ts_id " +
                        "WHERE teacher_school.school_id = ? " +
                        "ORDER BY teacher_school.teacher_id, class.name")) {
            return listTeacherOrSchoolInfo(stat, schoolId, 1);
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }
    */

    @Override
    public Iterable<TeacherSchoolInfo> listSchoolInfo(int teacherId) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT teacher_id, teacher_school.school_id, teacher_school.id, school.name, class.id, class.name " +
                        "FROM teacher_school" +
                        "  JOIN school ON teacher_school.school_id = school.id " +
                        "  JOIN globals USING (yr) " +
                        "  LEFT JOIN class ON school.id = class.school_id " +
                        "WHERE teacher_school.teacher_id = ? " +
                        "ORDER BY teacher_school.school_id, class.name")) {
            return listTeacherOrSchoolInfo(stat, teacherId, 2);
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    /**
     * Code common to (old) {@code #listTeacherInfo} and {@link #listSchoolInfo(int)}
     */
    private Iterable<TeacherSchoolInfo> listTeacherOrSchoolInfo
    (PreparedStatement stat, int id, int sortColumnIndex) throws SQLException {

        stat.setInt(1, id);
        try (ResultSet rs = stat.executeQuery()) {
            List<TeacherSchoolInfo> list = new ArrayList<>();
            TeacherSchoolInfo tsi = null;
            int currentId = -1;
            while (rs.next()) {
                int thisId = rs.getInt(sortColumnIndex);
                if (thisId != currentId) {
                    currentId = thisId;
                    tsi = new TeacherSchoolInfo();
                    tsi.teacherId = rs.getInt(1);
                    tsi.schoolId = rs.getInt(2);
                    tsi.teacherSchoolId = rs.getInt(3);
                    tsi.name = rs.getString(4);
                    tsi.classList = new ArrayList<>();
                    list.add(tsi);
                }
                assert tsi != null;
                Integer classId = (Integer) rs.getObject(5);
                if (classId != null) {
                    tsi.classList.add(new ClassInfo(classId, tsi.schoolId, rs.getString(6)));
                }
            }
            return list;
        }
    }

    @Override
    public Iterable<TeacherSchoolInfo> listSchoolInfoShort() {
        try (PreparedStatement stat = prepareStatement(
                "SELECT teacher_id, school_id, teacher_school.id, school.name " +
                        "FROM teacher_school" +
                        "  JOIN school ON teacher_school.school_id = school.id " +
                        "  JOIN globals USING (yr) " +
                        "  WHERE teacher_school.teacher_id = ? " +
                        "ORDER BY teacher_school.school_id"
        )) {
            stat.setInt(1, context.getUserId());
            try (ResultSet rs = stat.executeQuery()) {
                List<TeacherSchoolInfo> list = new ArrayList<>();
                while (rs.next()) {
                    TeacherSchoolInfo tsi = new TeacherSchoolInfo();
                    tsi.teacherId = rs.getInt(1);
                    tsi.schoolId = rs.getInt(2);
                    tsi.teacherSchoolId = rs.getInt(3);
                    tsi.name = rs.getString(4);
                    tsi.classList = new ArrayList<>();
                    list.add(tsi);
                }
                return list;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public void addStudentToClass(int studentId, int classId) {
        // TODO: check uniqueness over the years
        try (PreparedStatement stat = prepareStatement(
                "INSERT INTO student_class (student_id,class_id,who_created) VALUES (?,?,?)")) {
            stat.setInt(1, studentId);
            stat.setInt(2, classId);
            stat.setInt(3, context.getUserId());
            stat.executeUpdate();
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public Map<String, List<Student>> listClassStudentsInSchool(int schoolId) {

        // difficult to do with a single statement

        // first generate all class keys
        Map<String, List<Student>> map = new LinkedHashMap<>();
        try (PreparedStatement stat = prepareStatement(
                "SELECT class.name FROM class WHERE class.school_id = ?")) {
            stat.setInt(1, schoolId);
            try (ResultSet rs = stat.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), new ArrayList<>());
                }
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }

        // now add all students into the map
        try (PreparedStatement stat = prepareStatement(
                "SELECT class.name,student.id, email, bebras_id, student.name, male, initial_password " +
                        " FROM student " +
                        "  JOIN student_class ON student_id = student.id" +
                        "  JOIN class ON class_id = class.id" +
                        " WHERE class.school_id=?  AND student.status <> 0" +
                        " ORDER BY student.name")) {
            stat.setInt(1, schoolId);
            try (ResultSet rs = stat.executeQuery()) {

                while (rs.next()) {
                    map.get(rs.getString(1)).add(new Student(
                            rs.getInt(2), Status.DEFAULT, rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getBoolean(6), rs.getString(7)));
                }
                return map;
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }

    @Override
    public boolean fitsCurrentUser(int teacherSchoolId) {
        try (PreparedStatement stat = prepareStatement(
                "SELECT 1 FROM teacher_school " +
                        "WHERE teacher_school.id = ? AND teacher_school.teacher_id = ?")) {
            stat.setInt(1, teacherSchoolId);
            stat.setInt(2, context.getUserId());
            try (ResultSet rs = stat.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw convert(ex);
        }
    }
}
