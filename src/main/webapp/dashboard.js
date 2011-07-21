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

  function init(context) {
    api.getDashboard(function(obj) {
      dashboard = obj;
      dashboard.addNewsListener({ 
        newsItemAdded: function(newsItem) {
          $("#newsFeed li:first").before("<li>" + newsItemView(newsItem) + "</li>");
        } 
      });
      render(context);
      dashboard.subscribeNews();
    });
  }

  function render(context) {
    return context.swap(dashboardView(dashboard));
  }

  return function(context) {
    if (dashboard === undefined) {
      init(context);
    } else {
      render(context);
    }
  };
      
});