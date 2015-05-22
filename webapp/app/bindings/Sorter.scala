/* Sorter.scala
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

package bindings

import play.api.mvc.QueryStringBindable

/**
 * Query parameter with sorting information.
 */
case class Sorter (sortColumn: String, ascending: Boolean)  {
    def cellClass (field: String) : String = if (field==sortColumn) "sorted" else ""

    def columnClass (field: String) : String = {
        if (field == sortColumn) {
            if (ascending) "sorting_asc" else "sorting_desc"
        } else {
          "sorting"
        }
    }

    def forColumn (field: String) : Sorter = {
      if (field == sortColumn) {
         Sorter (sortColumn, ! ascending)
      } else {
         Sorter (field, ascending = true)
      }
    }

}

/**
 * Define an implicit query string binder for this parameter type
 */
object Sorter {

  implicit def queryStringBinder (implicit stringBinder: QueryStringBindable[String],
                    boolBinder: QueryStringBindable[Boolean]) = new QueryStringBindable[Sorter] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Sorter]] = {
      for {
        sortColumn <- stringBinder.bind(key + ".col", params)
        ascending <- boolBinder.bind(key + ".asc", params)
      } yield {
        (sortColumn, ascending) match {
          case (Right(sc), Right(asc)) => Right(Sorter(sc, asc))
          case _ => Left("Unable to bind a Pager")
        }
      }
    }

    override def unbind(key: String, sorter: Sorter): String = {
      stringBinder.unbind(key + ".col", sorter.sortColumn) + "&" + boolBinder.unbind(key + ".asc", sorter.ascending)
    }
  }

}
