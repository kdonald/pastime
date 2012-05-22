define(["collections/map"], function(map) {

  var observerablePrototype = {
    on: function(event, listener) {
      this.listeners.get(event).push(listener);
      return this;
    },
    off: function() {
      this.listeners.clear();
      return this;
    },
    trigger: function(event, args) {
      var context = this.context, listeners = this.listeners.get(event);
      listeners.forEach(function(listener) {
        listener.call(context, args);
      });
      return this;
    }    
  }
  
  var observer = function(obj) {
    var self = this, observer = self.observers.get(obj);
    return {
      on: function(event, listener) {
        observer.on(event, listener);
        return this;
      },
      off: function() {
        observer.off();
        delete self.observers.data[obj];
        return this;
      }
    }
  }
  
  var on = function(event, listener) {
    return this.observer(this).on(event, listener);
  }
  
  var off = function() {
    for (var observer in this.observers.data) {
      this.observers.data[observer].off();
    }
    return this;
  }
  
  var trigger = function(event, args) {
    for (var observer in this.observers.data) {
      this.observers.data[observer].trigger(event, args);        
    }
    return this;
  }
  
  return function(obj) {
    obj.observer = observer;
    obj.on = on;
    obj.off = off;
    obj.trigger = trigger;
    obj.observers = map.create({
      newValue: function(key) {
        return Object.create(observerablePrototype, {
          listeners: { value: map.ofArrays() },
          context: { value: key }
        });
      }
    });
    return obj;  
  }

});