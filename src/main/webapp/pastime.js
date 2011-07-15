define(["jquery", "handlebars", "pastime-api", "text!newsFeed.hb", "text!yourLeagues.hb", "text!watchedLeagues.hb"], 
    function($, Handlebars, api, newsFeedTemplate, yourLeaguesTemplate, watchedLeaguesTemplate) {

  var views = (function() {
    function compile(template) {
      return Handlebars.compile(template);
    }
    return {
      newsFeed: compile(newsFeedTemplate),
      yourLeagues: compile(yourLeaguesTemplate),
      watchedLeagues: compile(watchedLeaguesTemplate)          
    };
  })();
  
  function start() {
    api.getNews(function(news) {
        $("#newsFeed").html(views.newsFeed({ articles: news }));
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