define(["mvc", "text!./home.html", "upcoming-seasons/upcoming-seasons"], function(mvc, template, upcomingSeasons) {
  return mvc.create({
    path: "/",
    template: template,
    init: function() {
      this.append(upcomingSeasons);
    }
  });      
});