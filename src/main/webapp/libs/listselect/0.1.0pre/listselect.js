define(["jquery"], function($) {

  $.fn.listselect = function() {
    return this.each(function() {
      var list = $(this);
      list.focus(function() {
        list.find("li").first().addClass("selected");            
      });
      list.keydown(function(e) {
        var selected = list.find("li.selected");
        if (e.keyCode === 40) {
          selected.removeClass("selected");
          selected.next().addClass("selected");
          return false;
        } else if (e.keyCode === 38) {
          selected.removeClass("selected");
          selected.prev().addClass("selected");
          return false;              
        } else if (e.keyCode === 13) {
          var next = selected.next();
          selected.find("span.select").trigger("click");
          next.addClass("selected");              
          return false;
        }            
      });
    });    
  };
  
});