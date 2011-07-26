define(["jquery", "handlebars", "api"],
    function($, handlebars, api) {

  var season = undefined;

  var seasonPreviewViewLoader = (function() {
    var seasonPreview = undefined;
    function registerHandlers(view) {
      view.find("#joinNow").click(function() {
        console.log("yo!");
      });
    }
    function render(callback) {
      var view = $(seasonPreview(season));
      registerHandlers(view);
      callback(view);
    }
    return function(callback) {
      if (seasonPreview === undefined) {
        require(["text!seasonPreview.hb"], function(template) {
          seasonPreview = handlebars.compile(template);
          render(callback);
        });
      } else {
        render(callback);
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
      seasonPreviewViewLoader(function(view) {
        context.swap(view);
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