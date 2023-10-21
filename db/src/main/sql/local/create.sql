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

-- local competition
--
CREATE TABLE local_contest (
    id SERIAL PRIMARY KEY,
    status INTEGER DEFAULT 1, -- 2 open, 3 closed
    contest_id INTEGER REFERENCES contest,
    lvl INTEGER,
    school_id INTEGER REFERENCES school,
    lang TEXT,
    comment TEXT,

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER

);

CREATE TRIGGER update_local_contest
    BEFORE UPDATE ON local_contest
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER create_local_contest
    BEFORE INSERT ON local_contest
    FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();

-- if the record exists, then permission is granted to the given user to
-- participate in the local contest

CREATE TABLE contest_permission (
    lc_id INTEGER REFERENCES local_contest,
    userId INTEGER REFERENCES student_user,

    time_created TIMESTAMP,
    who_created INTEGER,

    PRIMARY KEY (lc_id,userId)
);

CREATE TRIGGER create_contest_permission
     BEFORE INSERT ON contest_permission
     FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();

-- grant permission to an entire class
--
-- par: local_contest id
--      class name
--      who_created
CREATE OR REPLACE FUNCTION grant_permission_to_class(INTEGER,TEXT,INTEGER)
    RETURNS VOID AS $$
    DECLARE
        id INTEGER;
    BEGIN
      FOR id IN
         SELECT student_id
            FROM class JOIN local_contest USING (school_id)
                       JOIN student_class ON (class_id = class.id)
            WHERE name=$2 AND local_contest.id=$1
         EXCEPT
         SELECT userId FROM contest_permission WHERE lc_id = $1
      LOOP
         INSERT INTO contest_permission(lc_id,userId,who_created) VALUES ($1,id,$3)
         ON CONFLICT (lc_id,userId) DO NOTHING;
      END LOOP;
    END
$$ LANGUAGE 'plpgsql';

-- delete permission from an entire class
--
-- par: local_contest id
--      class name
CREATE OR REPLACE FUNCTION revoke_permission_from_class(INTEGER,TEXT)
    RETURNS VOID AS $$
    DECLARE
        id INTEGER;
    BEGIN
      DELETE FROM contest_permission
      WHERE lc_id=$1 AND userId IN
         ( SELECT student_id
            FROM class JOIN local_contest USING (school_id)
                       JOIN student_class ON (class_id = class.id)
            WHERE name=$2 AND local_contest.id=$1 );
    END
$$ LANGUAGE 'plpgsql';


-- view which extends local_contest with information from lvl, contest and contest_i18n
-- still contains information in several languages
CREATE VIEW local_contest_i18n AS
    SELECT local_contest.id, local_contest.status,
           title, comment, lvl.id AS level_id, name, local_contest.lang AS local_lang, contest_id, school_id,
           contest_type, contest.status AS parent_status,  contest_i18n.lang
    FROM local_contest
      JOIN contest ON local_contest.contest_id = contest.id
      JOIN contest_i18n ON local_contest.contest_id = contest_i18n.id
      JOIN lvl ON contest_i18n.lang = lvl.lang AND local_contest.lvl = lvl.id;


