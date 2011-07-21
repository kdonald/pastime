define(["jquery"], function($) {

  var dashboardPrototype = (function() {
    function addNewsItem(newsItem) {
      this.newsItems.unshift(newsItem);
      Array.forEach(this.newsListeners, function(listener) {
        listener.newsItemAdded(newsItem);
      });        
    }
      
    function addNewsListener(listener) {
      this.newsListeners.push(listener);          
    }

    function subscribeForUpdates() {
      window.setTimeout(addNewsItem.bind(this, { id: 4, author: { name: "Brevard Parks and Recreation" }, time: "4:39 PM", body: "<a href=\"leagues/florida/brevardparks/south-football/2011/summer\">South Brevard Adult Flag Football</a> summer league registration is now open." }), 5000);
    }
     
    return { 
      addNewsItem: addNewsItem,
      addNewsListener: addNewsListener,
      subscribeForUpdates: subscribeForUpdates
    };
    
  })();

  function getDashboard(callback) {
    var newsItems = [
      { id: 3, author: { name: "Brevard Parks and Recreation" }, time: "3:39 PM", body: "<a href=\"leagues/florida/brevardparks/south-football/2011/summer\">South Brevard Adult Flag Football</a> summer league registration is now open." },
      { id: 2, author: { name: "Palm Bay Pony Softball" }, time: "July 9", body: "<a href=\"leagues/florida/palm-bay/pony-softball/2011/summer/2#river-bandits-vs-pirates\">River Bandits down Pirates</a> 15-2." },
      { id: 1, author: { name: "Calvary Chapel Basketball" }, time: "July 7", body: " <a href=\"leagues/florida/ccm/basketball/2011/summer/3#red-vs-blue\">Red defeats Blue</a> 49-37." }
    ], yourLeagues = [
      { name: "Calvary Chapel Basketball", path: "leagues/florida/ccm/basketball/2011/summer", sport: "basketball" }
    ], watchedLeagues = [
      { name: "Palm Bay Pony Softball", path: "leagues/florida/palm-bay/pony-softball/2011/summer", sport: "softball" }              
    ];
    var dashboard = Object.create(dashboardPrototype, {
      newsItems: { value: newsItems },
      newsListeners: { value: [] },
      yourLeagues: { value: yourLeagues },
      watchedLeagues: { value: watchedLeagues }
    });
    callback(dashboard);
  }
    
  return {
    getDashboard: getDashboard
  };
  
});