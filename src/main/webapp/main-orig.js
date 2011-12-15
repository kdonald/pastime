require.config({
  baseUrl: "/app",
  paths: {
    "jquery": "/libs/jquery/1.7.1/jquery",
    "handlebars": "/libs/handlebars/1.0.0.20111108/handlebars",
    "mvc": "/libs/mvc/0.1.0pre/mvc",
    "jqueryui" : "/libs/jqueryui/1.8.16",
    "jqueryuicarousel" : "/libs/jqueryuicarousel/0.8.5",
    "sammy": "/libs/sammy/0.7.0/sammy",
    "listselect": "/libs/listselect/0.1.0pre/listselect"
  }
});
require(["pastime"], function(pastime) {
  pastime.start(); 
});