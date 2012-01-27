({
    baseUrl: "src/main/webapp/static",
    dir: "build/static",
    paths: {
      "facebook": "libs/facebook",
      "handlebars": "libs/handlebars/1.0.0.beta6/handlebars",    
      "jquery": "libs/jquery/1.7.1/jquery",
      "jqueryui": "libs/jqueryui/1.8.16",
      "text": "libs/requirejs-text/1.0.2/text",
      "webshims": "libs/webshims/1.8.7/webshims"
    },   
    modules: [
        {
            name: "common"
        },
        {
            name: "prelaunch/index",
            exclude: ["common"]            
        }
    ]
})