define(["collections/map"], function(map) {
  
  var observablePrototype = (function() {

    function listener(obj) {
      return this;
    }
    
    function on(event, listener) {
      var eventListeners = this.listeners.get(event);
      eventListeners.push(listener);
      return this;
    }

    function off() {
      this.listeners.clear();
      return this;
    }

    function trigger(event, args) {
      var self = this, listeners = this.listeners.get(event);
      listeners.forEach(function(listener) {
        listener.call(self, args);
      });
      return this;
    }
    
    return {
      listener: listener,
      on: on,
      off: off,
      trigger: trigger
    }
    
  })();
  
  return function(obj) {
    obj.listener = observablePrototype.listener;
    obj.on = observablePrototype.on;
    obj.off = observablePrototype.off;
    obj.trigger = observablePrototype.trigger;
    obj.listeners = map.ofArrays();
    return obj;
  }

});