define(["pastime", "jquery", "mvc", "text!./add-player.html", "text!./add-player-form.html", "jqueryui-autocomplete-html"],
    function(pastime, $, mvc, addPlayerTemplate, addPlayerFormTemplate) {

  var addPlayer = function(team) {

    return mvc.view({
      model: { value: "" },
      template: addPlayerTemplate,
      init: function() {
        var self = this;        
        this.$("input").autocomplete({
          html: true,
          source: function(request, response) {
            var xhr = pastime.get(team.links["new_member_search"], {
              name: request.term,
              role: "Player"
            });
            xhr.done(function(players) {
              var items = new Array(players.length);
              players.forEach(function(player, i) {
                var fullName = player.first_name + " " + player.last_name;
                items[i] = {};
                items[i].value = fullName;
                items[i].label = "<div class='player'><img src='" + player.picture + "'/><strong>" + fullName + "</strong></div>";
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
        			var xhr = pastime.post(team.links["members"], this.model);
        			xhr.done(function(player) {
        			  this.destroy();
        				self.trigger("player-added", player);
        			}.bind(this));
              return false;
            },
            "click button.cancel": function() {
              this.destroy();
              return false;
            }
          }
        }).on("destroy", function() {
          self.root.append(self.collapsed);            
        });
        this.expand = function(name) {
          var model = { name: name, email: "", role: "Player" };
          self.collapsed = self.$("form").detach();
          self.root.append(mvc.extend(expanded, model).render());
          this.$("input[name=email]").focus();                
        };
        this.input = this.$("input");          
      },
      events: {
        "submit form": function(event) {
          if (this.selectedPlayer) {
        	  var xhr = pastime.post(team.links["members"], {
        		  id: this.selectedPlayer.id,
        		  role: "Player"
        	  });
        	  xhr.done(function() {
        		  this.trigger("player-added", this.selectedPlayer);
        	  }.bind(this));
          } else {
            if (this.model.value === "me") {
            	pastime.post(team.links["members"], {
            		role: "Player"
            	}).done(function(player) {
            		this.trigger("player-added", player);
            	}.bind(this));
            } else {
            	this.expand(this.model.value);
            }
          }
          return false;
        } 
      }
    }).on("player-added", function() {
    	this.model.value = "";        
    });

  };
  
  return addPlayer;
  
});