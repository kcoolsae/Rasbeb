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

-- Question
------------------------------------------------------------------------

CREATE TABLE question (
    id SERIAL PRIMARY KEY,
    status INTEGER DEFAULT 1,
    answer_count INTEGER, -- if 0, then open ended question and not multiple choice

    external_id TEXT,
    magic_q TEXT,
    magic_f TEXT,

    time_created TIMESTAMP,
    who_created INTEGER,
    time_modified TIMESTAMP,
    who_modified INTEGER
);

CREATE TABLE question_i18n (
    id INTEGER REFERENCES question,
    lang TEXT REFERENCES i18n,

    title TEXT,
    correct_answer TEXT, -- in case of multiple choice: A,B,C,D... always trimmed upper case

    uploaded_q BOOLEAN,
    uploaded_f BOOLEAN,
    frozen BOOLEAN DEFAULT FALSE,

    time_modified TIMESTAMP,
    who_modified INTEGER,

    PRIMARY KEY (id, lang)
);

CREATE TRIGGER update_question
    BEFORE UPDATE ON question
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

-- Create the corresponding i18n entries when an new question is created
CREATE OR REPLACE FUNCTION populate_after_question_inserted()
	RETURNS TRIGGER AS $$
	BEGIN
	   INSERT INTO question_i18n(id,lang)
	       SELECT NEW.id, lang FROM i18n;
	   RETURN NULL;
	END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER create_question
    BEFORE INSERT ON question
    FOR EACH ROW EXECUTE PROCEDURE update_when_inserted();

CREATE TRIGGER create_question_populate_i18
    AFTER INSERT ON question
    FOR EACH ROW EXECUTE PROCEDURE populate_after_question_inserted();

CREATE TRIGGER update_question_i18n
    BEFORE UPDATE ON question_i18n
    FOR EACH ROW EXECUTE PROCEDURE update_when_modified();

-- View of all question / language combinations that can be frozen
CREATE VIEW freezable_question AS
    SELECT question.id AS question_id, lang
      FROM question, question_i18n
      WHERE question.id = question_i18n.id
        AND uploaded_q AND uploaded_f
        AND correct_answer IS NOT NULL;

-- Freeze a question / language combination if it can be frozen
-- pars: question id, language, who_modified
-- returns true if and only if freezing was allowed
CREATE OR REPLACE FUNCTION freeze_question (INTEGER,TEXT,INTEGER)
   RETURNS BOOLEAN AS $$
DECLARE
   r RECORD;
BEGIN
   FOR r IN SELECT question_id,lang FROM freezable_question
              WHERE question_id=$1 AND lang=$2
   LOOP -- will loop at most once!
       UPDATE question_i18n SET frozen = TRUE, who_modified=$3
           WHERE id=$1 AND lang=$2;
       RETURN TRUE;
   END LOOP;
   RETURN FALSE;
END;
$$ LANGUAGE 'plpgsql';
