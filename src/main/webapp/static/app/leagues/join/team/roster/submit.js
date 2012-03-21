define(["require", "jquery", "mvc", "./roster", "./listselect"], function(require, $, MVC, Roster) {

  var mvc = MVC.create(require);
  
  var factory = function(team, season, callback) {    
    
    var roster = Roster(season.roster_min, season.roster_max);

    var submitRoster = mvc.view({
      model: { season: season },
      template: "submit",
      events: {
        "submit form": function() {
          return false;
        }
      }
    });

    var createRoster = submitRoster.renderDeferred("#createRoster");
    
    if (team.franchise) {

      var franchisePlayers = mvc.view({
        template: "franchise-players",
        init: function() {
          var franchisePlayer = mvc.view({
            template: "franchise-player",
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
            mvc.extend(franchisePlayer, player).render(function(playerItem) {
              playerList.append(playerItem);
            });
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
      
      createRoster = createRoster.append(franchisePlayers);
        
    }

    var rosterPlayers = mvc.view({
      model: roster,
      template: "players",
      init: function() {
        var rosterPlayer = mvc.view({
          template: "player",
          events: {
            "click span.select": function() {
              roster.removePlayer(this.model);
              this.destroy();
            }          
          }
        });
        var playerList = this.$("ul").listselect();
        roster.playerAdd(function(player) {
          mvc.extend(rosterPlayer, player).render(function(playerItem) {
            playerList.append(playerItem);
          });
        });
      }      
    });

    var addPlayer = mvc.view({
      model: { value: "" },
      template: "add-player",
      init: function() {          
        var self = this;          
        var expanded = mvc.view({
          template: "add-player-form",
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
          mvc.extend(expanded, player).render(function(expanded) {
            self.collapsed = self.$("form").detach();
            self.root.append(expanded);
            this.$("button[type=submit]").focus();                
          });
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

    return createRoster.append(rosterPlayers).append(addPlayer).done(function(content) {
      callback(content);
    });
    
  };
  
  return factory;
  
});