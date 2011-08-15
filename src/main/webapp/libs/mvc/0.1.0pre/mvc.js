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
    function renderDeferred() {
      var thisRendered = $.Deferred();
      this.render(function(root) {
        thisRendered.resolve(root);
      });
      function createPromise(deferred) {
        function append(child, insertAt) {
          var childRendered = $.Deferred();
          $.when(thisRendered).then(function(root) {
            child.render(function(element) {
              root.find(insertAt).append(element);
              childRendered.resolve(root);
            });
          });
          return createPromise(childRendered);            
        }
        return Object.create(deferred.promise(), { 
          append: { value: append }
        });        
      };
      return createPromise(thisRendered);
    }
    function render(callback) {
      if (this.root === undefined) {
        this.template(this.model, function(content) {
          this.root = $(content);
          if (this.events) {
            attachEventHandlers(this);
          }
          if (this.init) {
            this.init();
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
    function attachEventHandlers(view) {
      for (eventDesc in view.events) {
        var array = eventDesc.split(" ");
        var event = array[0];
        var source = array[1];
        var handler = view.events[eventDesc];
        view.root.find(source).bind(event, handler.bind(view));
      }        
    }
    
    return {
      renderDeferred: renderDeferred,
      render: render,
      destroy: destroy,
      toString: toString
    };
  })();
  
  function view(args) {
    return Object.create(viewPrototype, {
      model: { value: args.model },
      template: { value: template(args.template) },
      events: { value: args.events },
      init: { value: args.init }
    });
  }
  
  function extend(view, model) {
    return Object.create(view, { model: { value: model } });
  }
  
  function guard(button, obj, constraint) {
    constraint = constraint.bind(obj);
    button.attr("disabled", !constraint());
    obj.change(function() {
      button.attr("disabled", !constraint());
    });
  }

  return {
    template: template,
    view: view,
    extend: extend,
    guard: guard
  };
  
});