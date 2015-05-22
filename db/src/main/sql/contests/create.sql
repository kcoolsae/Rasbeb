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

-- Contest
------------------------------------------------------------------------

CREATE TABLE contest (
    id SERIAL PRIMARY KEY,
    status INTEGER DEFAULT 1,
    contest_type INTEGER,

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER
);

CREATE TABLE contest_i18n (
    id INTEGER REFERENCES contest,
    lang TEXT REFERENCES i18n,

    title TEXT,

    time_modified TIMESTAMP,
    who_modified INTEGER,

    PRIMARY KEY (id, lang)
);

CREATE TABLE contest_level (
    contest_id INTEGER REFERENCES contest,
    lvl INTEGER,
    minutes INTEGER DEFAULT 45,

    time_modified TIMESTAMP,
    who_modified INTEGER,

    PRIMARY KEY (contest_id, lvl)
);

CREATE TABLE contest_level_i18n (
    contest_id INTEGER REFERENCES contest,
    lvl INTEGER,
    lang TEXT,
    frozen BOOLEAN DEFAULT FALSE,

    time_modified TIMESTAMP,
    who_modified INTEGER,

    PRIMARY KEY (contest_id, lvl, lang)
);

CREATE TRIGGER update_contest
    BEFORE UPDATE ON contest
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

-- Create the corresponding i18n entries when an new contest is created
CREATE OR REPLACE FUNCTION populate_after_contest_inserted()
	RETURNS TRIGGER AS $$
	BEGIN
	   INSERT INTO contest_i18n(id,lang)
	       SELECT NEW.id, lang FROM i18n;
	   INSERT INTO contest_level(contest_id,lvl)
	       SELECT DISTINCT NEW.id, lvl.id FROM lvl;
	   INSERT INTO contest_level_i18n(contest_id,lvl,lang)
	       SELECT DISTINCT NEW.id, lvl.id, i18n.lang FROM lvl,i18n;
	   RETURN NULL;
	END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER create_contest
    BEFORE INSERT ON contest
    FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();

CREATE TRIGGER create_contest_populate_i18
    AFTER INSERT ON contest
    FOR EACH ROW EXECUTE PROCEDURE populate_after_contest_inserted();

CREATE TRIGGER update_contest_i18n
    BEFORE UPDATE ON contest_i18n
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER update_contest_level
    BEFORE UPDATE ON contest_level
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER update_contest_level_i18n
    BEFORE UPDATE ON contest_level_i18n
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

-- view that combines information from contest,contest_level, contest_level_i18n and contest_i18n
--
CREATE VIEW contest_level_details AS
    SELECT contest_level.contest_id, contest_level.lvl, contest_i18n.lang,
           contest_type,title, minutes, frozen, status
      FROM contest,contest_i18n,contest_level_i18n,contest_level
      WHERE  contest_i18n.id = contest.id
        AND  contest_level.contest_id = contest.id
        AND  contest_level_i18n.contest_id = contest.id
        AND  contest_level_i18n.lang = contest_i18n.lang
        AND  contest_level_i18n.lvl = contest_level.lvl;
