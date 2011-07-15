define(["text!newsFeed.hb", "text!newsItem.hb", "text!yourLeagues.hb", "text!watchedLeagues.hb"], 
    function(newsFeed, newsItem, yourLeagues, watchedLeagues) {

  return {
    newsFeed: newsFeed,
    newsItem: newsItem,
    yourLeagues: yourLeagues,
    watchedLeagues: watchedLeagues
  };
  
});