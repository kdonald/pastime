define(["jquery", "handlebars", "api", "text!dashboard.hb", "text!newsItem.hb"],
    function($, handlebars, api, dashboardTemplate, newsItemTemplate) {

  var dashboard = undefined;
  
  var newsItemView = handlebars.compile(newsItemTemplate);
  var dashboardView = (function() {
    var compiled = handlebars.compile(dashboardTemplate);
    return function(context) {
      return compiled(context, { partials: { newsItem: newsItemView } });        
    };
  })();

  function init() {
    api.getDashboard(function(result) {
      dashboard = result;
      dashboard.addNewsListener({ 
        newsItemAdded: function(newsItem) {
          $("#newsFeed li:first").before("<li>" + newsItemView(newsItem) + "</li>");
        } 
      });
    });
  }

  function render() {
    return dashboardView(dashboard);
  }

  return function(context) {
    if (dashboard === undefined) {
      init();
    }
    context.swap(render());    
  };
      
});