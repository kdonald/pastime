define(["jquery"], function($) {

  $.fn.listselect = function() {
    return this.each(function() {
      var list = $(this);
      list.focus(function() {
        list.find("li").first().addClass("selected");            
      });
      list.blur(function() {
        list.find("li.selected").removeClass("selected");
      });
      list.keydown(function(e) {
        var selected = list.find("li.selected");
        if (e.keyCode === 40) {
          var next = selected.next();          
          if (next.is("li")) {  
            selected.removeClass("selected");
            next.addClass("selected");
          }
          return false;
        } else if (e.keyCode === 38) {
          var prev = selected.prev();          
          if (prev.is("li")) {  
            selected.removeClass("selected");
            prev.addClass("selected");
          }
          return false;              
        } else if (e.keyCode === 13) {
          var prev = selected.prev();
          var next = selected.next();
          selected.find("span.select").trigger("click");
          if (next.is("li")) {  
            next.addClass("selected");
          } else if (prev.is("li")) {
            prev.addClass("selected");
          }
          return false;
        }            
      });
    });    
  };
  
});