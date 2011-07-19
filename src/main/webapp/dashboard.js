define(["jquery", "handlebars", "api", "text!dashboard.hb", "text!newsItem.hb"],
    function($, handlebars, api, dashboardTemplate, newsItemTemplate) {

  var dashboard = undefined;
 
  var dashboardView = (function() {
    var compiled = handlebars.compile(dashboardTemplate);
    var newsItem = handlebars.compile(newsItemTemplate);
    return function(context) {
      return compiled(context, { partials: { newsItem: newsItem } });        
    };
  })();

  function init() {
    console.log("Initializing dashboard");
    api.getDashboard(function(result) {
      dashboard = result;
    });
    api.subscribeNews(function(newsItem) {
      dashboard.addNewsItem(newsItem);
    });
  }

  function render() {
    console.log("Rendering dashboard");      
    return dashboardView(dashboard);
  }

  return function(context) {
    if (dashboard === undefined) {
      init();
    }
    context.swap(render());    
  };
      
});