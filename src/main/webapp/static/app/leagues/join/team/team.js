define([ "require", "jquery", "mvc" ], function(require, $, MVC) {

  var mvc = MVC.create(require);

  function team(league, franchise) {

    var root = $("<div></div>", {
      id : "register-team"
    });

    var team = {
      name : "",
      franchise : franchise
    };

    function teamName() {
      var teamName = mvc.view({
        model : team,
        template : "team-name",
        events : {
          "submit form" : function() {
            roster();
            return false;
          }
        }
      });
      teamName.renderAt(root);
    }

    function roster() {
      require([ "./roster/submit" ], function(roster) {
        roster(team, league, function(content) {
          root.html(content);
        });
      });
    }

    if (team.franchise) {
      roster();
    } else {
      teamName();
    }
    
    return root;
    
  }

  return team;

});