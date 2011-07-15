define(["sammy", "dashboard"], function(sammy, dashboard) {
  
  function start() {
    var pastime = sammy("#main", function() {
      this.get("/", dashboard);
    });
    pastime.run();    
  }
  
  return {
    start: start
  };
  
});