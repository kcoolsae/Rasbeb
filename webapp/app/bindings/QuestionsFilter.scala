/* QuestionsFilter.scala
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

import scala.language.implicitConversions

case class QuestionsFilter(externalId: String, title: String)

/**
 * Define an implicit query string binder for this parameter type
 */
object QuestionsFilter {

  implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[QuestionsFilter] {


    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, QuestionsFilter]] = {
      def extract(field: String) : String = {
        (for (s <- stringBinder.bind(key + "." + field, params)) yield {
          s match {
            case Right(value) => value;
            case _ => null
          }
        }).orNull
      }

      Some(Right(QuestionsFilter(
        extract("EXTERNAL_ID"), extract("TITLE") )))
    }

    override def unbind(key: String, sf: QuestionsFilter): String = {
      (sf match {
        case QuestionsFilter(externalId, title) =>
          Seq(("EXTERNAL_ID", externalId), ("TITLE", title))
        case _ => Seq[(String,String)]()
      }).filter( _._2 != null)
        .map (( t:(String,String)) => stringBinder.unbind(key+"."+t._1, t._2)).mkString("&")
    }
  }

}
