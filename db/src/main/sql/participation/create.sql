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

-- Participation
-----------------------------------------------------------------------

CREATE TABLE participation (
    id SERIAL PRIMARY KEY,
    status INTEGER DEFAULT 1,
    user_id INTEGER,
    contest_id INTEGER REFERENCES contest,
    lvl INTEGER NOT NULL,
    seq integer default currval('participation_id_seq'),
    lang TEXT NOT NULL,
    initial_marks INTEGER,
    maximum_marks INTEGER,
    total_marks INTEGER,  -- only filled in after participation is closed
    deadline TIMESTAMP,

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER,

    UNIQUE (user_id,contest_id,lvl,seq)
);

CREATE TABLE participation_detail (
    p_id INTEGER REFERENCES participation,
    index_p INTEGER, -- consecutive, starts at 1
    index_c INTEGER,
    answer TEXT, 
    marks INTEGER, -- only filled in after participation is closed
    
    time_created TIMESTAMP,
    -- who_created INTEGER,      -- no longer included: difficult to automate
    time_modified TIMESTAMP,
    who_modified INTEGER,

    PRIMARY KEY (p_id,index_p)
);

-- A view of all questions that belong to a certain participation
--
CREATE VIEW question_in_participation
    AS SELECT p_id, index_p, question_id, correct, wrong, answer, marks, lang
     FROM participation, participation_detail, question_in_set
     WHERE participation.id = participation_detail.p_id
       AND question_in_set.contest_id = participation.contest_id
       AND question_in_set.lvl = participation.lvl
       AND question_in_set.id = participation_detail.index_c;

CREATE TRIGGER update_participation
    BEFORE UPDATE ON participation
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

CREATE TRIGGER create_participation_detail
    BEFORE INSERT ON participation_detail
    FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();

CREATE TRIGGER update_participation_detail
    BEFORE UPDATE ON participation_detail
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

-- Trigger function for participation
--   - updates user and time stamps
--   - computes maximum and initial marks
--   - sets deadline
--   - sets time_created and who created
CREATE OR REPLACE FUNCTION update_when_participation_inserted()
  RETURNS TRIGGER AS $$
  DECLARE
     w INTEGER;
     c INTEGER;
     m INTEGER;
  BEGIN

      SELECT SUM(wrong), SUM(correct) FROM question_in_set
           WHERE question_in_set.correct > 0
             AND question_in_set.lvl = NEW.lvl
             AND question_in_set.contest_id = NEW.contest_id
           INTO w, c;
      NEW.initial_marks = -w;
      NEW.maximum_marks = c - w;

      SELECT minutes INTO m
             FROM contest_level
             WHERE contest_level.lvl = NEW.lvl
             AND contest_level.contest_id = NEW.contest_id;

      NEW.deadline = NOW() + m * INTERVAL '1 minute';


	  NEW.time_created = now();

	  RETURN NEW;
  END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER create_participation
    BEFORE INSERT ON participation
    FOR EACH ROW EXECUTE PROCEDURE update_when_participation_inserted();


-- Create the participation details after the participation itself is created
CREATE OR REPLACE FUNCTION populate_after_participation_inserted()
   RETURNS TRIGGER AS $$
   DECLARE
      r INTEGER;
      index INTEGER;
   BEGIN
      index := 1;
      FOR r IN
          SELECT index_c
            FROM question_set_details
            WHERE question_set_details.lang = NEW.lang
             AND question_set_details.lvl = NEW.lvl
             AND question_set_details.contest_id = NEW.contest_id
             ORDER BY index_c
      LOOP
         INSERT INTO participation_detail(p_id,index_p,index_c)
            VALUES (NEW.id,index,r);
         index := index + 1;
      END LOOP;
      RETURN NULL;
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER create_participation_populate_i18
    AFTER INSERT ON participation
    FOR EACH ROW EXECUTE PROCEDURE populate_after_participation_inserted();

--
-- Compute the marks for every answer and change participation status to CLOSED
-- par: p_id  who_modified
CREATE OR REPLACE FUNCTION close_participation(INTEGER,INTEGER)
    RETURNS VOID AS $$
    DECLARE
       r RECORD;
       m INTEGER;
       t INTEGER;
    BEGIN
      t := 0;
      FOR r IN
          SELECT index_p AS index,
                 CASE
                    WHEN question_in_participation.answer = '' THEN NULL
                    ELSE question_in_participation.answer
                 END AS my_answer,
                 question_i18n.correct_answer,
                 wrong, correct
           FROM question_in_participation,question_i18n
           WHERE p_id=$1
             AND question_in_participation.lang=question_i18n.lang
             AND question_in_participation.question_id=question_i18n.id
      LOOP
          IF r.my_answer IS NULL THEN
            m := 0;
          ELSIF r.my_answer = r.correct_answer THEN
            m := r.correct;
          ELSE
            m := r.wrong;
          END IF;

          UPDATE participation_detail
             SET marks = m, who_modified=$2 WHERE p_id = $1 AND index_p= r.index;
          t := t + m;
       END LOOP;


       UPDATE participation SET status = 3, total_marks = initial_marks + t, who_modified=$2  WHERE id = $1;

    END;
$$ LANGUAGE 'plpgsql';
