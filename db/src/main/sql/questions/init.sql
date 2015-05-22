-- init.sql
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

-- QUESTIONS
\copy question(id,status,answer_count,external_id,magic_q,magic_f) FROM 'questions/questions.txt' DELIMITER '|' ENCODING 'UTF-8'

UPDATE question_i18n SET uploaded_q=FALSE,uploaded_f=FALSE,frozen=FALSE WHERE lang<>'fr';
UPDATE question_i18n SET uploaded_q=true,uploaded_f=TRUE,frozen=TRUE WHERE lang='fr';
UPDATE question_i18n SET uploaded_q=true,uploaded_f=TRUE,frozen=TRUE WHERE lang='nl';

CREATE TEMP TABLE tmp (id INTEGER, lang TEXT, title TEXT, correct_answer TEXT);
\copy tmp (id,lang,title,correct_answer) FROM 'questions/questions_i18n.txt' DELIMITER '|' ENCODING 'UTF-8'
UPDATE question_i18n SET title=tmp.title, correct_answer=tmp.correct_answer
       FROM tmp WHERE question_i18n.id=tmp.id AND question_i18n.lang=tmp.lang;
DROP TABLE tmp;

SELECT setval ('question_id_seq', 400) \g /dev/null
