define(["pastime", "jquery", "mvc/view", "text!./add-player.html", "text!./add-player-form.html", "jqueryui-autocomplete-html"],
    function(pastime, $, view, addPlayerTemplate, addPlayerFormTemplate) {

  var addPlayer = function(team) {

    return view.create({
      model: { value: "" },
      template: addPlayerTemplate,
      init: function() {
        var self = this;
        this.$("input").autocomplete({
          html: true,
          source: function(request, response) {
            var xhr = pastime.get(team.links["new_player_search"], {
              name: request.term
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
        var expanded = view.create({
          template: addPlayerFormTemplate,
          events: {
            "submit": function() {
        			pastime.post(team.links["players"], this.model)
        				.done(self.triggerPlayerAdded)
        				.done(function() {
        					this.destroy();
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
        this.expand = function(value) {
          var form = view.extend(expanded, { name: "", email: "" });
          self.collapsed = self.$("form").detach();
          self.root.append(form.render());
          if (/\S+@\S+\.\S+/.test(value)) {
        	  form.model.email = value;
        	  this.$("input[name=name]").focus();
          } else {
        	  form.model.name = value;
        	  this.$("input[name=email]").focus();        	  
          }
        };
        this.triggerPlayerAdded = function(result) {
          this.trigger("player-added", result);
    	}.bind(this);
    	this.clearSelectedPlayer = function() {
    	  delete this.selectedPlayer;
    	}.bind(this);
      },
      events: {
        "submit form": function(event) {
          if (this.selectedPlayer) {
        	  pastime.post(team.links["players"], {
        		  id: this.selectedPlayer.id,
        	  })
        	  .done(this.triggerPlayerAdded)
        	  .done(this.clearSelectedPlayer);
          } else {
            if (this.model.value === "me") {
            	pastime.post(team.links["players"]).done(this.triggerPlayerAdded);
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