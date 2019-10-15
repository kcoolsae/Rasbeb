/* Global.scala
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
 * along with the Rasbeb Web Application (file LICENSE in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

import db.DataAccess
import play.api.db.DB
import play.api.libs.mailer.{SMTPConfiguration, SMTPMailer}
import play.api.{Application, GlobalSettings, Mode}
import play.libs.mailer.MailerClient
import util.Mail

/**
 * Adapts the global settings object to our needs.
 */
object Global extends GlobalSettings {

  //  lazy val smtpEchoServer = new SmtpEchoServer()

  // we have chosen the Scala version because the Java version does not have a Mode parameter
  // in onLoadConfig. Bug?


  /**
   * Merge additional config files into the application, depending on the current mode
   */

  /* No longer used: config file must now be given as startup parameter
  override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {

    // see http://stackoverflow.com/questions/9723224/how-to-manage-application-conf-in-several-environments-with-play-2-0

    config ++ Configuration(ConfigFactory.load(mode.toString.toLowerCase + ".conf"))
  }
  */

  /**
   * Initialize the data access provider for this application
   * and the mail server
   */
  override def onStart(app: Application) {

    Mail.setupMailerClient(app.configuration);

    app.mode match {
      case Mode.Dev =>
        DataAccess.setProviderFromDataSource(DB.getDataSource("dev")(app))
      // smtpEchoServer.start() // using mock=yes functionality of play.mailer
      case Mode.Prod =>
        DataAccess.setProviderFromDataSource(DB.getDataSource("prod")(app))
      case Mode.Test =>
        DataAccess.setProviderForTesting()
    }

  }

  /**
   * Stop the mail server
   */
  /*
    override def onStop(app: Application) {
      app.mode match {
        case Mode.Dev =>
          // smtpEchoServer.stop() // TODO use mock=yes functionality of play.mailer
        case Mode.Prod =>
        // currently nothing needs to be done
        case Mode.Test =>
        // currently nothing needs to be done
      }
    }
  */


}
