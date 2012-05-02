define(["pastime", "jquery", "mvc/view", "./roster", "text!./submit.html", "text!./franchise-players.html", "text!./franchise-player.html", 
        "text!./players.html", "text!./player.html", "text!./invites.html", "text!./invite.html", "./add-player", "./listselect"],
    function(pastime, $, view, Roster, submitTemplate, franchisePlayersTemplate, franchisePlayerTemplate,
        rosterPlayersTemplate, rosterPlayerTemplate, invitesTemplate, inviteTemplate, addPlayer) {

  var submit = function(team, season) {    
    
    var roster = Roster(season.roster_min, season.roster_max);

    var submitRoster = view.create({
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
      var franchisePlayers = view.create({
        template: franchisePlayersTemplate,
        init: function() {
          var franchisePlayer = view.create({
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
            playerList.append(view.extend(franchisePlayer, player).render());
          }
          roster.on("playerRemoved", function(player) {
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

    var rosterPlayers = view.create({
      model: roster,
      template: rosterPlayersTemplate,
      init: function() {
        var rosterPlayer = view.create({
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
        roster.on("playerAdded", function(player) {
          playerList.append(view.extend(rosterPlayer, player).render());
        });
      }      
    });
    
    var invites = view.create({
      model: roster,
      template: invitesTemplate,
      init: function() {
        var inviteItem = view.create({
          template: inviteTemplate,
          events: {
            "click span.select": function() {
            	var removeInvite = function() {
            		roster.removeInvite(this.model);
            		this.destroy();            	    
            	}.bind(this);
            	pastime.del(this.model.url).done(removeInvite());
            }          
          }
        });
        var inviteList = this.$("ul").listselect();
        roster.on("inviteAdded", function(invite) {
          inviteList.append(view.extend(inviteItem, invite).render());
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
    
    createRoster.append(rosterPlayers.render()).append(invites.render()).append(addPlayerView.render());
    
    return submitRoster;
    
  };
  
  return submit;
  
});