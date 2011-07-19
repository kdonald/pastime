define(["jquery", "underscore"], function($, _) {
  function getDashboard(callback) {
    function dashboard() {
      var newsItems = [
        { id: 3, author: { name: "Brevard Parks and Recreation" }, time: "3:39 PM", body: "<a href=\"leagues/florida/brevardparks/south-football/2011/summer\">South Brevard Adult Flag Football</a> summer league registration is now open." },
        { id: 2, author: { name: "Palm Bay Pony Softball" }, time: "July 9", body: "<a href=\"leagues/florida/palm-bay/pony-softball/2011/summer/2#river-bandits-vs-pirates\">River Bandits down Pirates</a> 15-2." },
        { id: 1, author: { name: "Calvary Chapel Basketball" }, time: "July 7", body: " <a href=\"leagues/florida/ccm/basketball/2011/summer/3#red-vs-blue\">Red defeats Blue</a> 49-37." }       ];
        
      var yourLeagues = [
        { name: "Calvary Chapel Basketball", path: "leagues/florida/ccm/basketball/2011/summer", sport: "basketball" }
      ];
        
      var watchedLeagues = [
        { name: "Palm Bay Pony Softball", path: "leagues/florida/palm-bay/pony-softball/2011/summer", sport: "softball" }              
      ];

      var newsListeners = [];

      function addNewsItem(newsItem) {
        newsItems.unshift(newsItem);
        _.each(newsListeners, function(listener) {
          listener.newsItemAdded(newsItem);
        });        
      }
        
      function addNewsListener(listener) {
        newsListeners.push(listener);          
      }

      function subscribeNews() {
        function newsGenerator() {
          addNewsItem({ id: 4, author: { name: "Brevard Parks and Recreation" }, time: "4:39 PM", body: "<a href=\"leagues/florida/brevardparks/south-football/2011/summer\">South Brevard Adult Flag Football</a> summer league registration is now open." });
        }
        window.setTimeout(newsGenerator, 5000);
      }
       
      subscribeNews();
      
      return { 
        newsItems : newsItems,
        yourLeagues : yourLeagues,
        watchedLeagues : watchedLeagues,
        addNewsItem : addNewsItem,
        addNewsListener : addNewsListener
      };
    };
    callback(dashboard());
  }
    
  return {
    getDashboard: getDashboard
  };
  
});

