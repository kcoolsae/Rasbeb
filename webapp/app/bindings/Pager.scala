/* Pager.scala
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

import scala.language.implicitConversions

/**
 * Query parameter with paging information.
 *
 * <p>Note: does not seem to work with ?:-notation in its Java version
 *
 * @see QueryStringBindable
 */
case class Pager(offset: Int, limit: Int) {
  def next : Pager = new Pager(offset+limit,limit)
  def previous : Pager = new Pager(if (offset >= limit) offset-limit else 0, limit)

  def start = offset+1
  def end(count:Int) = scala.math.min(offset+limit,count)
  def hasNext(count:Int) = offset+limit < count
  def hasPrevious = offset > 0

  def first : Pager = new Pager(0,limit)
}

/**
 * Define an implicit query string binder for this parameter type
 */
object Pager {

  implicit def queryStringBinder(implicit intBinder: QueryStringBindable[Int]) = new QueryStringBindable[Pager] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Pager]] = {
      for {
        offset <- intBinder.bind(key + ".offset", params)
        limit <- intBinder.bind(key + ".limit", params)
      } yield {
        (offset, limit) match {
          case (Right(o), Right(l)) => Right(Pager(o, l))
          case _ => Left("Unable to bind a Pager")
        }
      }
    }

    override def unbind(key: String, pager: Pager): String = {
      intBinder.unbind(key + ".offset", pager.offset) + "&" + intBinder.unbind(key + ".limit", pager.limit)
    }
  }
}

