({
    appDir: "src/main/webapp/static",
    baseUrl: "app",
    dir: "build/static",
    paths: {
      "handlebars": "../libs/handlebars/1.0.0.beta6/handlebars",    
      "jquery": "../libs/jquery/1.7.1/jquery",
      "text": "../libs/requirejs-text/1.0.2/text",
      "textselect": "../libs/textselect/1.0.0/textselect",
      "polyfiller": "../libs/webshims/1.8.7/polyfiller"
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