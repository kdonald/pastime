require({ 
  baseUrl: "/app",
  paths: {
	jquery: "/libs/jquery/1.7b2/jquery",
    handlebars: "/libs/handlebars/1.0.0.beta3/handlebars",
    mvc: "/libs/mvc/0.1.0pre/mvc",
    jqueryui: "/libs/jqueryui/1.8.14",
    sammy: "/libs/sammy/0.7.0/sammy",
    listselect: "/libs/listselect/0.1.0pre/listselect"
  }
}, 
  ["pastime"], function(pastime) {
    pastime.start(); 
  }
);