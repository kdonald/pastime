define([ "require", "jquery", "mvc", "text!./team-name.html" ], function(require, $, mvc, teamNameTemplate) {

  function team(league, franchise) {

    var container = $("<div></div>", {
      id : "register-team"
    });

    var team = {
      name : "",
      franchise : franchise
    };

    function teamName() {
      var teamName = mvc.view({
        model : team,
        template : teamNameTemplate,
        events : {
          "submit form" : function() {
            roster();
            return false;
          }
        }
      });
      container.html(teamName.render());
    }

    function roster() {
      require([ "./roster/submit" ], function(roster) {
        container.html(roster(team, league).root);
      });
    }

    if (team.franchise) {
      roster();
    } else {
      teamName();
    }
    
    return container;
    
  }

  return team;

});