define(["jquery", "mvc", "./roster", "text!./submit.html", "text!./franchise-players.html", "text!./franchise-player.html", 
        "text!./players.html", "text!./player.html", "./add-player", "./listselect"],
    function($, mvc, Roster, submitTemplate, franchisePlayersTemplate, franchisePlayerTemplate, rosterPlayersTemplate, rosterPlayerTemplate, addPlayer) {

  var submit = function(team, season) {    
    
    var roster = Roster(season.roster_min, season.roster_max);

    var submitRoster = mvc.view({
      model: { season: season },
      template: submitTemplate,
      events: {
        "submit form": function() {
          return false;
        }
      }
    });

    var createRoster = submitRoster.render().find("#createRoster");
    
    if (team.franchise) {
      var franchisePlayers = mvc.view({
        template: franchisePlayersTemplate,
        init: function() {
          var franchisePlayer = mvc.view({
            template: franchisePlayerTemplate,
            events: {
              "click span.select": function() {
                roster.addPlayer(this.model);
                this.destroy();                
              }
            }
          });            
          var playerList = this.$("ul");
          playerList.listselect();          
          function addPlayer(player) {
            playerList.append(mvc.extend(franchisePlayer, player).render());
          }
          roster.playerRemove(function(player) {
            addPlayer(player);  
          });
          function initModel() {
            var setModel = function(franchise) {
              this.model = franchise;
              this.model.players.forEach(function(player) {
                addPlayer(player);
              });              
            }.bind(this);
            $.getJSON("/franchises/" + team.franchise).done(setModel);     
          }
          initModel();
        }
      });
      createRoster.append(franchisePlayers.render());
    }

    var rosterPlayers = mvc.view({
      model: roster,
      template: rosterPlayersTemplate,
      init: function() {
        var rosterPlayer = mvc.view({
          template: rosterPlayerTemplate,
          events: {
            "click span.select": function() {
              roster.removePlayer(this.model);
              this.destroy();
            }          
          }
        });
        var playerList = this.$("ul").listselect();
        roster.playerAdd(function(player) {
          playerList.append(mvc.extend(rosterPlayer, player).render());
        });
      }      
    });

    var addPlayerView = addPlayer(team);
    addPlayerView.on("player-added", function(player) {
      roster.addPlayer(player);
    });
    createRoster.append(rosterPlayers.render()).append(addPlayerView.render());
    
    return submitRoster;
    
  };
  
  return submit;
  
});