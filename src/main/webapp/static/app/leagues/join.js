define(["require", "jquery", "mvc"], function(require, $, MVC) {

  var mvc = MVC.create(require);
  
  function signedIn() {
    return true;
  }
  
  $(document).ready(function() {
    var main = $("#main");
    
    if (!signedIn()) {
      require(["./signin"], function(account) {
        account.on("signedin", function(id) {
          createTeam();
        });        
        account.html($("#main"));
      });      
    } else {
      createTeam(main);
    }
  });
  
  function createTeam(main) {
    require(["./join-type"], function(joinType) {
      joinType.on("team", function() {
        var teamName = mvc.view({
          template: "team-name"
        });
        teamName.html(main);
      });
      joinType.on("freeagent", function() {
        console.log("freeagent join type");
      });
      joinType.html(main);
    });
  }
  
});