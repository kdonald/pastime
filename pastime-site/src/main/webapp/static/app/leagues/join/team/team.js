-define(["pastime", "require", "jquery", "mvc/view", "text!./team-name.html"], function(pastime, require, $, view, teamNameTemplate) {

  function team(season, franchise) {

    var container = $("<div></div>", {
      id: "register-team"
    });

    var team = {
      name: franchise ? franchise.name : null,
      franchise: franchise,
    };

    function teamName() {
      var teamName = view.create({
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
      xhr.done(function() {
        var location = xhr.getResponseHeader("Location");
        pastime.get(location).done(function(team) {
          require(["./roster/submit"], function(roster) {
            container.html(roster(team, season).render());
          });        	
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