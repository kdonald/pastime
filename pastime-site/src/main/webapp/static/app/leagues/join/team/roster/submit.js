define(["pastime", "jquery", "mvc", "./roster", "text!./submit.html", "text!./franchise-players.html", "text!./franchise-player.html", 
        "text!./players.html", "text!./player.html", "./add-player", "./listselect"],
    function(pastime, $, mvc, Roster, submitTemplate, franchisePlayersTemplate, franchisePlayerTemplate, rosterPlayersTemplate, rosterPlayerTemplate, addPlayer) {

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
          pastime.get(team.franchise.links[players]).done(function(players) {
            team.franchise.players = players;  
            players.forEach(function(player) {
              addPlayer(player);
            });
          });
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
            	var removePlayer = function() {
            		roster.removePlayer(this.model);
            		this.destroy();            	    
            	}.bind(this);
            	pastime.del(this.model.url).done(removePlayer());
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
    addPlayerView.on("player-added", function(result) {
      var xhr = pastime.get(result.link);
      xhr.done(function(entity) {
        if (result.type === "MEMBER_CONFIRMED") {
        	roster.addPlayer(entity);
        } else {
          roster.addInvite(entity);
        }
      });
    });
    createRoster.append(rosterPlayers.render()).append(addPlayerView.render());
    
    return submitRoster;
    
  };
  
  return submit;
  
});