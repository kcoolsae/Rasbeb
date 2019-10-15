package util;

import play.api.Configuration;
import play.api.PlayConfig$;
import play.api.libs.mailer.SMTPConfiguration$;
import play.api.libs.mailer.SMTPMailer;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

public class Mail {

    private static MailerClient mailerClient;

    public static void setupMailerClient(Configuration configuration) {
        // hack: cannot be done in Scala because PLayCOnfig is private to play package
        configuration = configuration.getConfig("play.mailer").get();
        //System.err.println("Configuration = " + configuration);
        mailerClient = new SMTPMailer(
                SMTPConfiguration$.MODULE$.apply(
                        PlayConfig$.MODULE$.apply(
                                configuration
                        )));
    }

    public static void sendEmail(String subject, String to, String text) {
        Email email = new Email()
                .setSubject(subject)
                .setCharset("UTF-8")
                .setFrom(Messages.get("mail.noreply.address"))
                .addTo(to)
                .setBodyText(text);
        // Do not use the following! It generates a new connection pool every time, leading to connection
        // problems.
        //MailerClient mailerClient = new GuiceApplicationBuilder().injector().instanceOf(MailerClient.class);
        //System.err.println("Mailer client of class " + mailerClient.getClass());
        mailerClient.send(email);
    }
}
