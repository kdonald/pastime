define(["require", "jquery", "mvc", "api"], function(require, $, mvc, api) {
  
  var view = undefined;
  
  function seasonPreview(season) {
    function openJoinNowDialog() {
      var joinNow = function(callback) {
        var roster = (function() {
          var players = [];
          var addListeners = [];
          var removeListeners = [];
          function addPlayer(player) {
            players.push(player);
            addListeners.forEach(function(listener) {
              listener(player);
            });
          }
          function removePlayer(player) {
            var index = players.indexOf(player);
            players.splice(index, 1);
            removeListeners.forEach(function(listener) {
              listener(player);
            });
          }
          function playerAdded(callback) {
            addListeners.push(callback);
          }
          function playerRemoved(callback) {
            removeListeners.push(callback);
          }          
          return {
            addPlayer : addPlayer,
            removePlayer : removePlayer,
            playerAdded : playerAdded,
            playerRemoved : playerRemoved
          };
        })();
        api.getEligibleFranchises(season, function(franchises) {
          
          var franchise = franchises.length > 0 ? franchises[0] : undefined; 

          var franchisePlayerView = Object.create(mvc.viewPrototype, {
            template: { value: mvc.template("franchisePlayer") },
            events: { value: function() {
              // TODO consider more declarative event expression that encapsulates require this.root query and this binding 
              this.root.find("span.add").click(function() {
                roster.addPlayer(this.model);
                this.destroy();
              }.bind(this));
            }}
          });

          var franchiseView = Object.create(mvc.viewPrototype, {
            model: { value: { franchise: franchise } },
            template: { value: mvc.template("franchise") },
            events: { value: function() {
              var playerList = this.root.find("#players");
              function addFranchisePlayer(player) {
                Object.create(franchisePlayerView, { model: { value: player } }).render(function(li) {
                  li.appendTo(playerList);
                });                
              }
              function addFranchisePlayers(root) {
                franchise.activePlayers.forEach(function(player) {
                  addFranchisePlayer(player);
                });
              }
              addFranchisePlayers(this.root);
              roster.playerRemoved(function(player) {
                addFranchisePlayer(player);  
              });              
            }}
          });
          
          var rosterEntryView = Object.create(mvc.viewPrototype, {
            template: { value: mvc.template("rosterEntry") },
            events: { value: function() {
              this.root.find("span.remove").click(function() {
                roster.removePlayer(this.model);
                this.destroy();
              }.bind(this));                 
            }}
          });

          var rosterView = Object.create(mvc.viewPrototype, {
            template: { value: mvc.template("roster") },
            events: { value: function() {
              var playerList = this.root.find("#players");
              roster.playerAdded(function(player) {
                Object.create(rosterEntryView, { model: { value: player } }).render(function(playerItem) {
                  playerItem.appendTo(playerList);
                });
              });
              var counter = this.root.find("#summary span.counter");
              roster.playerAdded(function(player) {
                counter.html(parseInt(counter.html()) + 1);
              });
              roster.playerRemoved(function(player) {
                counter.html(parseInt(counter.html()) - 1);
              });                
            }}
          });
                    
          Object.create(mvc.viewPrototype, { 
            model: { value: { season: season } },
            template: { value: mvc.template("join") },
          }).render(function(root) {
            var createRoster = root.find("#createRoster");
            // TODO explore use of deferreds to clean this up
            franchiseView.render(function(element) {
              element.appendTo(createRoster);
              rosterView.render(function(element) {
                element.appendTo(createRoster);                
                callback(root);                
              });
            });
          });
          
        });
      };
      joinNow(function(root) {
        require(["jqueryui/dialog"], function() {
          root.dialog({ title: "Join League", modal: true, width: "auto" });          
        });
      });        
      return false;          
    }
    return Object.create(mvc.viewPrototype, { 
      model: { value: season },
      template: { value: mvc.template("seasonPreview") },
      events: { value: function() {
        this.root.find("#joinNow").click(openJoinNowDialog);        
      }}
    });
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