define(["mvc", "text!./join-type.html"], function(mvc, template) {

  var joinType = mvc.view({
    template: template,
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
  
  return joinType;
  
});