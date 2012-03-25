define([ "require", "jquery", "mvc", "text!./team-name.html" ], function(require, $, mvc, teamNameTemplate) {

  function team(season, franchise) {

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
      var xhr = $.post(season.link + "/teams", team);
      xhr.done(function(id) {
        console.log("new team id:" + id);
        team.id = id;
        require([ "./roster/submit" ], function(roster) {
          container.html(roster(team, season).render());
        });
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