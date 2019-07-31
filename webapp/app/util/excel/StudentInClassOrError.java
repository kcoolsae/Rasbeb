/* StudentInClassOrError.java
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

/**
 * Represents one row of a student list spreadsheet
 */
public class StudentInClassOrError {

    private int rowNumber;
    private String classCode;
    private String name;
    private String firstName;
    private String email;
    private Boolean male; // null if empty
    private String bebrasId;
    private String initialPassword;

    private String errorCode;    // indicate an error

    public String getClassCode() {
        return classCode;
    }

    public String getName() {
        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean isMale() {
        return male;
    }

    public String getBebrasId() {
        return bebrasId;
    }

    public String getInitialPassword() {
        return initialPassword;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMale(Boolean male) {
        this.male = male;
    }

    public void setBebrasId(String bebrasId) {
        this.bebrasId = bebrasId;
    }

    public void setInitialPassword(String initialPassword) {
        this.initialPassword = initialPassword;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean hasError () {
        return errorCode != null;
    }

    /**
     * Return one of the keys 'gender.abbrev.male', 'gender.abbrev.female' or 'gender.abbrev.null' depending
     * on the gender of this object. Used with internationalized messages.
     */
    public String getGenderKey () {
        if (male == null)
            return "gender.abbrev.null";
        else if (male) {
            return "gender.abbrev.male";
        } else {
            return "gender.abbrev.female";
        }
    }
}
