define(["jquery", "handlebars", "pastime-api", "templates"], function($, Handlebars, api, templates) {
  var views = (function() {
    function compile(template) {
      return Handlebars.compile(template);
    }
    function partial(name, template) {
      var compiled = Handlebars.compile(template);
      Handlebars.registerPartial(name, compiled);
      return compiled;
    }
    return {
      newsFeed: compile(templates.newsFeed),
      newsItem: partial("item", templates.newsItem),
      yourLeagues: compile(templates.yourLeagues),
      watchedLeagues: compile(templates.watchedLeagues)          
    };
  })();
  
  function start() {
    api.getNews(function(news) {
        $("#newsFeed").html(views.newsFeed({ items: news }));
    });
    api.getLeagues(function(leagues) {
        $("#yourLeagues").html(views.yourLeagues({ leagues: leagues }));
    });
    api.getWatchedLeagues(function(leagues) {
        $("#watchedLeagues").html(views.watchedLeagues({ leagues: leagues }));          
    });
  }
  
  return {
    start: start
  };
  
});