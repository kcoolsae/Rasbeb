-- remove-tables.sql
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

-- Removes the tables created by create-tables.sql. This is done in reverse order, so that
-- no constraints are violated.
--
-- (Note that triggers are automatically removed with the corresponding tables.)

DROP VIEW local_contest_i18n;
DROP TABLE contest_permission;
DROP TABLE local_contest;

DROP VIEW student_class_teacher_school;
DROP TABLE student_class;
DROP TABLE class;
DROP TABLE teacher_school;

DROP VIEW question_in_participation;
DROP TABLE participation_detail;
DROP TABLE participation;

DROP VIEW freezable_question_set;
DROP VIEW question_set_details;
DROP TABLE question_in_set;

DROP VIEW contest_level_details;
DROP TABLE contest_level_i18n;
DROP TABLE contest_level;
DROP TABLE contest_i18n;
DROP TABLE contest;

DROP VIEW freezable_question;
DROP TABLE question_i18n;
DROP TABLE question;

DROP TABLE school;

DROP VIEW student;
DROP TABLE student_user;
DROP TABLE activation;
DROP TABLE general_user;

DROP TABLE lvl;

DROP TABLE config_i18n;
DROP TABLE config;

DROP TABLE globals;

DROP TABLE i18n;
