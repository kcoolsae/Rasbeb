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

-- CONTESTS

\copy contest(id,status,contest_type) FROM 'contests/contests.txt' DELIMITER '|' ENCODING 'UTF-8'

CREATE TEMP TABLE tmp (id INTEGER, lang TEXT, title TEXT, frozen BOOLEAN);
\copy tmp (id,lang,title) FROM 'contests/contests_i18n.txt' DELIMITER '|' ENCODING 'UTF-8'
UPDATE contest_i18n SET title=tmp.title
       FROM tmp WHERE contest_i18n.id=tmp.id AND contest_i18n.lang=tmp.lang;
UPDATE contest_level SET minutes=30 WHERE contest_id = 2;
UPDATE contest_level_i18n SET frozen=TRUE WHERE contest_id < 10 AND lang='fr';
UPDATE contest_level_i18n SET frozen=TRUE WHERE contest_id < 10 AND lang='nl';

DROP TABLE tmp;
SELECT setval ('contest_id_seq', 40) \g /dev/null
