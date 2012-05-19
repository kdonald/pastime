define(["collections/map"], function(map) {
  
  var observablePrototype = (function() {

    function listener(obj) {
      return this;
    }
    
    function on(event, listener) {
      var eventListeners = this.listeners.get(event);
      eventListeners.push(listener);
      if ("destroy" === event) {
        console.log(this + ":" + event + ": " + eventListeners.length);        
      }
      return this;
    }

    function off() {
      this.listeners = map.ofArrays();
      return this;
    }

    function trigger(event, args) {
      var offs = [], self = this, listeners = this.listeners.get(event);
      listeners.forEach(function(listener) {
        var off = function() {
          console.log("off called");
          offs.push(listener);
        }
        listener.call(self, { off: off, args: args });
      });
      offs.forEach(function(listener) {
        var index = listeners.indexOf(listener);
        console.log("removing listener " + index);
        listeners.splice(index, 1);
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