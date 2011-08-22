define(["require", "./roster", "mvc", "api", "listselect"], function(require, Roster, MVC, api) {
  var mvc = MVC.create(require);
  
  var join = function(season, callback) {  
    var roster = Roster(7, 16);
    
    api.getEligibleTeams(season, function(teams) {
      var team = teams.length > 0 ? teams[0] : undefined;   

      var joinView = mvc.view({
        model: { season: season },
        template: "join",
        events: {
          "submit form": function() {
            this.destroy();
            return false;
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
          this.input = this.$("input");          
          var parent = this;
          this.expand = function(result) {
            mvc.extend(this.expandedViewPrototype, result).render(function(expanded) {
              parent.form = parent.$("form").detach();
              parent.root.append(expanded);
              this.$("button[type=submit]").focus();              
            });
          };          
          this.expandedViewPrototype = mvc.view({
            template: "addNewPlayerForm",
            events: {
              "submit": function() {
                api.inviteUser(this.model, function() {
                  roster.addPlayer(this.model);
                  this.destroy();
                }.bind(this));
                return false;
              },
              "click button.cancel": function() {
                this.destroy();
                return false;
              }
            }
          }).postDestroy(function() {
            parent.model.value = "";
            parent.root.append(parent.form);
          });
        },
        events: {
          "submit form": function() {
            function handleApiResult(result) {
              if (result.exactMatch) {
                roster.addPlayer(result.player);
                this.model.value = "";
              } else {
                this.expand(result);
              }
            }
            api.findUser(this.input.val(), handleApiResult.bind(this));
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
            
      joinView.renderDeferred().append(teamView, "#createRoster").append(addNewPlayerView, "#createRoster").append(rosterView, "#createRoster").done(callback);
      
    });
  };
  
  return join;
      
});