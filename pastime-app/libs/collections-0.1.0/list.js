define(["observable"], function(observable) {
    
  var collectionPrototype = {
    add: function(obj) {
      if (this.elements.indexOf(obj) != -1) {
        throw new Error("Element already in list: potential memory leak detected");
      }
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
      return this;
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