define(["require", "jquery", "mvc"], function(require, $, MVC) {

  var mvc = MVC.create(require);

  var root = $("<div></div>", { 
    id: "register-team" 
  });
  
  var team = {
    name: ""
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

  function roster(league) {
    require([ "./roster/submit" ], function(roster) {
      roster(team, league, function(content) {
        root.html(content);
      });
    });
  }

  return {
    args: function(args) {
      this.args = args || {};
      return this;
    },
    renderAt: function(container) {
      if (this.args.franchise) {
        team.franchise = this.args.franchise;
        roster(this.args.league);
      } else {
        teamName();
      }
      container.html(root);
    }
  };
  
});