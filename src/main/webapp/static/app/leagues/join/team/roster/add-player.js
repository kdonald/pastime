define(["jquery", "mvc", "text!./add-player.html", "text!./add-player-form.html", "jqueryui-autocomplete-html"],
    function($, mvc, addPlayerTemplate, addPlayerFormTemplate) {

  var addPlayer = function(team) {

    return mvc.view({
      model: { value: "" },
      template: addPlayerTemplate,
      init: function() {
        this.$("input").autocomplete({
          html: true,
          source: function(request, response) {
            var xhr = $.getJSON("/me/players", {
              name: request.term,
              franchise: team.franchise
            });
            xhr.done(function(players) {
              response(players);
            });
          },
          select: function(event, ui) {
            console.log(ui.item);
          }
        });
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

  };
  
  return addPlayer;
  
});