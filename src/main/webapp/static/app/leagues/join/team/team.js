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
    // temp
    var season = {
      league : {
        name : "South Brevard Adult Flag Football",
        sport : "flag-football",
        quotes : {
          player : "Keith",
          quote : "Fun and competitive"
        },
      },
      name : "Fall 2011 Season",
      startDate : new Date(2011, 08, 06),
      format : "7-on-7",
      games : 16,
      playoffs : true,
      gameDays : [ {
        day : "Wednesday",
        time : "nights"
      } ],
      venues : [ {
        id : 1,
        name : "Max K. Rodes Park",
        address : "3000 Minton Road, Melbourne, FL 32904"
      }, {
        id : 2,
        name : "Palm Bay Regional Park",
        address : "1951 Malabar Rd. NW Palm Bay, FL 32908"
      } ],
      commissioner : {
        name : "Troy Cox"
      },
      referees : [ {
        name : "Bruce"
      }, {
        name : "Al"
      }, {
        name : "Sammy"
      }, ],
      teams : [ {
        id : 1,
        name : "Hitmen"
      }, {
        id : 2,
        name : "Elite Stucco"
      }, {
        id : 3,
        name : "Beef o' Bradys"
      }, {
        id : 4,
        name : "Long Doggers"
      }, {
        id : 5,
        name : "Mean Machine"
      }, ],
      freeAgents : [ {
        id : 1,
        name : "Dale"
      }, {
        id : 2,
        name : "Brandon"
      }, {
        id : 3,
        name : "Jeff"
      } ],
      preview : true
    };
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
        roster();
      } else {
        teamName();
      }
      container.html(root);
    }
  };
  
});