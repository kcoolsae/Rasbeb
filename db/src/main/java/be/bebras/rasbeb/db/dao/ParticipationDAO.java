/* ParticipationDAO.java
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

package be.bebras.rasbeb.db.dao;

import be.bebras.rasbeb.db.data.Participation;
import be.bebras.rasbeb.db.data.ParticipationDetail;
import be.bebras.rasbeb.db.data.Status;

import java.util.List;
import java.util.Map;

/**
 * Access to participations and participation details
 */
public interface ParticipationDAO {

    /**
     * Create a new participation record for the current user
     * @return Id of the newly created record.
     */
    public int createParticipation(int contestId, int level, String lang);
        // TODO: lang not needed as parameter?

    /**
     * Return the participation with the given id.
     */
    public Participation get (int id);

    /**
     * Return the participation details for the given id and index
     */
    public ParticipationDetail get(int id, int index);

    /**
     * Return the number of participation details for the given participation. Only valid
     * for participations that are active or closed
     */
    public int countDetails (int id);

    /**
     * Transfer object for {@link #listQuestionDetails}.
     */
    public static class QuestionDetails {
        public String title;
        public int indexInParticipation;
        public boolean answered;
        public Integer marks;
        public int maximumMarks;
    }

    /**
     * List questions for this participation, by increasing index
     */
    public Iterable<QuestionDetails> listQuestionDetails (int id);

    /**
     * Update the answer stored for a particular question
     */
    public void updateAnswer (int id, int index, String answer);

    /**
     * Close the participation with the given id. Computes the final marks for each question
     * and for the participation as a whole.
     */
    public void closeParticipation (int id);

    /**
     * Find the participation for the current user, contest and level with the given status.
     * Use {@link Status#CLOSED} or {@link Status#DEFAULT} (for running).
     * There should be at most one result.
     * @return the participation, or null when no result
     */
    public Participation findParticipationWithStatus (int contestId, int level, Status status);


    public static class StudentMarks {
        public String name;
        public int studentId;
        public int totalMarks;
        public int maximumMarks;
    }

    /**
     * List the marks obtained by students
     * @param contestId ID of the contest.
     * @param level  Level for which the marks should be listed
     * @param schoolId Only list the students for the given school id.
     * @return Map with class names as keys
     */
    public Map<String,List<StudentMarks>> listStudentMarksForSchool(int contestId, int level, int schoolId);

}
