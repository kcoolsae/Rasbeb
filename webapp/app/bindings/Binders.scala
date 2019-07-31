/* Binders.scala
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

package bindings

import play.api.mvc.QueryStringBindable
import be.bebras.rasbeb.db.data.Role

/**
 * Defines implicit binders for existing types
 */
object Binders {

  /* not used? No query string parameters of type Role?

  implicit def queryStringBinder = new QueryStringBindable[Role] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Role]] = {
      params.get(key).flatMap(_.headOption) match {
        case Some(string) => Some(Right(Role.valueOf(string)))
        case _ => None
      }
    }

    override def unbind(key: String, role: Role): String = {
      key + "=" + role.name()
    }

  }
  */
}
