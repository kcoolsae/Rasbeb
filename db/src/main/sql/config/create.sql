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

-- Global 'variables'
------------------------------------------------------------------------

-- table contains one row
CREATE TABLE globals (
    yr INTEGER -- current school year, 2013 means 2013-2014
);

-- System configuration
------------------------------------------------------------------------

CREATE TABLE config (
    k TEXT PRIMARY KEY,
    val TEXT NOT NULL,
    time_modified TIMESTAMP,
    who_modified INTEGER
);

CREATE TRIGGER update_config
    BEFORE UPDATE ON config
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TABLE config_i18n (
   k TEXT REFERENCES config,
   lang TEXT REFERENCES i18n,
   description TEXT
);
