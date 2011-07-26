require({ paths: {
    handlebars: "/handlebars/1.0.0.beta3/handlebars",
    jqueryui: "/jqueryui/1.8.14",
    sammy: "/sammy/0.7.0pre/sammy"
  }
}, 
  ["pastime"], function(pastime) {
    pastime.start(); 
  }
);