/* SmtpEchoServer.java
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

import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mail server that echos all mails it receives to the standard output channel. For use in
 * testing. Runs on localhost:2525 by default.
 */
public class SmtpEchoServer extends Wiser {

    /** */
    private SMTPServer server;

    /**
     * Create a new SMTP server with this class as the listener.
     * Call setPort()/setHostname() before
     * calling start().
     */
    public SmtpEchoServer() {
        super();
        setPort(2525);
        setHostname("localhost");
    }

    /**
     * Instead of 'delivering' the message, print it out.
     */
    @Override
    public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
        // adds it to the list of messages
        super.deliver(from, recipient, data);

        // remove it from that list and print it
        printMessage(messages.remove(messages.size() - 1), System.out);

    }

    /**
     * Print out the message
     */
    public void printMessage(WiserMessage message, PrintStream out) {
        out.println("===== New message =====");

        out.println("Envelope sender: " + message.getEnvelopeSender());
        out.println("Envelope recipient: " + message.getEnvelopeReceiver());

        try {
            MimeMessage mm = message.getMimeMessage();
            mm.writeTo(out);
        } catch (IOException | MessagingException ex) {
            out.println("*** ERROR: EXCEPTION THROWN ***\n" + ex);
        }

        out.println("===== End message =====\n");
    }


    /**
     * A main() for this class. Starts up the server.
     */
    public static void main(String[] args) throws Exception {
        new SmtpEchoServer().start();
    }

}
