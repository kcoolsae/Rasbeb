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

-- General purpose functions
-- ~~~~~~~~~~~~~~~~~~~~~~~~~

-- long bit of code to avoid 'create language' to generate an error when
-- the language already exists
-- cf. http://wiki.postgresql.org/wiki/CREATE_OR_REPLACE_LANGUAGE

CREATE OR REPLACE FUNCTION make_plpgsql()
RETURNS VOID
LANGUAGE SQL
AS $$
CREATE LANGUAGE plpgsql;
$$;

SELECT
    CASE
    WHEN EXISTS(
        SELECT 1
        FROM pg_catalog.pg_language
        WHERE lanname='plpgsql'
    )
    THEN NULL
    ELSE make_plpgsql() END \g /dev/null

DROP FUNCTION make_plpgsql();

-- used to automatically change the time modified time
-- cf. http://stackoverflow.com/questions/1035980/postgresql-update-timestamp-when-row-is-updated

CREATE OR REPLACE FUNCTION update_when_modified()
	RETURNS TRIGGER AS $$
	BEGIN
	   NEW.time_modified = now();
	   RETURN NEW;
	END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION update_when_inserted()
	RETURNS TRIGGER AS $$
	BEGIN
	   IF NEW.time_created IS NULL THEN
	        -- old time created should be kept when data is restored from file
	        NEW.time_created = now();
	   END IF;
	   RETURN NEW;
	END;
$$ LANGUAGE 'plpgsql';


