/* Fixtures.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright (C) 2015 Universiteit Gent
 * 
 * This file is part of the Rasbeb project, an interactive web
 * application for Bebras competitions.
 * 
 * Corresponding author:
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Rasbeb Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Rasbeb Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package be.bebras.rasbeb.db.jdbc;

import be.bebras.rasbeb.db.DataAccessContext;
import be.bebras.rasbeb.db.dao.*;
import be.bebras.rasbeb.db.data.ContestType;
import be.bebras.rasbeb.db.data.Role;

/**
 * Provides several methods that initialize the database for testing
 */
public final class Fixtures {

    public static int[] createQuestions(DataAccessContext context) {

        QuestionDAO dao = context.getQuestionDAO();
        int[] ids = new int[]{
                dao.createQuestion(4, "Q0001"),
                dao.createQuestion(4, "Q0002"),
                dao.createQuestion(0, "Q1202"),
                dao.createQuestion(0, "Q1345")
        };

        dao.updateQuestionI18n(ids[0], "en", "Hello Bebras", "C");
        dao.updateQuestionI18n(ids[1], "en", "Bebras NG", "D");
        dao.updateQuestionI18n(ids[2], "en", "Do not run", null);
        dao.updateQuestionI18n(ids[3], "en", "Chicken run", "answer3");

        dao.setFeedbackUploaded(ids[0], "en");
        dao.setQuestionUploaded(ids[0], "en");
        dao.setFeedbackUploaded(ids[2], "en");
        dao.setQuestionUploaded(ids[2], "en");

        return ids;
    }

    public static int[] createContests(DataAccessContext context) {
        ContestDAO dao = context.getContestDAO();
        int[] result = new int[]{
                dao.createContest(ContestType.OFFICIAL),
                dao.createContest(ContestType.PUBLIC)
        };

        dao.updateContestI18n(result[0], "en", "English title 0");
        dao.updateContestI18n(result[1], "en", "English title 1");
        dao.updateContestI18n(result[0], "fr", "French title 0");
        dao.updateContestI18n(result[1], "fr", "French title 1");

        return result;
    }

    public static int[] createQuestionSets(DataAccessContext context, int contestId, int[] questionIds) {
        QuestionSetDAO dao = context.getQuestionSetDAO();
        int[] indices = new int[]{
                dao.addQuestionInSet(contestId, questionIds[0]),
                dao.addQuestionInSet(contestId, questionIds[1]),
                dao.addQuestionInSet(contestId, questionIds[2]),
                dao.addQuestionInSet(contestId, questionIds[3]),
        };

        dao.updateQuestionInSet(contestId, 1, indices[0], 3, -1);
        dao.updateQuestionInSet(contestId, 1, indices[1], 4, -2);
        dao.updateQuestionInSet(contestId, 2, indices[2], 5, -3);
        dao.updateQuestionInSet(contestId, 4, indices[3], 6, -4);

        return indices;
    }

    public static int[] createTeachers(DataAccessContext context) {
        UserDAO dao = context.getUserDAO();
        return new int[]{
                dao.createUser("teacher1@gmail.com", Role.TEACHER, "Bob"),
                dao.createUser("teacher2@gmail.com", Role.TEACHER, "Alice"),
                dao.createUser("teacher3@gmail.com", Role.TEACHER, "John"),
                dao.createUser("teacher4@gmail.com", Role.TEACHER, "Mary"),
                dao.createUser("teacher5@gmail.com", Role.TEACHER, "Clark"),
        };
    }

    public static int[] createStudents(DataAccessContext context) {
        UserDAO dao = context.getUserDAO();
        return new int[]{
                dao.createStudent("student1@gmail.com", "Doe, John", true, "initial1"),
                dao.createStudent("student2@gmail.com", "Doe, Jane", false, "initial2"),
                dao.createStudent(null, "Atkins, Thomas", true, "initial3"),
        };
    }

    public static int[] createSchools(DataAccessContext context) {
        SchoolDAO dao = context.getSchoolDAO();
        return new int[]{
                dao.createSchool("Old school", "Old street", "1705", "Old town", 2013),
                dao.createSchool("School of Theology", "Street of small gods", "1230", "Chinatown", 2014),
                dao.createSchool("School of Arts", "Paris Avenue", "1200", "Jerusalem", 2014),
                dao.createSchool("Royal institute", "Street of cunning artificers", "1700", "Downtown Chicago", 2014),
                dao.createSchool("Saint Michael", "Saint Michael Square", "1001", "Rivendel", 2014),
        };
    }

    public static int[] createTeacherSchools(DataAccessContext context, int[] teacherIds, int[] schoolIds) {
        TeacherSchoolClassDAO dao = context.getTeacherSchoolClassDAO();
        return new int[] {
            dao.createTeacherSchool(teacherIds[0], schoolIds[1]),
            dao.createTeacherSchool(teacherIds[1], schoolIds[3]),
            dao.createTeacherSchool(teacherIds[1], schoolIds[1]),
            dao.createTeacherSchool(teacherIds[2], schoolIds[2]),
            dao.createTeacherSchool(teacherIds[2], schoolIds[3]),
            dao.createTeacherSchool(teacherIds[3], schoolIds[1]),
            dao.createTeacherSchool(teacherIds[1], schoolIds[0]),
            dao.createTeacherSchool(teacherIds[2], schoolIds[0]),
            dao.createTeacherSchool(teacherIds[4], schoolIds[4])
        };
    }

    public static int[] createClasses (DataAccessContext context, int[] schoolIds) {
        TeacherSchoolClassDAO dao = context.getTeacherSchoolClassDAO();
        return new int[] {
                dao.createClassInSchool(schoolIds[1], "3a"),
                dao.createClassInSchool(schoolIds[1], "3b"),
                dao.createClassInSchool(schoolIds[3], "4a-we"),
                dao.createClassInSchool(schoolIds[3], "4a-ec"),
                dao.createClassInSchool(schoolIds[2], "3a"),
                dao.createClassInSchool(schoolIds[3], "3b"),
                dao.createClassInSchool(schoolIds[3], "3c"),
        };
    }

    public static void createStudentClasses (DataAccessContext context, int[] studentIds, int[] classIds) {
        TeacherSchoolClassDAO dao = context.getTeacherSchoolClassDAO();
        dao.addStudentToClass(studentIds[0], classIds[1]);
        dao.addStudentToClass(studentIds[1], classIds[1]);
        dao.addStudentToClass(studentIds[2], classIds[4]);
    }

    public static int[] createLocalContests (DataAccessContext context, int[] contestIds, int[] schoolIds) {
        LocalContestDAO dao = context.getLocalContestDAO();
        return new int[] {
                dao.createLocalContestInSchool(contestIds[0], 2, "en", schoolIds[1], "Next thursday"),
                dao.createLocalContestInSchool(contestIds[1], 4, "en", schoolIds[1], "Next friday"),
                dao.createLocalContestInSchool(contestIds[1], 4, "en", schoolIds[3], "Next monday"),
                dao.createLocalContestInSchool(contestIds[0], 4, "en", schoolIds[1], "Never"),
        };
    }

    public static int[] createParticipations (DataAccessContext context, int[] contestIds) {
        ParticipationDAO dao = context.getParticipationDAO();
        int[] pids = new int[4];
        // uses current user id!
        pids[3] = dao.createParticipation(contestIds[0], 1, "en");
        context.setUserId(3);
        pids[0] = dao.createParticipation(contestIds[0], 2, "en");
        context.setUserId(4);
        pids[1] = dao.createParticipation(contestIds[0], 1, "en");
        context.setUserId(5);
        pids[2] = dao.createParticipation(contestIds[1], 1, "en");

        dao.updateAnswer(pids[3], 1, "C");
        dao.updateAnswer(pids[3], 2, "B");
        dao.closeParticipation(pids[3]);

        return pids;
    }

}
