define(["jquery", "page"], function($, page) {
  
  function navigate(view, router) {
    var content = view.render();
    if (router.current) {
      router.current.destroy();
    }
    $("body").append(content);
    router.current = view;
  }
  
  function navigator(view, router, handler) {
    if (handler) {
      if (typeof handler === "string") {
        handler = view[handler];
      }
      return function(context) {
        navigate(handler.call(view, context.params), router); 
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
  
  function routeView(view, router) {
    if (typeof view.path === "undefined") {
      throw new Error("Unable to route: view.path not defined");
    }
    page(view.path, navigator(view, router, view.navigate));
    for (var route in view.routes) {
      var handler = view.routes[route];
      page(view.path + "/" + mapSyntax(route), navigator(view, router, handler));
    }
  }
  
  return {
    route: function() {
      if (arguments.length == 1) {
        routeView(arguments[0], this);
      } else {
        page(arguments[0], arguments[1]);
      }
      return this;      
    },
    listen: function() {
      page();
      return this;
    }
  }
  
});