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
    
  function getEligibleTeams(openSeason, callback) {
    var teams = [{
      name: "Hitmen",
      path: "/teams/florida/brevard/hitmen",
      activePlayers: [ 
        { id: 1, name: "Brian Fisher", picture: "http://localhost:8080/users/1/picture" },
        { id: 2, name: "Alexander Weaver", picture: "http://localhost:8080/users/2/picture" },
        { id: 3, name: "Keith Donald", picture: "http://localhost:8080/users/3/picture" },
        { id: 4, name: "Marc Szczesny-Pumarada", picture: "http://localhost:8080/users/4/picture" },
        { id: 5, name: "Gabe Barfield", picture: "http://localhost:8080/users/5/picture" },
        { id: 6, name: "Joe Petrone", picture: "http://localhost:8080/users/6/picture" },
        { id: 7, name: "Jason Barry", picture: "http://localhost:8080/users/7/picture" },
        { id: 8, name: "Matthew Wade", picture: "http://localhost:8080/users/8/picture"},
        { id: 9, name: "Kelvin Zhang", picture: "http://localhost:8080/users/9/picture" },        
        { id: 10, name: "Stephen Tomko", picture: "http://localhost:8080/users/10/picture" },        
      ]
    }];
    callback(teams);
  }
  
  function findUser(name, callback) {
    var exactMatch = false;
    var friendMatches = false;
    if (exactMatch) {
      callback({
        exactMatch: true,
        player: {
          id: 11,
          name: "David Murray",
          picture: "http://localhost:8080/users/11/picture"
        }
      });      
    } else if (friendMatches) {
      callback({
        exactMatch: false,
        friendMatches: [
          { 
            id: 1255689239,
            name: "Keith Donald",
            picture: "http://graph.facebook.com/keith.donald/picture",          
          }
        ]
      });
    } else {
      callback({
        exactMatch: false,
        name: name,
        email: "",
      });
    }
  }
  
  function inviteUser(user, callback) {
    callback();
  }
  
  function createTeam(args, callback) {
    callback({ location: "http://localhost:8080/#/teams/hitmen", slug: "hitmen" });
  }
  
  function getSports(callback) {
    sports = [ { value: 0, label: "Flag football" }, { value: 1, label: "Softball" } ];
    callback(sports);
  }
  
  function getTeam(slug, callback) {
    var team = {
      name: "Hitmen",
      logo: "http://localhost:8080/teams/hitmen/logo",
      stories: [
       { date: "2011/9/19 8:00 AM", title: "Hitmen to Face Long Doggers in Round 1 of Playoffs", author: "Analyst Bot", picture: "http://localhost:8080/teams/hitmen/news/2011/09/19/playoffs/thumbnail.png", summary: "Hitmen took the first meeting 23-16 and will need to do it again to advance.", article: "http://localhost:8080/teams/hitmen/news/2011/09/19/hitmen-to-face-long-doggers-in-round-1-of-playoffs" },
       { date: "2011/9/14 11:05 PM", title: "Hitmen Edge Out Elite Stucco on Late Game Heorics", author: "Analyst Bot", picture: "http://localhost:8080/teams/hitmen/news/2011/09/14/elite-stucco/thumbnail.png", summary: "Marc P. threw for 6 touchdowns and Dale Heinz scored twice.", article: "http://localhost:8080/teams/hitmen/news/2011/09/14/hitmen-edge-out-elite-stucco-on-late-game-heroics"  },
       { date: "2011/9/14 11:00 PM", title: "Hitmen Blow Out Prestige Worldwide", author: "Analyst Bot", picture: "http://localhost:8080/teams/hitmen/news/2011/09/14/prestiege-worldwide/thumbnail.png", summary: "Keith Donald scored the game winning TD and Dale Heinz got the game winning INT.", article: "http://localhost:8080/teams/hitmen/news/2011/09/14/hitmen-blow-out-prestiege-worldwide"  },
      ],
      schedule: [
       { date: "2011/9/14 7:00 PM", opponent: { name: "Prestige Worldwide", ref: "http://localhost:8080/leagues/florida/brevardparks/south-football/2011/summer/5/1" }, result: { code: "W", score: 40, opponent: 0 } },
       { date: "2011/9/14 8:00 PM", opponent: { name: "Elite Stucco", ref: "http://localhost:8080/leagues/florida/brevardparks/south-football/2011/summer/5/2" }, result: { code: "W", score: 18, opponent: 3 } },
       { date: "2011/9/21 6:45 PM", opponent: { name: "Long Doggers", ref: "http://localhost:8080/leagues/florida/brevardparks/south-football/2011/summer/6/1" }, playoffs: true }
      ],
    };
    callback(team);
  }
  
  return {
    getDashboard: getDashboard,
    getSeason: getSeason,
    getEligibleTeams: getEligibleTeams,
    findUser: findUser,
    inviteUser: inviteUser,
    createTeam: createTeam,
    getSports: getSports,
  };
  
});