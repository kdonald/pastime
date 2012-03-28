define(["require", "sammy", "home/home", "leagues/leagues", "teams/teams"], function(require, sammy, home, leagues, teams) {

  function start() {    
    var pastime = sammy("#main", function() {
      home(this);
      leagues(this);
      teams(this);
    });
    pastime.run();
  }
  
  return {
    start: start
  };
  
});