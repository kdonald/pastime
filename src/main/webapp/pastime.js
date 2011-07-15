define(["jquery", "handlebars", "sammy", "pastime-api", "templates"], function($, templating, app, api, templates) {
  var views = (function() {
    function compile(template) {
      return templating.compile(template);
    }
    var newsItem = compile(templates.newsItem);
    return {
      newsItem: newsItem,
      newsFeed: (function() {
        var view = compile(templates.newsFeed);
        return function(context) {
          return view(context, { partials: { item: newsItem } });        
        };
      })(),
      yourLeagues: compile(templates.yourLeagues),
      watchedLeagues: compile(templates.watchedLeagues),
    };
  })();
  
  function start() {
    var pastime = app(function() {
      function dashboard() {
        function initNews() {
          api.getNews(function(items) {
            $("#newsFeed").html(views.newsFeed({ items: items }));
          });
          api.subscribeNews(function(item) {
            $("#newsFeed li:first").before("<li>" + views.newsItem(item) + "</li>");
          });          
        }
        function initLeagues() {
          api.getLeagues(function(leagues) {
            $("#yourLeagues").html(views.yourLeagues({ leagues: leagues }));
          });
          api.getWatchedLeagues(function(leagues) {
            $("#watchedLeagues").html(views.watchedLeagues({ leagues: leagues }));          
          });          
        }
        initNews();
        initLeagues();
      }
      this.get("/", dashboard);
    });
    pastime.run();    
  }
  
  return {
    start: start
  };
  
});