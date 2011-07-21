define(["jquery", "handlebars", "api"],
    function($, handlebars, api) {

  var season = undefined;

  var seasonPreviewViewLoader = (function() {
    var view = undefined;
    return function(renderer) {
      console.log(view);
      if (view === undefined) {
        require(["text!seasonPreview.hb"], function(template) {
          console.log(template);
          view = handlebars.compile(template);
          renderer(view);
        });
      } else {
        renderer(view);
      }
    };
  })();

  function init(context) {
    api.getSeason({
      state: context.params["state"],
      org: context.params["org"],
      league: context.params["league"],
      year: context.params["year"],
      season: context.params["season"]
    }, function(obj) {
      season = obj;
      render(context);
    });
  }

  function render(context) {
    if (season.preview) {
      console.log("Rendering");
      seasonPreviewViewLoader(function(view) {
        context.swap(view(season));
      });
    } else {
      context.swap("Season view");
    }
  }

  return function(context) {
    if (season === undefined) {
      init(context);
    } else {
      render(context);
    }
  };
      
});