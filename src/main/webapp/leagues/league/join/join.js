define(["require", "./roster", "mvc", "api"], function(require, Roster, MVC, api) {
  var mvc = MVC.create(require);
  
  var join = function(season, callback) {  
    var roster = Roster(7, 16);
    
    api.getEligibleTeams(season, function(teams) {
      var team = teams.length > 0 ? teams[0] : undefined;   
      var teamPlayerView = mvc.view({
        template: "teamPlayer",
        events: {
          "click span.select": function() {
            roster.addPlayer(this.model);
            this.destroy();                
          }
        }
      });
      
      var teamView = mvc.view({
        model: team,
        template: "team",
        init: function() {
          var playerList = this.$("ul");
          function addPlayer(player) {
            mvc.extend(teamPlayerView, player).render(function(playerItem) {
              playerList.append(playerItem);
            });
          }
          this.model.activePlayers.forEach(function(player) {
            addPlayer(player);
          });
          roster.playerRemove(function(player) {
            addPlayer(player);  
          });
          playerList.listselect();
        }
      });
      
      var rosterPlayerView = mvc.view({
        template: "rosterPlayer",
        events: {
          "click span.select": function() {
            roster.removePlayer(this.model);
            this.destroy();
          }          
        }
      });

      var rosterView = mvc.view({
        model: roster,
        template: "roster",
        init: function() {
          var playerList = this.$("ul");
          var rosterSummary = this.$("#rosterSummary");
          
          var addNewPlayerView = mvc.view({
            model: { value: "" },
            template: "addNewPlayer",
            init: function() {
              this.input = this.$("input");              
            },
            events: {
              "submit form": function() {
                function handleApiResult(result) {
                  if (result.exactMatch) {
                    roster.addPlayer(result.player);
                    this.model.value = "";
                  } else {
                    this.detach(result);
                  }
                }
                api.findUser(this.input.val(), handleApiResult.bind(this));
                return false;
              } 
            }
          });

          var addNewPlayerFormView = mvc.view({
            template: "addNewPlayerForm",
            events: {
              "submit form": function() {
                api.inviteUser(this.model, function() {
                  console.log(this.model);
                  roster.addPlayer(this.model);
                  this.destroy();
                }.bind(this));
                return false;
              }
            }
          });

          addNewPlayerView.postDetach(function(result) {
            mvc.extend(addNewPlayerFormView, result).renderDeferred().insertAfter(rosterSummary);
          });
          
          addNewPlayerFormView.postDestroy(function() {
            addNewPlayerView.model.value = "";
            addNewPlayerView.renderDeferred().insertAfter(rosterSummary);  
          });

          roster.playerAdd(function(player) {
            mvc.extend(rosterPlayerView, player).render(function(playerItem) {
              playerList.append(playerItem);
            });
          });

          playerList.listselect();
          addNewPlayerView.renderDeferred().insertAfter(rosterSummary);          
        }      
      });
                
      var joinView = mvc.view({
        model: { season: season },
        template: "join"
      });
      
      require(["listselect"]);
      
      joinView.renderDeferred().append(teamView, "#createRoster").append(rosterView, "#createRoster").done(callback);
      
    });
  };
  
  return join;
      
});