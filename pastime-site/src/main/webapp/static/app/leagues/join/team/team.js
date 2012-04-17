define(["pastime", "require", "jquery", "mvc", "text!./team-name.html"], function(pastime, require, $, mvc, teamNameTemplate) {

  function team(season, franchise) {

    var container = $("<div></div>", {
      id: "register-team"
    });

    var team = {
      name: franchise ? franchise.name : null,
      franchise: franchise,
    };

    function teamName() {
      var teamName = mvc.view({
        model: team,
        template: teamNameTemplate,
        events: {
          "submit form": function() {
            roster();
            return false;
          }
        }
      });
      container.html(teamName.render());
    }

    function createTeam() {
      var data = {
        name: team.name
      };
      if (team.franchise) {
        data.franchise = team.franchise.id;
      }
      return pastime.post(season.links["teams"], data);
    }

    function roster() {
      var xhr = createTeam();
      xhr.done(function(created) {
        // TODO read Location header and make a get request for team data
        team.id = created.id;
        team.links = created.links;
        require(["./roster/submit"], function(roster) {
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