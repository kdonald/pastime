define(["jquery", "handlebars", "api", "text!dashboard.hb", "text!newsItem.hb"],
    function($, handlebars, api, dashboardTemplate, newsItemTemplate) {

  newsItemTemplate = handlebars.compile(newsItemTemplate);
  dashboardTemplate = (function() {
    var compiled = handlebars.compile(dashboardTemplate);
    return function(context) {
      return compiled(context, { partials: { newsItem: newsItemTemplate } });      
    };
  })();

  var dashboard = undefined;

  function init(context) {
    api.getDashboard(function(obj) {
      dashboard = obj;
      dashboard.addNewsListener({ 
        newsItemAdded: function(newsItem) {
          $("#newsFeed li:first").before("<li>" + newsItemTemplate(newsItem) + "</li>");
        } 
      });
      render(context);
      dashboard.subscribeForUpdates();
    });
  }

  function render(context) {
    var view = $(dashboardTemplate(dashboard));
    return context.swap(view);
  }

  return function(context) {
    if (dashboard === undefined) {
      init(context);
    } else {
      render(context);
    }
  };
      
});