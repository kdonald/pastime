define(["jquery", "handlebars", "api"],
    function($, handlebars, api) {

  var model = undefined;
 
  function init() {
    
  }

  function render(context) {
    return context.params["state"] + " " + context.params["org"] + " " + context.params["league"] + " " + context.params["season"] + " " + context.params["year"]; 
  }

  return function(context) {
    if (model === undefined) {
      init();
    }
    context.swap(render(context));    
  };
      
});