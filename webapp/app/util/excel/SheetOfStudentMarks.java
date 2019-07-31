/* SheetOfStudentMarks.java
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
 * along with the Rasbeb Web Application (file LICENSE in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package util.excel;

import be.bebras.rasbeb.db.dao.ParticipationDAO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import play.i18n.Messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Converts lists of student marks to spreadsheets
 */
public class SheetOfStudentMarks {

    /**
     * Create an input stream which can be read by the server to send a spreadsheet to the server.
     */
    public static InputStream getDownloadStream(Map<String, List<ParticipationDAO.StudentMarks>> map) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.setColumnWidth(1, 15*256);
        int count = 0;

        // Header row
        Row row = sheet.createRow(count);



        String[] strings = Messages.get("spreadsheet.header.marks").split(",");
        for (int i = 0; i < strings.length; i++) {
            row.createCell(i).setCellValue(strings[i]);
        }
        count ++;

        for (Map.Entry<String, List<ParticipationDAO.StudentMarks>> entry : map.entrySet()) {

            // Blank row before each class
            count++;

            for (ParticipationDAO.StudentMarks marks : entry.getValue()) {

                row = sheet.createRow(count);
                row.createCell(0).setCellValue(entry.getKey());
                // TODO: factor out code in common with SheetOfStudentInfo
                String[] names = marks.name.split("\\s*,\\s*");
                row.createCell(1).setCellValue(names[0]);
                if (names.length > 0) {
                    row.createCell(2).setCellValue(names[1]);
                }
                row.createCell(3).setCellValue(marks.totalMarks);
                row.createCell(4).setCellValue(marks.maximumMarks);
                count++;
            }
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write (out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException("Could not create file to download", ex);
        }

    }

}
