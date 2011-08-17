define(["sammy"], function(sammy) {
  
  function start() {
    var pastime = sammy("#main", function() {
      this.get("/", function(context) {
        require(["dashboard"], function(dashboard) {
          dashboard(context);
        });
      });
      this.get("/leagues/:state/:org/:league/:year/:season", function(context) {
        require({baseUrl:"/leagues/league", paths: {
            join: "join/join",
            team: "join/team",
            teamPlayer: "join/teamPlayer",
            roster: "join/roster",
            rosterPlayer: "join/rosterPlayer",
            addNewPlayer: "join/addNewPlayer",
            addNewPlayerForm: "join/addNewPlayerForm"
        }
        }, ["season"], function(season) {
          season(context);
        });
      });      
    });
    pastime.run();    
  }
  
  return {
    start: start
  };
  
});