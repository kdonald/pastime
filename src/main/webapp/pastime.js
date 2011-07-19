define(["sammy", "dashboard"], function(router, dashboard) {
  
  function start() {
    var pastime = router("#main", function() {
      this.get("/", dashboard);
      this.get("/leagues", function() {
        this.swap("Leagues!");
      });
      this.get("/leagues/:state/:org/:league/:year/:season", function() {
        this.swap(this.params["state"] + " " + this.params["org"] + " " + this.params["league"] + " " + this.params["season"] + " " + this.params["year"]);
      });      
    });
    pastime.run();    
  }
  
  return {
    start: start
  };
  
});