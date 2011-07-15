define(["sammy", "dashboard"], function(sammy, dashboard) {
  
  function start() {
    var pastime = sammy("#main", function() {
      this.get("/", dashboard);
      this.get("/leagues", function() {
        this.swap("Leagues!");
      });
    });
    pastime.run();    
  }
  
  return {
    start: start
  };
  
});