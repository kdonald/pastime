define(["require", "jquery", "mvc", "./roster", "text!./submit.html", "text!./franchise-players.html", "text!./franchise-player.html", 
        "text!./players.html", "text!./player.html", "text!./add-player.html", "text!./add-player-form.html", "./listselect"],
    function(require, $, mvc, Roster, submitTemplate, franchisePlayersTemplate, franchisePlayerTemplate, rosterPlayersTemplate, rosterPlayerTemplate, addPlayerTemplate, addPlayerFormTemplate) {

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

    var addPlayer = mvc.view({
      model: { value: "" },
      template: addPlayerTemplate,
      init: function() {          
        var self = this;          
        var expanded = mvc.view({
          template: addPlayerFormTemplate,
          events: {
            "submit": function() {
              return false;
            },
            "click button.cancel": function() {
              this.destroy();
              return false;
            }
          }
        }).on("destroy", function() {
          self.model.value = "";
          self.root.append(self.collapsed);            
        });
        this.expand = function(player) {
          self.collapsed = self.$("form").detach();
          self.root.append(mvc.extend(expanded, player).render());
          this.$("button[type=submit]").focus();                
        };
        this.input = this.$("input");          
      },
      events: {
        "submit form": function() {
          this.expand({ email: this.model.value });
          return false;
        } 
      }
    });

    createRoster.append(rosterPlayers.render()).append(addPlayer.render());
    
    return submitRoster;
    
  };
  
  return submit;
  
});