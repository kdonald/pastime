define(["mvc", "text!./home.html", "upcoming-seasons/upcoming-seasons"], function(mvc, template, upcomingSeasons) {
  var view = mvc.create({
    path: "/",
    template: template,
    init: function() {
      this.append(upcomingSeasons);
    }
  });
  console.log(view);
  return view;
});