define(["jquery"], function($) {

  var dashboardPrototype = (function() {
    function addNewsItem(newsItem) {
      this.newsItems.unshift(newsItem);
      this.newsListeners.forEach(function(listener) {
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
      { id: 3, author: { name: "Brevard Parks and Recreation" }, time: "3:39 PM", body: "<a href=\"leagues/florida/brevardparks/south-football/2011/summer\">South Brevard Adult Flag Football</a> fall league registration is now open." },
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
  
  function getSeason(season, callback) {
    var seasonPreview = {
      league: {
        name: "South Brevard Adult Flag Football",
        sport: "flag-football",
        quotes: { player: "Keith", quote: "Fun and competitive" }, 
      },
      name: "Fall 2011 Season",
      startDate: new Date(2011, 08, 06), 
      format: "7-on-7",
      games: 16,
      playoffs: true,
      gameDays: [
        { day: "Wednesday", time: "nights" }
      ],
      venues: [
        { id: 1, name: "Max K. Rodes Park", address: "3000 Minton Road, Melbourne, FL 32904" },
        { id: 2, name: "Palm Bay Regional Park", address: "1951 Malabar Rd. NW Palm Bay, FL 32908" }
      ],
      commissioner: { name: "Troy Cox" }, 
      referees: [
        { name: "Bruce" },
        { name: "Al" },
        { name: "Sammy" }, 
      ],
      teams: [
        { id: 1, name: "Hitmen"},
        { id: 2, name: "Elite Stucco"},
        { id: 3, name: "Beef o' Bradys"},
        { id: 4, name: "Long Doggers"},
        { id: 5, name: "Mean Machine"},       
      ],
      freeAgents: [
        { id: 1, name: "Dale" },
        { id: 2, name: "Brandon" },
        { id: 3, name: "Jeff" }
      ],
      preview: true
    };
    callback(seasonPreview);
  }
    
  function getEligibleFranchises(openSeason, callback) {
    var franchises = [{
      name: "Hitmen",
      path: "/franchises/florida/brevard/hitmen",
      activePlayers: [ 
        { name: "Brian Fisher"},
        { name: "Alexander Weaver"},
        { name: "Keith Donald"}
      ]
    }];
    callback(franchises);
  }
  
  return {
    getDashboard: getDashboard,
    getSeason: getSeason,
    getEligibleFranchises: getEligibleFranchises
  };
  
});