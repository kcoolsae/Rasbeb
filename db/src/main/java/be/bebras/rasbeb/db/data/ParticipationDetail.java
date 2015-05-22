/* ParticipationDetail.java
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

package be.bebras.rasbeb.db.data;

/**
 * Details about the individual questions or a {@link be.bebras.rasbeb.db.data.Participation}-record
 */
public class ParticipationDetail {

    private int participationId;

    private int indexInContest;

    private int indexInParticipation;

    private String answer;

    private Integer marks;

    /**
     * Participation (i.e., contest/user/level) to which these details belong.
     */
    public int getParticipationId() {
        return participationId;
    }

    /**
     * Index of the corresponding question within the question sets of the contest, as in {@link QuestionInSet}
     */
    public int getIndexInContest() {
        return indexInContest;
    }

    /**
     * Sequence number within the participation. Details are numbered consecutively, starting at 1.
     */
    public int getIndexInParticipation() {
        return indexInParticipation;
    }

    /**
     * Answer given to the corresponding question. Represents a number (starting at 0) for multiple choice
     * questions. Null indicates that no answer was (yet) given
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Marks received for this question. Computed when the participation is given status {@link Status#CLOSED}
     */
    public Integer getMarks() {
        return marks;
    }

    public ParticipationDetail(int participationId, int indexInContest, int indexInParticipation, String answer, Integer marks) {
        this.participationId = participationId;
        this.indexInContest = indexInContest;
        this.indexInParticipation = indexInParticipation;
        this.answer = answer;
        this.marks = marks;
    }

}
