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

-- Schools
------------------------------------------------------------------------

CREATE TABLE school (
    id SERIAL PRIMARY KEY,
    status INTEGER DEFAULT 1,

    -- lang CHARACTER(2), -- might allow more selective searches
    name TEXT NOT NULL,
    street TEXT,
    zip TEXT,
    town TEXT,
    yr INTEGER, -- could be set automatically in trigger

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER
);

CREATE TRIGGER update_school
    BEFORE UPDATE ON school
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER create_school
    BEFORE INSERT ON school
    FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();
