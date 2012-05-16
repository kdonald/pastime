define(["collections/map"], function(map) {
  
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
    }
    
  })();
  
  return function(obj) {
    obj.on = observablePrototype.on;
    obj.trigger = observablePrototype.trigger;
    obj.listeners = map.ofArrays();
    return obj;
  }

});