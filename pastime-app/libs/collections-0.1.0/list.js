define(["observable"], function(observable) {
    
  var collectionPrototype = {
    add: function(obj) {
      this.elements.push(obj);
      this.trigger("add", obj);
      return this;
    },
    forEach: function(callback) {
      this.elements.forEach(callback);
      return this;
    },
    size: function() {
      return this.elements.length;
    }
  }
  
  return {
    create: function() {
      var collection = Object.create(collectionPrototype, {
        elements: { value: [] }
      });
      return observable(collection);
    }
  }
  
});