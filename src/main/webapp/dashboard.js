define(["jquery", "handlebars", "api", "text!newsFeed.hb", "text!newsItem.hb", "text!yourLeagues.hb", "text!watchedLeagues.hb"],
    function($, handlebars, api, newsFeedTemplate, newsItemTemplate, yourLeaguesTemplate, watchedLeaguesTemplate) {
      
  function dashboard() {
    
    var views = (function() {
      function compile(template) {
        return handlebars.compile(template);
      }
      var newsItem = compile(newsItemTemplate);
      return {
        newsItem: newsItem,
        newsFeed: (function() {
          var view = compile(newsFeedTemplate);
          return function(context) {
            return view(context, { partials: { item: newsItem } });        
          };
        })(),
        yourLeagues: compile(yourLeaguesTemplate),
        watchedLeagues: compile(watchedLeaguesTemplate),
      };
    })();
    
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
  
  return dashboard;
      
});