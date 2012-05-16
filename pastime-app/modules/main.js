require.config({
  paths: {
    "collections": "../libs/collections-0.1.0",
    "dateformat": "../libs/dateformat-1.0.0",
    "jquery": "../libs/jquery-core-1.7.2",
    "jqueryui": "../libs/jqueryui-1.8.18",
    "jqueryui-carousel": "../libs/rcarousel-1.1.3",    
    "handlebars": "../libs/handlebars-1.0.0.beta6",
    "observable": "../libs/observable-0.1.0",  
    "mvc": "../libs/mvc-0.1.0",
    "path": "../libs/path-0.8.4",
    "router": "../libs/router-0.1.0",
    "text": "../libs/text-1.0.8"
  }
});	  
require(["router", "home/home", "dateformat"], function(router, home) {
  
  function addRoutes() {
    router.route(home);
    router.defaultRoute(home);
  }  
  
  function startListening() {
    $(document).ready(function() {
      router.listen();
    });    
  }
  
  addRoutes();
  startListening();
  
});