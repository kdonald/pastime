define([], function() {
    
  var mapPrototype = (function() {

    function get(key) {
      if (!this.data.hasOwnProperty(key)) {
        this.data[key] = this.newValue();
      }
      return this.data[key];
    }
  
    return {
      get: get
    };

  })();

  var observablePrototype = (function() {

    function on(event, listener) {
      this.listeners.get(event).push(listener);
      return this;
    }

    function trigger(event, args) {
      var self = this;
      this.listeners.get(event).forEach(function(listener) {
        listener.call(self, args);
      });
      return this;
    }
 
    return {
      on: on,
      trigger: trigger
    };
    
  })();
  
  return function(obj) {
	  var listeners = Object.create(mapPrototype, {
      data: { value: {} },
      newValue: { value: function() { return []; } }
    });
	  obj.on = observablePrototype.on;
	  obj.trigger = observablePrototype.trigger;
	  obj.listeners = listeners;
	  return obj;
  };
  
});