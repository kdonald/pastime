define(["mvc", "text!./season.html"], function(mvc, template) {
  return mvc.create({
    template: template,
    helpers: {
      formatDate: function(iso) {        
        return Date.parseIso(iso).format("MMMM d''x", { asUtc: true });
      }
    }
  });  
});