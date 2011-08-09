define(["jquery", "mvc", "api"], function($, mvc, api) {
  
  var view = undefined;
  
  function seasonPreview(season) {
    function openJoinNowDialog() {
      var joinNow = function(callback) {
        api.getEligibleFranchises(season, function(franchises) {
          Object.create(mvc.viewPrototype, { 
            model: { value: { season: season, franchise: franchises[0] } },
            template: { value: mvc.template("join", ["jqueryui/dialog"]) },
            events: { value: function() {
              
            }}
          }).render(callback);
        });
      };
      joinNow(function(root) {
        $("<div></div>").html(root).dialog({ title: "Join League", modal: true, width: "auto" });   
      });        
      return false;          
    }
    return Object.create(mvc.viewPrototype, { 
      model: { value: season },
      template: { value: mvc.template("seasonPreview") },
      events: { value: function() {
        this.root.find("#joinNow").click(openJoinNowDialog);        
      }}
    });
  }

  function init(context) {
    api.getSeason({
      state: context.params["state"],
      org: context.params["org"],
      league: context.params["league"],
      year: context.params["year"],
      season: context.params["season"]
    },
    function(season) {
      if (season.preview) {
        view = seasonPreview(season);
      }
      render(context);
    });
  }

  function render(context) {
    view.render(function(root) {
      context.$element().children().detach();
      context.$element().append(root);
    });
  }

  return function(context) {
    if (view === undefined) {
      init(context);
    } else {
      render(context);
    }
  };
      
});