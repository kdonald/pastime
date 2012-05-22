define([ "observable", "jquery", "handlebars" ], function(observable, $, handlebars) {

  var model = (function() {
    var modelPrototype = (function() {
      function observer() {
        return this;
      }
      function off() {
        this.changeListeners = {};
      }
      function change(property, listener) {
        if (this.changeListeners[property] === undefined) {
          this.changeListeners[property] = [];
        }
        this.changeListeners[property].push(listener);
      }
      return Object.create(Object.prototype, {
        change: { value: change },
        observer: { value: observer },
        off: { value: off }
      });
    })();
    
    function addProperties(model, obj) {
      for (var name in obj) {
        var value = obj[name];
        if (typeof value === "function") {
          addPropertyForGetterFunction(model, obj, name, value);
        } else {
          addProperty(model, obj, name);
        }
      }
    }

    function addPropertyForGetterFunction(model, obj, name, getter) {
      Object.defineProperty(model, name, {
        enumerable: true,
        get: function() {
          return getter.call(obj);
        }
      });
      if (obj.on) {
        obj.on(name, function(value) {
          if (this.changeListeners[name]) {
            this.changeListeners[name].forEach(function(listener) {
              listener(value);
            });
          }
        }.bind(model));
      }
    }

    function addProperty(model, obj, name) {
      Object.defineProperty(model, name, {
        enumerable: true,
        get: function() {
          return obj[name];
        },
        set: function(value) {
          obj[name] = value;
          if (this.changeListeners[name]) {
            this.changeListeners[name].forEach(function(listener) {
              listener(value);
            });
          }
        }
      });
    }
    
    return function(obj) {
      var model = Object.create(modelPrototype, {
        changeListeners: { value: {} }
      });
      addProperties(model, obj);
      return model;
    };
    
  })();

  function template(template, options) {
    var compiled = undefined;          
    var renderOptions = {};
    options = options || {};
    function initRenderOptions() {
      var helpers = Object.create(handlebars.helpers);
      for (var helper in options.helpers) {
        helpers[helper] = options.helpers[helper];
      }
      renderOptions.helpers = helpers;
      renderOptions.partials = options.partials;
    }
    function render(context) {
      if (compiled === undefined) {
        compiled = handlebars.compile(template);
        initRenderOptions();
      }
      return compiled(context, renderOptions);
    }
    render.toString = function() {
      return template;
    };
    return render;
  }
  
  var viewPrototype = (function() {

    function render() {
      if (!this.root) {
        this.root = $(this.template(this.model));
        if (this.events) {
          attachEventHandlers(this);
        }
        if (this.model) {
          attachDataBindings(this);            
        }
        this.subviews = [];          
        if (this.init) {
          this.init();
        }
      }
      return this.root;
    }

    function find(selector) {
      return this.root.find(selector);
    }

    function append(view) {
      this.root.append(view.render());
      this.subviews.push(view);
      var cleanupSubview = function(event) {
        var index = this.subviews.indexOf(view);
        this.subviews.splice(index, 1);
      }.bind(this);
      view.on("destroy", cleanupSubview);    
      return this;
    }
    
    function destroy(result) {
      if (!this.root) {
        return;
      }
      this.subviews.forEach(function(view) {
        view.destroy();
      });
      this.root.remove();
      delete this.root;        
      this.trigger("destroy", result);
      this.off();      
    }

    function toString() {
      return this.template.toString();
    }

    // internal

    function attachEventHandlers(view) {
      for (var eventDesc in view.events) {
        var array = eventDesc.split(" ");
        var event = array[0];
        var source = array[1];
        var handler = view.events[eventDesc].bind(view);
        if (source) {
          view.root.find(source).on(event, handler);
        } else {
          view.root.on(event, handler);
        }
      }
    }

    function attachDataBindings(view) {

      function postProcess(value) {
        return typeof value === "function" ? value.bind(view.model) : value;
      }

      function viewDirection(view, propertyName) {
        view.model.observer(view).change(propertyName, function(newValue) {
          element.val(newValue);
        });
      }
      
      function bindInput(element, view, propertyName, propertyValue) {
        element.val(propertyValue);
        viewDirection(element, propertyName);
        element.change(function() {
          view.model[propertyName] = element.val();
        });
      }
      
      function bindSelect(element, view, propertyName, propertyValue) {
        var optionsSource = element.attr("data-options");
        if (optionsSource) {
          var optionsLoader = view.referenceData[optionsSource], deferred = $.Deferred();
          deferred.done(function(options) {
            options.forEach(function(option) {
              var optionElement = $("<option></option>").attr("value", option.value).append(option.label);
              if (option.value === propertyValue) {
                optionElement.attr("selected", "selected");
              }
              optionElement.appendTo(element);
            });
          });
          optionsLoader(deferred);
        }
      }
      
      function bindList(element, view, propertyName, propertyValue) {
        var appender = view[propertyName + "Appender"] ? view[propertyName + "Appender"] : function(item, list) { list.append(element); }
        function addItem(item) {
          var itemView = create(view[propertyName + "View"], { model: item });
          var li = $("<li/>").attr("data-id", item.id).append(itemView.render());
          appender.call(view, li, element);
        }
        function removeItem(item) {
        }        
        propertyValue.forEach(function(item) {
          addItem(item);
        });
        propertyValue.observer(view).on("add", addItem);
        propertyValue.observer(view).on("remove", removeItem);        
        view.on("destroy", function() {
          propertyValue.observer(view).off();
        }); 
      }
      
      function setupBindings(element) {
        var propertyName = element.attr("data-bind");
        var propertyValue = postProcess(view.model[propertyName]);
        if (element.is("input")) {
          bindInput(element, view, propertyName, propertyValue);
        } else if (element.is("select")) {
          bindSelect(element, view, propertyName, propertyValue);
        } else if (element.is("ul")) {
          bindList(element, view, propertyName, propertyValue);
        } else {
          element.html(propertyValue);
          viewDirection(element, propertyName);
        }
      };

      if (view.root.attr("data-bind")) {
        setupBindings(view.root);
      }
      
      var bindElements = view.root.find("[data-bind]");
      bindElements.each(function() {
        setupBindings($(this));          
      });
      
      view.on("destroy", function(event) {
        view.model.observer(this).off();
      });
      
    }

    return {
      render: render,
      $: find,
      append: append,        
      destroy: destroy,
      toString: toString
    }

  })();

  function props(args) {
    var props = {};
    for (var arg in args) {
      var value = args[arg];
      if (arg === "model") {
        value = model(value);
      } else if (arg === "template") {
        value = template(value, { helpers: args.helpers, partials: args.partials } );
      }
      props[arg] = { value: value }
    }
    return props;
  }
  
  function prototype(args) {
    return Object.create(viewPrototype, props(args));    
  }
  
  function create() {
    var prototype;
    if (arguments.length === 2) {
      prototype = arguments[0];
      args = arguments[1];
    } else {
      prototype = viewPrototype;
      args = arguments[0];
    }
    return observable(Object.create(prototype, props(args)));
  }
  
  return {
    prototype: prototype,
    create: create
  };
    
});