define(["require", "./roster", "mvc", "./api", "./listselect"], function(require, Roster, MVC, api) {
  var mvc = MVC.create(require);
  
  var join = function(season, callback) {  
    var roster = Roster(7, 16);
    
    api.getEligibleTeams(season, function(teams) {
      var team = teams.length > 0 ? teams[0] : undefined;   
      
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
          playerList.listselect();
        }
      });

      var teamPlayerView = mvc.view({
        template: "teamPlayer",
        events: {
          "click span.select": function() {
            roster.addPlayer(this.model);
            this.destroy();                
          }
        }
      });
      
      var addNewPlayerView = mvc.view({
        model: { value: "" },
        template: "addNewPlayer",
        init: function() {          
          var self = this;          
          var expandedView = mvc.view({
            template: "addNewPlayerForm",
            events: {
              "submit": function() {
                var successHandler = function() {              
                  roster.addPlayer(this.model);
                  this.destroy();
                }.bind(this);
                api.inviteUser(this.model, successHandler);
                return false;
              },
              "click button.cancel": function() {
                this.destroy();
                return false;
              }
            }
          }).postDestroy(function() {
            self.model.value = "";
            self.root.append(self.originalForm);            
          });
          this.expand = function(result) {
            mvc.extend(expandedView, result).render(function(expandedForm) {
              self.originalForm = self.$("form").detach();
              self.root.append(expandedForm);
              this.$("button[type=submit]").focus();                
            });
          };
          this.input = this.$("input");          
        },
        events: {
          "submit form": function() {
            var successHandler = function(result) {
              if (result.exactMatch) {
                roster.addPlayer(result.player);
                this.model.value = "";
              } else {
                this.expand(result);
              }
            }.bind(this);
            api.findUser(this.input.val(), successHandler);
            return false;
          } 
        }
      });

      var rosterView = mvc.view({
        model: roster,
        template: "roster",
        init: function() {
          var playerList = this.$("ul").listselect();
          roster.playerAdd(function(player) {
            mvc.extend(rosterPlayerView, player).render(function(playerItem) {
              playerList.append(playerItem);
            });
          });
        }      
      });

      var rosterPlayerView = mvc.view({
        template: "rosterPlayer",
        events: {
          "click span.select": function() {
            roster.removePlayer(this.model);
            this.destroy();
          }          
        }
      });

      var joinDiv = $('<div id="join"></div>');
      
      var submitRosterView = mvc.view({
        model: { season: season },
        template: "submitRoster",
        events: {
          "submit form": function() {
            this.destroy();
            confirmView.render(function(content) {
              joinDiv.append(content);
            });
            return false;
          }
        }
      });
      
      var confirmView = mvc.view({
        template: "confirm"
      });
      
      submitRosterView.renderDeferred("#createRoster").append(teamView).append(addNewPlayerView).append(rosterView).done(function(content) {
        callback(joinDiv.append(content));
      });
      
    });
  };
  
  return join;
      
});