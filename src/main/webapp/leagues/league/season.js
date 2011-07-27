define(["require", "jquery", "handlebars", "api"],
    function(require, $, handlebars, api) {

  var season = undefined;

  var seasonPreviewViewLoader = (function() {
    var seasonPreview = undefined;
    var joinNow = undefined;
    return function(callback) {
      
      function render(callback) {
        var view = $(seasonPreview(season));
        registerHandlers(view);
        callback(view);
      }

      function registerHandlers(view) {
        view.find("#joinNow").click(function() {
          function open() {
            $("<div></div>").html(joinNow(season)).dialog({ title: "Join Season" });
          }
          if (joinNow === undefined) { 
            require(["text!join.hb", "jqueryui/dialog"], function(template) {
              joinNow = handlebars.compile(template);
              open();
            });
          } else {
            open();
          }
        });
      }
     
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