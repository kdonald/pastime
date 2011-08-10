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
          var createRoster = Object.create(mvc.viewPrototype, { 
            model: { value: { season: season, franchise: franchise } },
            template: { value: mvc.template("join") },
            events: { value: function() {
              var rosterEntryView = Object.create(mvc.viewPrototype, {
                template: { value: mvc.template("rosterEntry") },
                events: { value: function() {
                  this.root.find("span.remove").click(function() {
                    roster.removePlayer(this.model);
                    this.destroy();
                  }.bind(this));                 
                }}
              });
              roster.playerAdded(function(player) {
                var rosterList = this.root.find("#roster");
                Object.create(rosterEntryView, { model: { value: player } }).render(function(li) {
                  li.appendTo(rosterList);
                });
              }.bind(this));
              roster.playerAdded(function(player) {
                var counter = this.root.find("#rosterSummary span.counter");
                counter.html(parseInt(counter.html()) + 1);
              }.bind(this));
              roster.playerRemoved(function(player) {
                var counter = this.root.find("#rosterSummary span.counter");
                counter.html(parseInt(counter.html()) - 1);
              }.bind(this));
            }}
          });
          createRoster.render(function(root) {
            function addFranchisePlayers(root) {
              var franchisePlayerList = root.find("#franchisePlayers");              
              var franchisePlayerView = Object.create(mvc.viewPrototype, {
                template: { value: mvc.template("franchisePlayer") },
                events: { value: function() {
                  this.root.find("span.add").click(function() {
                    roster.addPlayer(this.model);
                    this.destroy();
                  }.bind(this));
                }}
              });
              function addFranchisePlayer(player) {
                Object.create(franchisePlayerView, { model: { value: player } }).render(function(li) {
                  li.appendTo(franchisePlayerList);
                });                
              }              
              franchise.activePlayers.forEach(function(player) {
                addFranchisePlayer(player);
              });
              roster.playerRemoved(function(player) {
                addFranchisePlayer(player);  
              });              
            }            
            addFranchisePlayers(root);
            callback(root);
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