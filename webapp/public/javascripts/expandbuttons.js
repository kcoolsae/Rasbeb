/*
 */

/* Manages the expand buttons. Needs JQuery.

   selector: where to add this functionality, usually "table tr td.expand_button"
   callback: what to fill in in the expanded area, of the form callback($component,id)

*/


function manageExpandButtons(selector,callback) {
    $( selector ).click (function() {
      /* TODO: avoid loading contents twice */
      var $this = $(this);
      var $parentRow = $(this).parent();
      if ($this.attr("class") == "expand_button") {
          $this.attr("class", "collapse_button");
          var evenOdd = $parentRow.attr("class");
          $parentRow.attr("class", "row_selected " + evenOdd);
          var $row = $("<tr class='details_row " + evenOdd +"'><td colspan='10'>...</td></tr>");
          callback($row.children("td"), $this.attr("data-id")) ;
          $row.insertAfter($parentRow);
      } else {
          $this.attr("class", "expand_button");
          $parentRow.attr("class", $parentRow.attr("class").substring(13)); // removes row_selected
          $parentRow.next().remove();
      }
    });
};
