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

  function roster() {
    require([ "./roster/join" ], function(roster) {
      roster(season, function(content) {
        root.html(content);
      });
    });
  }

  return {
    options: function(options) {
      this.options = options || {};
      return this;
    },
    renderAt: function(container) {
      if (this.options.franchise) {
        team.franchise = this.options.franchise;
        roster();
      } else {
        teamName();
      }
      container.html(root);
    }
  };
  
});