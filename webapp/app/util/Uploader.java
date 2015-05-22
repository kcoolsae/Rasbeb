/* Uploader.java
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

package util;

import play.Play;

import java.io.File;

/**
 * Can be used to upload a file to the static page server, using a preconfigured upload script
 */
public final class Uploader {

    /**
     * Upload the (ZIP)-file with the given parameters.
     *
     * @return true if and only if the upload succeeded
     */
    public static boolean upload(File file, String magic, String lang, int id) {
        String script = Play.application().configuration().getString("rasbeb.upload.script");
        if (script == null) {
            throw new RuntimeException("No upload script configured (rasbeb.upload.script)");
        }
        ProcessBuilder builder = new ProcessBuilder(
                script, magic, lang, Integer.toString(id)
        );
        builder.redirectInput(file);
        try {
            Process process = builder.start();
            process.waitFor(); // TODO: do this asynchronously?
            return process.exitValue() == 0;
        } catch (Exception ex) {
            throw new RuntimeException("Problems with upload script", ex);
        }
    }

}
