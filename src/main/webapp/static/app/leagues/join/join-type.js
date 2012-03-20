define(["require", "mvc"], function(require, MVC) {

  var mvc = MVC.create(require);

  var type = mvc.view({
    template: "join-type",
    init: function() {
      this.handleSelectedType = function(val) {
        if ("Team" === val) {
          this.team();
        } else if ("Free Agent" === val) {
          this.freeAgent();
        }      
      }; 
      this.team = function() {
        this.trigger("team");
      };
      this.freeAgent = function() {
        this.trigger("freeagent");        
      };
      this.handleSelectedType(this.$("form[input:radio[name=type][checked]").val());      
    },
    events: {
      "change form[input:radio[name=type]": function(event) {
        this.handleSelectedType(event.currentTarget.value);
      }      
    }
  });
  
  return type;
  
});