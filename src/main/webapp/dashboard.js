define(["jquery", "handlebars", "api", "text!dashboard.hb", "text!newsItem.hb"],
    function($, handlebars, api, dashboardTemplate, newsItemTemplate) {

  newsItemTemplate = handlebars.compile(newsItemTemplate);
  dashboardTemplate = (function() {
    var compiled = handlebars.compile(dashboardTemplate);
    return function(context) {
      return compiled(context, { partials: { newsItem: newsItemTemplate } });        
    };
  })();
  
  function dashboard(context) {
    function init() {
      api.getDashboard(function(items) {
        context.swap(dashboardTemplate(items));
      });
      api.subscribeNews(function(item) {
        $("#newsFeed li:first").before("<li>" + newsItemTemplate(item) + "</li>");
      });          
    }
    
    init();
  }
  
  return dashboard;
      
});