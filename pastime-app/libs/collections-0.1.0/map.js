define([], function() {

  var mapPrototype = {
     get: function(key) {
      if (!this.data.hasOwnProperty(key)) {
        this.data[key] = this.newValue();
      }
      return this.data[key];
    },
    clear: function() {
      console.log("Clearing data");
      console.log(this.data);
      this.data = {};
      console.log(this.data);      
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