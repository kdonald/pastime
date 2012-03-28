define(["jquery", "mvc", "text!./add-player.html", "text!./add-player-form.html", "jqueryui-autocomplete-html"],
    function($, mvc, addPlayerTemplate, addPlayerFormTemplate) {

  var addPlayer = function(team) {

    return mvc.view({
      model: { value: "" },
      template: addPlayerTemplate,
      init: function() {
        var self = this;        
        this.$("input").autocomplete({
          html: true,
          source: function(request, response) {
            var xhr = $.getJSON("/me/players", {
              name: request.term,
              franchise: team.franchise
            });
            xhr.done(function(players) {
              var items = new Array(players.length);
              players.forEach(function(player, i) {
                var fullName = player.first_name + " " + player.last_name;
                items[i] = {};
                items[i].value = fullName;
                items[i].label = "<div class='player'><img src='" + player.link + "/picture'/><strong>" + fullName + "</strong></div>";
                items[i].player = player;
              });
              response(items);
            });
          },
          select: function(event, ui) {
            self.selectedPlayer = ui.item.player;
          }
        });
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
        "submit form": function(event) {
          if (this.selectedPlayer) {
            this.trigger("player-added", this.selectedPlayer);            
          } else {
            this.expand({ email: this.model.value });            
          }
          return false;
        } 
      }
    });

  };
  
  return addPlayer;
  
});