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

import com.typesafe.config.ConfigFactory
import db.DataAccess
import java.io.File
import java.util.Date
import play.api.db.DB
import play.api.mvc.{RequestHeader, Handler}
import play.api.{Configuration, Mode, Application, GlobalSettings}

/**
 * Adapts the global settings object to our needs.
 */
object Global extends GlobalSettings {

  lazy val smtpEchoServer = new SmtpEchoServer()

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

    app.mode match {
      case Mode.Dev =>
        DataAccess.setProviderFromDataSource(DB.getDataSource("dev")(app))
        smtpEchoServer.start()
      case Mode.Prod =>
        DataAccess.setProviderFromDataSource(DB.getDataSource("prod")(app))
      case Mode.Test =>
        DataAccess.setProviderForTesting()
    }

  }

  /**
   * Stop the mail server
   */
  override def onStop(app: Application) {
    app.mode match {
      case Mode.Dev =>
        smtpEchoServer.stop()
      case Mode.Prod =>
        // currently nothing needs to be done
      case Mode.Test =>
        // currently nothing needs to be done
    }
  }

  /*
   * Expires the session if not active long enough, otherwise refreshes the session
   * @return
   */
/*
  override def onRouteRequest (request: RequestHeader): Option[Handler] = {
      val old = java.lang.Long.parseLong(request.session("stamp"),16);
      val now = new Date().getTime
      if (now - old > 4800*1000) {
        // 80 minutes no action expires the  session
        super.onRouteRequest(request.session)
      } else {
        request.session.data.("stamp", java.lang.Long.toHexString(now));
      }

      super.onRouteRequest(request)
  }*/


}
