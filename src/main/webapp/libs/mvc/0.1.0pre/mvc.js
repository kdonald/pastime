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
            attachEventHandlers(this);
          }
          attachDataBindings(this);
          if (this.init) {
            this.init();
          }
          callback(this.root);         
        }.bind(this));
      } else {
        callback(this.root);
      }
    }
    function renderDeferred() {
      var thisRendered = $.Deferred();
      this.render(function(root) {
        thisRendered.resolve(root);
      });
      function createPromise(deferred) {
        function append(child, insertAt) {
          var childRendered = $.Deferred();
          $.when(deferred).then(function(root) {
            child.render(function(element) {
              root.find(insertAt).append(element);
              childRendered.resolve(root);
            });
          });
          return createPromise(childRendered);            
        }
        function insertAfter(element) {
          $.when(thisRendered).then(function(root) {
            root.insertAfter(element);
          });
          return this;
        }
        return Object.create(deferred.promise(), { 
          append: { value: append },
          insertAfter: { value: insertAfter }
        });        
      };
      return createPromise(thisRendered);
    }    
    function find(element) {
      return this.root.find(element);
    }
    function detach(result) {
      this.root.detach();
      this.postDetachListeners.forEach(function(listener) {
        listener(result);
      });
    }
    function destroy(result) {
      this.root.remove();
      this.postDestroyListeners.forEach(function(listener) {
        listener(result);
      });
    }
    function postDetach(listener) {
      this.postDetachListeners.push(listener);
    }
    function postDestroy(listener) {
      this.postDestroyListeners.push(listener);
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
    function attachDataBindings(view) {
      var bindElements = view.root.find("[data-bind]");
      bindElements.each(function(node) {
        node = $(this);
        var property = node.attr("data-bind");
        var value = view.model[property];
        if (typeof value === "function") {
          var getter = value.bind(view.model);
          node.html(getter());
          view.model.change(function() {
            node.html(getter());            
          });
        }
      });
    }
    
    return {
      renderDeferred: renderDeferred,
      render: render,
      $: find,
      detach: detach,
      postDetach: postDetach,
      destroy: destroy,
      postDestroy: postDestroy,
      toString: toString
    };
  })();
  
  function view(args) {
    return Object.create(viewPrototype, {
      model: { value: args.model },
      template: { value: template(args.template) },
      events: { value: args.events },
      init: { value: args.init },
      postDetachListeners: { value: [] },      
      postDestroyListeners: { value: [] }
    });
  }
  
  function extend(view, model) {
    return Object.create(view, { model: { value: model } });
  }
  
  var Model = (function() {
    var modelPrototype = (function() {
      function get() {
        return this.value;
      }
      function set(value) {
        this.value = value;
        invokeListeners(this.changeListeners);       
      }
      function reset() {
        this.value = "";
      }
      function change(listener) {
        this.changeListeners.push(listener);
      }
      function invokeListeners(listeners) {
        listeners.forEach(function(listener) {
          listener();
        });
      }      
      return {
        get: get,
        set: set,
        reset: reset,
        change: change
      };
    })();      

    return { 
      create: function() {
        Object.create(modelPrototype, {
          value: { value: "" }
        });          
      }
    };
    
  })();
  
  return {
    template: template,
    view: view,
    extend: extend,
    model: Model.create
  };
  
});