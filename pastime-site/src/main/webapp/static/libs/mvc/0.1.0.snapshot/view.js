define([ "./observable", "jquery", "handlebars" ], function(observable, $, handlebars) {

	var model = (function() {
    
		var modelPrototype = (function() {
			function change(property, listener) {
				if (this.changeListeners[property] === undefined) {
					this.changeListeners[property] = [];
				}
				this.changeListeners[property].push(listener);
			}
			return Object.create(Object.prototype, {
				change: { value: change }
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

  var create = (function() {
  
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
        return "{ template: " + template + ", renderOptions: " + renderOptions + "}";
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
          attachDataBindings(this);
          if (this.init) {
            this.init();
          }
        }
        return this.root;
      }
  
      function find(element) {
        return this.root.find(element);
      }
  
      function destroy(result) {
        this.root.remove();
        this.trigger("destroy", result);
      }
  
      function toString() {
        return "{ template: " + this.template + "], model: " + this.model + "}";
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
        
        function bindInput(element, view, propertyName, propertyValue) {
          element.val(propertyValue);
          view.model.change(propertyName, function(newValue) {
            element.val(newValue);
          });
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
                var optionElement = $("<option></option>").attr("value",
                    option.value).append(option.label);
                if (option.value === propertyValue) {
                  optionElement.attr("selected", "selected");
                }
                optionElement.appendTo(element);
              });
            });
            optionsLoader(deferred);
          }
        }
        
        var bindElements = view.root.find("[data-bind]");
        bindElements.each(function(element) {
          var element = $(this);
          var propertyName = element.attr("data-bind");
          var propertyValue = postProcess(view.model[propertyName]);
          if (element.is("input")) {
            bindInput(element, view, propertyName, propertyValue);
          } else if (element.is("select")) {
            bindSelect(element, view, propertyName, propertyValue);
          } else {
            element.html(propertyValue);
            view.model.change(propertyName, function(newValue) {
              element.html(newValue);
            });
          }
        });
      }
  
      return {
        render: render,
        $: find,
        destroy: destroy,
        toString: toString
      };
  
    })();

    return function(args) {
    	return observable(Object.create(viewPrototype, {
    		model: { value: model(args.model) },
    		referenceData : { value: args.referenceData },
    		template: { value: template(args.template) },
    		events: { value: args.events },
    		init: { value: args.init }
    	}));
    };
    
  })();

  var extend = function(view, obj) {
	  return Object.create(view, {
		  model: { value: model(obj) }
	  });
  };

  return {
	  create: create,
	  extend: extend
  };
    
});