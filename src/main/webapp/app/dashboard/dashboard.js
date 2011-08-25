define(["jquery", "handlebars", "api", "text!./dashboard.hb", "text!./newsItem.hb"],
    function($, handlebars, api, dashboardTemplate, newsItemTemplate) {

  newsItemTemplate = handlebars.compile(newsItemTemplate);
  dashboardTemplate = (function() {
    var compiled = handlebars.compile(dashboardTemplate);
    return function(context) {
      return compiled(context, { partials: { newsItem: newsItemTemplate } });      
    };
  })();

  var dashboard = undefined;
  var root = undefined;
  
  function init(context) {
    api.getDashboard(function(obj) {
      dashboard = obj;
      dashboard.addNewsListener({ 
        newsItemAdded: function(newsItem) {
          root.find("#newsFeed li:first").before("<li>" + newsItemTemplate(newsItem) + "</li>");
        } 
      });
      root = $(dashboardTemplate(dashboard));
      render(context);
      dashboard.subscribeForUpdates();
    });
  }

  function render(context) {
    context.$element().children().detach();
    context.$element().append(root);    
  }

  return function(routes) {
    routes.get("/", function(context) {
      if (dashboard === undefined) {
        init(context);
      } else {
        render(context);
      }      
    });
  };
      
});