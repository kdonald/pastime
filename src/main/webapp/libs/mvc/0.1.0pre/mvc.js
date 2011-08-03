define(["require", "jquery", "handlebars"], function(require, $, handlebars) {

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

  return {
    template : template,
    viewPrototype : viewPrototype
  };
  
});
  
