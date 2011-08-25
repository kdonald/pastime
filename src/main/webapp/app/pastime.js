define(["require", "sammy", "dashboard/dashboard", "leagues/leagues", "teams/teams"], function(require, sammy, dashboard, leagues, teams) {

  function start() {    
    var pastime = sammy("#main", function() {
      dashboard(this);
      leagues(this);
      teams(this);
    });
    pastime.run();
  }
  
  return {
    start: start
  };
  
});