define(["jquery", "pastime-api"], function($, api) {
  
  var templates = (function() {
    function load(path) {
      var content = $.ajax({ url: path, async: false }).responseText;
      return Handlebars.compile(content);
    }
    return {
      newsFeed: load("newsFeed.hb"),
      yourLeagues: load("yourLeagues.hb"),
      watchedLeagues: load("watchedLeagues.hb")          
    };
  })();
  
  function start() {
    $("#newsFeed").html(templates.newsFeed({ articles: api.getNews() }));
    $("#yourLeagues").html(templates.yourLeagues({ leagues: api.getLeagues() }));
    $("#watchedLeagues").html(templates.watchedLeagues({ leagues: api.getWatchedLeagues() }));          
  }
  
  return {
    start: start
  };
  
});