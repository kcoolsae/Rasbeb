/* SheetOfStudentInfo.java
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

package util.excel;

import be.bebras.rasbeb.db.DataAccessContext;
import be.bebras.rasbeb.db.DataAccessException;
import be.bebras.rasbeb.db.dao.UserDAO;
import be.bebras.rasbeb.db.data.Student;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import play.i18n.Messages;
import util.PasswordGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Converts list of students in classes to spreadsheets
 */
public class SheetOfStudentInfo {

    // TODO: encapsulate the list of entries in an object removing the need for static methods

    private static int countNonBlankCells(Row row) {
        int count = 0;
        int cellCount = row.getLastCellNum();
        for (int i = 0; i < cellCount; i++) {
            Cell cell = row.getCell(i, Row.RETURN_BLANK_AS_NULL);
            if (cell != null &&
                    cell.getCellType() == Cell.CELL_TYPE_STRING &&
                    !cell.getStringCellValue().trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static boolean cellStartsComment(Cell cell) {
        if (cell == null || cell.getCellType() != Cell.CELL_TYPE_STRING)
            return false;
        String value = cell.getStringCellValue();
        return value != null && !value.isEmpty() && value.charAt(0) == '#';
    }

    /** Returns a string value, even for numeric type strings.  */
    private static String getStringValueOrNull(Row row, int index) {
        Cell cell = row.getCell(index, Row.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            // TODO: use DataFormatter for this?
            double value = cell.getNumericCellValue();
            if (value == (long)value) {
                return Long.toString ((long) value);
            } else {
                return Double.toString((long) value);
            }
        } else {
            return null;
        }
    }

    /**
     * Reads student information which is present in the given spreadsheet
     */
    public static List<StudentInClassOrError> read(File file) {
        try {
            Workbook workBook = WorkbookFactory.create(file);
            Sheet sheet = workBook.getSheetAt(0);
            // TODO: check that there is exactly one sheet

            List<StudentInClassOrError> result = new ArrayList<>();
            for (Row row : sheet) {

                // ignore empty rows
                if (countNonBlankCells(row) > 0) {
                    Cell firstCell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
                    // ignore 'comment' lines
                    if (!cellStartsComment(firstCell)) {
                        StudentInClassOrError data = new StudentInClassOrError();

                        data.setRowNumber(row.getRowNum() + 1);

                        String classCode = getStringValueOrNull(row, 0);
                        if (classCode == null || classCode.trim().isEmpty()) {
                            data.setErrorCode("spreadsheet.classcode.empty");
                            continue;
                        }
                        data.setClassCode(classCode);

                        String name = getStringValueOrNull(row, 1);
                        if (name == null || name.trim().isEmpty()) {
                            data.setErrorCode("spreadsheet.name.empty");
                        }
                        data.setName(name);

                        String firstName = getStringValueOrNull(row, 2);
                        if (firstName == null || firstName.trim().isEmpty()) {
                            data.setErrorCode("spreadsheet.firstname.empty");
                        }
                        data.setFirstName(firstName);

                        data.setEmail(getStringValueOrNull(row, 3));

                        String gender = getStringValueOrNull(row, 4);
                        if (gender != null && !gender.isEmpty()) {
                            // TODO: do not hard code language information
                            char firstChar = Character.toLowerCase(gender.charAt(0));
                            if (firstChar == 'm') {
                                data.setMale(true);
                            } else if (firstChar == 'f' || firstChar == 'v') {
                                data.setMale(false);
                            } else {
                                data.setErrorCode("spreadsheet.invalid.gender");
                            }
                        } else {
                            data.setErrorCode("spreadsheet.gender.empty");
                        }

                        data.setBebrasId(getStringValueOrNull(row, 5));
                        result.add(data);
                    }
                }
            }
            return result;

        } catch (IOException | OpenXML4JException ex) {
            throw new RuntimeException("Error reading spreadsheet file", ex);
        }
    }

    /**
     * Checks whether a student with exactly the same name occurs in the given list
     *
     * @param data Information to be checked for
     * @param list List in which to search
     */
    private static boolean studentExists(StudentInClassOrError data, List<Student> list) {
        // TODO: also check bebras id
        for (Student student : list) {
            if (student.getName().equalsIgnoreCase(data.getName() + ", " + data.getFirstName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the given list with a map of all classes and students currently in the database. Removes
     * data that corresponds to students that are already registered in this class.
     */
    public static void checkList(List<StudentInClassOrError> list, Map<String, List<Student>> map) {
        Iterator<StudentInClassOrError> iter = list.iterator();
        while (iter.hasNext()) {
            StudentInClassOrError data = iter.next();
            if (!data.hasError()) {
                List<Student> studentList = map.get(data.getClassCode());
                if (studentList == null) {
                    data.setErrorCode("spreadsheet.unknown.class");
                } else if (studentExists(data, studentList)) {
                    iter.remove();
                }
            }
        }
    }

    public static int countErrors(List<StudentInClassOrError> list) {
        int count = 0;
        for (StudentInClassOrError data : list) {
            if (data.hasError()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check whether the information about the student as retrieved from the database is compatible
     * with the information stored in the spreadsheet. Stores an error if not compatible
     */
    private static boolean isCompatible (Student student, StudentInClassOrError data) {

        if (! student.getName().equalsIgnoreCase(data.getName() + ", " + data.getFirstName())) {
            data.setErrorCode("student.wrong.name");
            return false;
        }
        if (data.getEmail() != null && ! student.getEmail().equalsIgnoreCase(data.getEmail())) {
            data.setErrorCode("student.wrong.email");
            return false;
        }
        if (data.getBebrasId() != null && ! student.getBebrasId().equalsIgnoreCase(data.getBebrasId())) {
            data.setErrorCode("student.wrong.bebras");
            return false;
        }
        if (data.isMale() != student.isMale()) {
            data.setErrorCode("student.wrong.gender");
            return false;
        }
        return true;
    }

    /**
     * Registers the given student with the given class. If a student is known to the system,
     * only his initial password is reset.
     */
    public static void registerStudent(DataAccessContext context, int schoolId, StudentInClassOrError data) {

        // TODO: 5 database connections per student is a lot. Class/student map could contain class ids
        int classId = context.getTeacherSchoolClassDAO().findClassInSchool(schoolId, data.getClassCode());

        UserDAO dao = context.getUserDAO();

        data.setInitialPassword(PasswordGenerator.generate());

        String email = data.getEmail();
        String bebrasId = data.getBebrasId();
        if (email != null || bebrasId != null) {
            // student may already exist in the database
            Student student = dao.findStudent(email, bebrasId);
            if (student != null) {
                if (isCompatible(student, data)) {
                    dao.resetInitialPassword(student.getId(), data.getInitialPassword());
                    data.setBebrasId(student.getBebrasId());
                    context.getTeacherSchoolClassDAO().addStudentToClass(student.getId(), classId);
                    return; // registration finished
                } else {
                    return; // not registered
                }
            }
        }

        // this is a new student
        int studentId;
        try {
            studentId = dao.createStudent(
                    email,
                    data.getName() + ", " + data.getFirstName(),
                    data.isMale(),
                    data.getInitialPassword()
            );
        } catch (DataAccessException ex) {
            data.setErrorCode("student.not.created");
            return; // not registered
        }
        data.setBebrasId(dao.getUser(studentId).getBebrasId());
        context.getTeacherSchoolClassDAO().addStudentToClass(studentId, classId);
    }

    /**
     * Create an input stream which can be read by the server to send a spreadsheet to the server.
     */
    public static InputStream getDownloadStream(Map<String, List<Student>> map) {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.setColumnWidth(1, 15 * 256);
        sheet.setColumnWidth(3, 20 * 256);
        sheet.setColumnWidth(4, 3 * 256);
        int count = 0;

        // Header row
        Row row = sheet.createRow(count);


        String[] strings = Messages.get("spreadsheet.header.info").split(",");
        for (int i = 0; i < strings.length; i++) {
            row.createCell(i).setCellValue(strings[i]);
        }
        count++;

        for (Map.Entry<String, List<Student>> entry : map.entrySet()) {

            // Blank row before each class
            count++;

            for (Student student : entry.getValue()) {
                row = sheet.createRow(count);
                row.createCell(0).setCellValue(entry.getKey());
                String[] names = student.getName().split("\\s*,\\s*");
                row.createCell(1).setCellValue(names[0]);
                if (names.length > 0) {
                    row.createCell(2).setCellValue(names[1]);
                }
                row.createCell(3).setCellValue(student.getEmail());

                row.createCell(4).setCellValue(
                        Messages.get(student.isMale() ? "gender.abbrev.male" : "gender.abbrev.female")
                );

                row.createCell(5).setCellValue(student.getBebrasId());
                row.createCell(6).setCellValue(student.getInitialPassword());
                count++;
            }
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException("Could not create file to download", ex);
        }
    }

}
