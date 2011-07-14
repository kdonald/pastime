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
    $("#newsFeed").html(views.newsFeed({ articles: api.getNews() }));
    $("#yourLeagues").html(views.yourLeagues({ leagues: api.getLeagues() }));
    $("#watchedLeagues").html(views.watchedLeagues({ leagues: api.getWatchedLeagues() }));          
  }
  
  return {
    start: start
  };
  
});