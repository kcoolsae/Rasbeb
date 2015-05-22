/* Table.scala
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

package controllers

import play.mvc.Call
import bindings.{Sorter, Pager}
import play.api.templates.Html

/**
 * Backend object for data tables with sort buttons and paging buttons and an enclosing form
 * (e.g., for filtering).
 *
 * Implemented in Scala to allow for methods with multiple parameter lists.
 * Can however be extended by Java classes
 */
abstract class Table {

  /** Route to be used when the 'previous' button is clicked */
  def previous : Call

  /** Route to be used when the 'previous' button is clicked */
  def next : Call

  /**
   * Route to be used when the 'sort' button is clicked for the given field
   * @param field Uppercase string name of the corresponding enumerated type value for the field
   */
  def sort(field: String): Call

  /** Route to be used when the 'resize' button is clicked */
  def resize: Call

  /** Route to be used when one of the button forms is clicked (e.g., the search button) */
  def action: Call

  /** Return a pager for this table (can be null) */
  def pager: Pager

  /** Return a pager for this table (can be null) */
  def sorter: Sorter

  /**
   * Generate html for the table preamble.
   */
  def pre = views.html.snippets.tables.pre(this) _

  /**
   * Generate html for the form containing the main part of the table and the part following it
   */
  def form = views.html.snippets.tables.form(this) _

  /**
   * Generate html for the main part of the table
   */
  def main(count: Int) = views.html.snippets.tables.main(this,count) _

 /**
   * Generate html for the part following the main part of the table
   */
  def post(count: Int) = views.html.snippets.tables.post(this,count)


  /**
   * Generate html for a sortable column header
   */
  def column(field: String) = views.html.snippets.tables.column(this,field) _

  /**
   * Generate html for a cell in a sortable column
   */
  def cell(field: String)= views.html.snippets.tables.cell(this,field) _

}
