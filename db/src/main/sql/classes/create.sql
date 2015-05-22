-- create.sql
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- Copyright (C) 2015 Universiteit Gent
-- 
-- This file is part of the Rasbeb project, an interactive web
-- application for Bebras competitions.
-- 
-- Corresponding author:
-- 
-- Kris Coolsaet
-- Department of Applied Mathematics, Computer Science and Statistics
-- Ghent University 
-- Krijgslaan 281-S9
-- B-9000 GENT Belgium
-- 
-- The Rasbeb Web Application is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- The Rasbeb Web Application is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
-- 
-- You should have received a copy of the GNU Affero General Public License
-- along with the Degage Web Application (file LICENSE in the
-- distribution).  If not, see <http://www.gnu.org/licenses/>.
-- 

--
-- Teacher / school association

CREATE TABLE teacher_school (
    id SERIAL PRIMARY KEY,
    status INTEGER DEFAULT 1,
    teacher_id INTEGER REFERENCES general_user,
    school_id  INTEGER REFERENCES school,

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER
);

CREATE TRIGGER update_teacher_school
    BEFORE UPDATE ON teacher_school
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER create_teacher_school
    BEFORE INSERT ON teacher_school
    FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();

-- no premature optimizing :-)
--
-- CREATE INDEX teacher_by_school ON teacher_school(school_id);
-- CREATE INDEX school_by_teacher ON teacher_school(teacher_id);

--
-- Classes

CREATE TABLE class ( 
    id SERIAL PRIMARY KEY,
    status INTEGER DEFAULT 1,
    school_id INTEGER REFERENCES school,
    name TEXT,

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER

);

CREATE TRIGGER update_class
    BEFORE UPDATE ON class
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER create_class
    BEFORE INSERT ON class
    FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();
    
--
--  Students
    
CREATE TABLE student_class (
    student_id INTEGER REFERENCES student_user,
    yr INTEGER, -- copy of year of class, used as part of the primary key to enforce uniqueness
    class_id INTEGER REFERENCES class,  -- note that the same student may belong to several classes over the years

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER,

    PRIMARY KEY (student_id, yr)
);

CREATE OR REPLACE FUNCTION update_when_student_class_inserted ()
    RETURNS TRIGGER AS $$
    BEGIN
       -- insert yr
       SELECT yr INTO STRICT NEW.yr
           FROM class JOIN school ON (school.id = class.school_id)
           WHERE class.id = NEW.class_id;

       -- same as update_when_inserted:
	   NEW.time_created = now();
	   RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';


CREATE TRIGGER update_student_class
    BEFORE UPDATE ON student_class
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER create_student_class
    BEFORE INSERT ON student_class
    FOR EACH ROW EXECUTE PROCEDURE update_when_student_class_inserted();
