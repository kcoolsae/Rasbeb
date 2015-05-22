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

-- Users
------------------------------------------------------------------------

CREATE TABLE general_user (
    id SERIAL PRIMARY KEY,
    status INTEGER DEFAULT 1,

    email TEXT UNIQUE,
    bebras_id TEXT UNIQUE,
    role INTEGER,
    name TEXT,

    password_hash TEXT,
    password_salt TEXT,

    lang TEXT,

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER
);

CREATE TRIGGER update_user
    BEFORE UPDATE ON general_user
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER create_user
    BEFORE INSERT ON general_user
    FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();

CREATE TABLE activation (
    email TEXT UNIQUE,
    token TEXT,
    role INTEGER,
    expires TIMESTAMP,
    registration BOOLEAN DEFAULT TRUE -- registration or change password
);
    
CREATE TABLE student_user (
    id INTEGER REFERENCES general_user PRIMARY KEY,
    male BOOLEAN NOT NULL DEFAULT TRUE,
    
    initial_password TEXT,
    
    time_modified TIMESTAMP,
    who_modified INTEGER
);

CREATE TRIGGER update_student
    BEFORE UPDATE ON student_user
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();
    

CREATE VIEW student AS
    SELECT id, status, email, bebras_id, name, password_hash, password_salt, male, initial_password
      FROM student_user JOIN general_user USING(id);



