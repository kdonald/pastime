define(["require", "jquery", "mvc", "api"], function(require, $, mvc, api) {
  
  var view = undefined;

  function seasonPreview(season) {
    return mvc.view({ 
      model: season,
      template: "seasonPreview",
      events: {
        "click #joinNow": openJoinDialog
      }
    });
    function openJoinDialog() {
      require(["join"], function(join) {
        join(season, function(root) {
          require(["jqueryui/dialog"], function() {
            root.dialog({ title: "Join League", modal: true, width: "auto" });          
          });
        });            
      });          
      return false;
    }    
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