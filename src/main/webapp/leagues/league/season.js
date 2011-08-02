define(["require", "jquery", "handlebars", "api"],
    function(require, $, handlebars, api) {

  function template(file, dependencies) {
    var compiled = undefined;
    return function(context, callback) {
      if (compiled === undefined) {
        require(["text!" + file + ".hb"].concat(dependencies), function(content) {
          compiled = handlebars.compile(content);
          callback(compiled(context));
        });
      } else {
        callback(compiled(context));
      }
    };
  }

  var viewPrototype = (function() {
    function render(callback) {
      if (this.root == undefined) {
        this.template(this.model, function(content) {
          this.root = $(content);
          if (this.eventHandlers) {
            this.eventHandlers.bind(this)();
          }
          callback(this.root);         
        }.bind(this));
      } else {
        callback(this.root);
      }
    }
    return {
      render : render
    };
  })();
  
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
      return Object.create(viewPrototype, { 
        model: { value: season },
        template: { value: template("join", ["jqueryui/dialog"]) },
      });      
    }    
    return Object.create(viewPrototype, { 
      model: { value: season },
      template: { value: template("seasonPreview") },
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
      context.swap(root);
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