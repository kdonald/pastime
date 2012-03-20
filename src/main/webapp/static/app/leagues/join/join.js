define(["require", "jquery"], function(require, $) {

  function signedIn() {
    return true;
  }
  
  $(document).ready(function() {
    var main = $("#main");
    
    if (!signedIn()) {
      require(["./signin/signin"], function(account) {
        account.on("signedin", function(id) {
          createTeam(main);
        });
        account.render(function(content) {
          main.html(content);
        });
      });      
    } else {
      createTeam(main);
    }
  });
  
  function createTeam(main) {
    require(["./join-type"], function(joinType) {
      joinType.on("team", function() {
        require(["./team/team"], function(team) {
          main.html(team);
        });
      });
      joinType.on("freeagent", function() {
        console.log("freeagent join type");
      });
      joinType.render(function(content) {
        main.html(content);
      });
      joinType.html(main);
    });
  }
  
});