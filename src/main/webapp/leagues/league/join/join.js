define(["require", "jquery", "mvc", "api"], function(require, $, mvc, api) {

  var join = function(season, callback) {
       
    var Roster = (function() {
      var rosterPrototype = (function() {
        function addPlayer(player) {
          this.players.push(player);
          this.addListeners.forEach(function(listener) {
            listener(player);
          });
          invokeListeners(this.changeListeners);           
        }
        function removePlayer(player) {
          var index = this.players.indexOf(player);
          this.players.splice(index, 1);
          this.removeListeners.forEach(function(listener) {
            listener(player);
          });
          invokeListeners(this.changeListeners);            
        }
        function playerCount() {
          return this.players.length;
        }
        function valid() {
          return this.playerCount() >= this.minPlayers && this.playerCount() <= this.maxPlayers;
        }         
        function change(listener) {
          this.changeListeners.push(listener);
        }
        function playerAdd(listener) {
          this.addListeners.push(listener);
        }
        function playerRemove(listener) {
          this.removeListeners.push(listener);
        }
        function invokeListeners(listeners) {
          listeners.forEach(function(listener) {
            listener();
          });
        }
        return {
          addPlayer: addPlayer,
          removePlayer: removePlayer,
          playerCount: playerCount,
          valid: valid,
          playerAdd: playerAdd,
          playerRemove: playerRemove,
          change: change,
        };
      })();
      
      return { 
        create: function(minPlayers, maxPlayers) {
          return Object.create(rosterPrototype, {
            minPlayers: { value: minPlayers },
            maxPlayers: { value: maxPlayers },
            players: { value: [] },
            addListeners: { value: [] },
            removeListeners: { value: [] },
            changeListeners: { value: [] }
          });          
        }
      };
    })();
    
    var roster = Roster.create(7, 16);
    
    api.getEligibleTeams(season, function(teams) {
      
      var team = teams.length > 0 ? teams[0] : undefined; 

      var teamPlayerView = mvc.view({
        template: "teamPlayer",
        events: {
          "click span.add": function() {
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
        }         
      });
      
      var rosterPlayerView = mvc.view({
        template: "rosterPlayer",
        events: {
          "click span.remove": function() {
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
              "click button": function() {
                function handleApiResult(result) {
                  if (result.exactMatch) {
                    roster.addPlayer(result.player);
                    this.model.reset();
                  } else {
                    this.detach(result);
                  }
                }
                api.findUser(this.input.val(), handleApiResult.bind(this));
                return false;
              } 
            }
          });
          addNewPlayerView.renderDeferred().insertAfter(rosterSummary);

          var addNewPlayerFormView = mvc.view({
            template: "addNewPlayerForm",
            events: {
              "click button.add": function() {
                api.inviteUser(this.model, function() {
                  roster.addPlayer(this.model);
                  this.destroy();
                }.bind(this));
                return false;
              },
              "click button.cancel": function() {
                this.destroy();
                return false;
              }
            }
          });

          addNewPlayerView.postDetach(function(result) {
            mvc.extend(addNewPlayerFormView, result).renderDeferred().insertAfter(rosterSummary);
          });
          
          addNewPlayerFormView.postDestroy(function() {
            addNewPlayerView.renderDeferred().insertAfter(rosterSummary);  
          });

          roster.playerAdd(function(player) {
            mvc.extend(rosterPlayerView, player).render(function(playerItem) {
              playerList.append(playerItem);
            });
          });
          
        }
      
      });
                
      var joinView = mvc.view({
        model: { season: season },
        template: "join"
      });
      
      joinView.renderDeferred().append(teamView, "#createRoster").append(rosterView, "#createRoster").done(callback);
      
    });
  };
  
  return join;
      
});