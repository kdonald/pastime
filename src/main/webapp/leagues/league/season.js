define(["require", "jquery", "mvc", "api"], function(require, $, mvc, api) {
  
  var view = undefined;

  function seasonPreview(season) {
    return mvc.view({ 
      model: season,
      template: "seasonPreview",
      events: {
        "click #joinNow": openJoinNowDialog
      }
    });
    function openJoinNowDialog() {
      var joinNow = function(callback) {
        
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
        
        var roster = Object.create(rosterPrototype, {
          minPlayers: { value: 7 },
          maxPlayers: { value: 16 },
          players: { value: [] },
          addListeners: { value: [] },
          removeListeners: { value: [] },
          changeListeners: { value: [] }
        });
        
        api.getEligibleFranchises(season, function(franchises) {
          
          var franchise = franchises.length > 0 ? franchises[0] : undefined; 

          var franchisePlayerView = mvc.view({
            template: "franchisePlayer",
            events: {
              "click span.add": function() {
                roster.addPlayer(this.model);
                this.destroy();                
              }
            }
          });
          
          var franchiseView = mvc.view({
            model: franchise,
            template: "franchise",
            init: function() {
              var playerList = this.root.find("#players");
              function addFranchisePlayer(player) {
                mvc.extend(franchisePlayerView, player).render(function(playerItem) {
                  playerList.append(playerItem);
                });
              }
              this.model.activePlayers.forEach(function(player) {
                addFranchisePlayer(player);
              });
              roster.playerRemove(function(player) {
                addFranchisePlayer(player);  
              });              
            }         
          });
          
          var rosterEntryView = mvc.view({
            template: "rosterEntry",
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
              var playerList = this.root.find("#players");
              roster.playerAdd(function(player) {
                mvc.extend(rosterEntryView, player).render(function(playerItem) {
                  playerList.append(playerItem);
                });
              });
            }
          });
                    
          var joinNowView = mvc.view({ 
            model: { season: season },
            template: "join",
            init: function() {
              mvc.guard(this.root.find("#next"), roster, roster.valid);
            }
          });
          joinNowView.renderDeferred().append(franchiseView, "#createRoster").append(rosterView, "#createRoster").done(callback);
        });
      };
      joinNow(function(root) {
        require(["jqueryui/dialog"], function() {
          root.dialog({ title: "Join League", modal: true, width: "auto" });          
        });
      }); 
      return false;          
    }
    
  }
  
  function init(context) {
    api.getSeason({
      state: context.params["state"],
      org: context.params["org"],
      league: context.params["league"],
      year: context.params["year"],
      season: context.params["season"]
    },
    function(season) {
      if (season.preview) {
        view = seasonPreview(season);
      }
      render(context);
    });
  }

  function render(context) {
    view.render(function(root) {
      context.$element().children().detach();
      context.$element().append(root);
    });
  }

  return function(context) {
    if (view === undefined) {
      init(context);
    } else {
      render(context);
    }
  };
      
});