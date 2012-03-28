define(["require", "jquery", "mvc", "api"], function(require, $, MVC, api) {

  var mvc = MVC.create(require), createTeam = undefined;
  
  function init(context) {
    createTeam = mvc.view({
      template: "createTeam",
      model: { name: "", sport: "" },
      referenceData: {
        sports: function(deferred) {
          api.getSports(function(sports) {
            deferred.resolve(sports);
          });
        }    
      },
      events: {
       "submit": function() {
         var self = this;
         api.createTeam(this.model, function(ref) {
           self.trigger("teamCreated", ref);
         });
         return false;
       }
      }
    });
    createTeam.bind("teamCreated", function(ref) {
      window.location.href = ref.location;
    });
    render(context);
  }

  function render(context) {
    createTeam.render(function(root) {
      context.$element().children().detach();
      context.$element().append(root);
    });
  }

  return function(routes) {
    routes.get("/teams", function(context) {
      if (createTeam === undefined) {
        init(context);
      } else {
        render(context);
      }      
    });
    routes.get("/teams/:slug", function(context) {
      api.getTeam(context.params["slug"], function(team) {
        
      });
    });
  };
      
});