define(["pastime", "jquery", "handlebars",
        "text!./seasonscarousel.html",
        "text!./season.html",
        "jqueryui-carousel/carousel",
        "jqueryui-carousel/autoscroll",
        "jqueryui-carousel/continuous"], function(pastime, $, handlebars, seasonsCarouselTemplate, seasonTemplate) {
  
  seasonsCarouselTemplate = handlebars.compile(seasonsCarouselTemplate);

  handlebars.registerPartial("season", handlebars.compile(seasonTemplate));
  handlebars.registerHelper("formatDate", function(iso) {
    var date = new Date(iso);
    return date.format("MMMM d''x");
  });
  
  $(document).ready(function() {
    var xhr = pastime.get(pastime.links["seasons"]);
    xhr.done(function(seasons) {
      var carousel = $(seasonsCarouselTemplate({
        seasons: seasons
      }));
      carousel.insertAfter($("#upcoming-seasons h2"));
      carousel.carousel({
        itemsPerPage: 1,
        autoScroll: true,        
        continuous: true,
        pagination: false,
        insertPrevAction: function() {
          return $(this).find("a[href='#prev']");
        },        
        insertNextAction: function() {
          return $(this).find("a[href='#next']");
        }
      });      
    });
  });
  
});