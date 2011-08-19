define(["require", "mvc", "api"], function(require, MVC, api) {

  var mvc = MVC.create(require);
  
  var join = function(season, callback) {  
    var Roster = (function() {
      var rosterPrototype = (function() {
        function addPlayer(player) {
          this.players.push(player);
          this.addListeners.forEach(function(listener) {
            listener(player);
          });
          invokeChangeListeners(this.changeListeners, this.playerCount());  
        }
        function removePlayer(player) {
          var index = this.players.indexOf(player);
          this.players.splice(index, 1);
          this.removeListeners.forEach(function(listener) {
            listener(player);
          });
          invokeChangeListeners(this.changeListeners, this.playerCount());
        }
        function playerCount() {
          return this.players.length;
        }
        function valid() {
          return this.playerCount() >= this.minPlayers && this.playerCount() <= this.maxPlayers;
        }         
        function change(property, listener) {
          this.changeListeners.push(listener);
        }
        function playerAdd(listener) {
          this.addListeners.push(listener);
        }
        function playerRemove(listener) {
          this.removeListeners.push(listener);
        }
        function invokeChangeListeners(listeners, value) {
          listeners.forEach(function(listener) {
            listener(value);
          });
        }
        return Object.create(Object.prototype, {
          addPlayer: { value: addPlayer },
          removePlayer: { value: removePlayer },
          playerCount: { value: playerCount, enumerable: true },
          valid: { value: valid },
          playerAdd: { value: playerAdd },
          playerRemove: { value: playerRemove },
          change: { value: change },
        });
      })();
      
      return { 
        create: function(minPlayers, maxPlayers) {
          return Object.create(rosterPrototype, {
            minPlayers: { value: minPlayers, enumerable: true },
            maxPlayers: { value: maxPlayers, enumerable: true },
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
          // TODO make this reusable
          playerList.focus(function() {
            playerList.find("li").first().addClass("selected");            
          });
          playerList.keydown(function(e) {
            var selected = playerList.find("li.selected");
            if (e.keyCode === 40) {
              selected.removeClass("selected");
              selected.next().addClass("selected");
              return false;
            } else if (e.keyCode === 38) {
              selected.removeClass("selected");
              selected.prev().addClass("selected");
              return false;              
            } else if (e.keyCode === 13) {
              var next = selected.next();
              selected.find("span.add").trigger("click");
              next.addClass("selected");              
              return false;
            }            
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
          addNewPlayerView.renderDeferred().insertAfter(rosterSummary);

          var addNewPlayerFormView = mvc.view({
            template: "addNewPlayerForm",
            events: {
              "submit form": function() {
                api.inviteUser(this.model, function() {
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