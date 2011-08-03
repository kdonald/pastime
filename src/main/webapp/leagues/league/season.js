define(["jquery", "mvc", "api"], function($, mvc, api) {
  
  var season = undefined;
  var view = undefined;

  function createSeasonPreview() {   
    function eventHandlers() {
      var joinNow = createJoinNow();
      this.root.find("#joinNow").click(function() {
        joinNow.render(function(root) {
          $("<div></div>").html(root).dialog({ title: "Join Season" });   
        });
        return false;          
      });
    }
    function createJoinNow() {
      return Object.create(mvc.viewPrototype, { 
        model: { value: season },
        template: { value: mvc.template("join", ["jqueryui/dialog"]) },
      });      
    }    
    return Object.create(mvc.viewPrototype, { 
      model: { value: season },
      template: { value: mvc.template("seasonPreview") },
      eventHandlers: { value: eventHandlers },
    });
  }
  
  function init(context) {
    api.getSeason({ state: context.params["state"], org: context.params["org"], league: context.params["league"], year: context.params["year"], season: context.params["season"] }, function(obj) {
      season = obj;
      if (season.preview) {
        view = createSeasonPreview();
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