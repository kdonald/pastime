define(["jquery", "path"], function($) {
  
  function navigate(view, router) {
    var content = view.render();
    $("body").html(content);
  }
  
  function navigator(view, router, handler) {
    if (handler) {
      if (typeof handler === "string") {
        handler = view[handler];
      }
      return function() {
        navigate(handler.call(view, this.params), router);        
      }
    } else {
      return function() {
        navigate(view, router);        
      }
    }
  }
  
  function mapSyntax(path) {
    return path.replace(/\{/g, ":").replace(/\}/g, "");
  }
  
  return {
    route: function(view) {
      if (typeof view.path === "undefined") {
        throw new Error("Unable to route: view.path not defined");
      }
      Path.map("#" + view.path).to(navigator(view, this));
      for (var route in view.routes) {
        var handler = view.routes[route];
        Path.map("#" + view.path + "/" + mapSyntax(route)).to(navigator(view, this, handler));
      }
      return this;
    },
    defaultRoute: function(view) {
      Path.root("#" + view.path);
      return this;
    },
    listen: function() {
      Path.listen();
      return this;
    }
  }
  
});