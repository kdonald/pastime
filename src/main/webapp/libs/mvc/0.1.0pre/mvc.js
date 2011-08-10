define(["require", "jquery", "handlebars"], function(require, $, handlebars) {

  function template(file, options) {
    options = options || {};
    var compiled = undefined;
    var renderOptions = {};
    function render(context, callback) {
      function initRenderOptions() {
        var helpers = Object.create(handlebars.helpers);
        for (helper in options.helpers) {
          helpers[helper] = options.helpers[helper];
        }
        renderOptions.helpers = helpers;
        renderOptions.partials = options.partials;
      }
      if (compiled === undefined) {
        require(["text!" + file + ".hb"].concat(options.dependencies), function(content) {
          compiled = handlebars.compile(content);
          initRenderOptions();
          callback(compiled(context, renderOptions));
        });
      } else {
        callback(compiled(context, renderOptions));
      }
    };
    render.toString = function() {
      return "{ file: " + file + ", options: " + options + "}"; 
    };
    return render;
  }

  var viewPrototype = (function() {
    function render(callback) {
      if (this.root === undefined) {
        this.template(this.model, function(content) {
          this.root = $(content);
          if (this.events) {
            this.events.bind(this)();
          }
          callback(this.root);         
        }.bind(this));
      } else {
        callback(this.root);
      }
    }
    function destroy() {
      this.root.remove();
    }
    function toString() {
      return "{ template: " + this.template + "], model: " + this.model + "}";
    }
    return {
      render: render,
      destroy: destroy,
      toString: toString
    };
  })();

  return {
    template : template,
    viewPrototype : viewPrototype
  };
  
});