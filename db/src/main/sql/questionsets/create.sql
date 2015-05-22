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

-- Questions assigned to contests
------------------------------------------------------------------------

CREATE TABLE question_in_set (
    contest_id INTEGER REFERENCES contest,
    lvl INTEGER NOT NULL,
    id INTEGER,
    question_id INTEGER REFERENCES question,
    correct INTEGER,
    wrong INTEGER,

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER,

    PRIMARY KEY (contest_id,lvl,id)  DEFERRABLE -- deferrable so questions can be reordered
);

CREATE TRIGGER update_question_in_set
    BEFORE UPDATE ON question_in_set
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER create_question_in_set
    BEFORE INSERT ON question_in_set
    FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();

-- View combining question set and question information
--
CREATE VIEW question_set_details AS
    SELECT contest_id,lvl,lang,question_id,question_in_set.id AS index_c,frozen
    FROM question_i18n, question_in_set
    WHERE question_i18n.id = question_in_set.question_id
      AND question_in_set.correct > 0;

-- View of freezable question sets
--
CREATE VIEW freezable_question_set AS
    SELECT contest_id,lvl,lang
       FROM question_set_details
       GROUP BY contest_id,lvl,lang
       HAVING every(frozen);

-- Add the entries for a new question - one record for each level
-- parameters: context id, question id, who_created
-- returns: new index
CREATE OR REPLACE FUNCTION add_question_in_set (INTEGER, INTEGER,INTEGER)
  RETURNS INTEGER AS $$
DECLARE
   index INTEGER;
   stamp TIMESTAMP;
BEGIN
   index := ( SELECT COALESCE(MAX(id), 0)+1 FROM question_in_set WHERE contest_id = $1);
   stamp := now();
   INSERT INTO question_in_set
       SELECT DISTINCT $1, lvl.id, index, $2, 0, 0, stamp, $3 FROM lvl;
   RETURN index;
END;
$$ LANGUAGE 'plpgsql';

-- Freeze a contest / level / language combination if it can be frozen
-- pars: contest id, level, language, who_modified
-- returns true if and only if freezing was allowed
CREATE OR REPLACE FUNCTION freeze_contest (INTEGER,INTEGER,TEXT,INTEGER)
   RETURNS BOOLEAN AS $$
DECLARE
   r RECORD;
BEGIN
   FOR r IN SELECT contest_id,lvl,lang FROM freezable_question_set
              WHERE contest_id=$1 AND lvl=$2 AND lang=$3
   LOOP -- will loop at most once!
       UPDATE contest_level_i18n SET frozen = TRUE, who_modified = $4
           WHERE contest_id=$1 AND lvl=$2 AND lang=$3;
       RETURN TRUE;
   END LOOP;
   RETURN FALSE;
END;
$$ LANGUAGE 'plpgsql';
