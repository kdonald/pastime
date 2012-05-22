define([], function() {

  var mapPrototype = {
     get: function(key) {
      if (!this.data.hasOwnProperty(key)) {
        this.data[key] = this.newValue(key);
      }
      return this.data[key];
    },
    clear: function() {
      for (var key in this.data) {
        delete this.data[key];
      }
    }
  }

  return {
    create: function(args) {
      return Object.create(mapPrototype, {
        data: { value: {} },
        newValue: { value: args.newValue }
      });
    },
    ofArrays: function() {
      return this.create({
        newValue: function() { return []; }      
      });
    }
  }
  
});