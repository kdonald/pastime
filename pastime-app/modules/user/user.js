define(["mvc", "text!./user.html"], function(mvc, template) {
  return {
    path: "*",
    navigate: function(params) {
      return mvc.create({
        template: template,
        model: {
          user: params[0]
        },
        events: {
          "click": function() {
            console.log("Hi");
          }
        }
      });
    }
  }
});