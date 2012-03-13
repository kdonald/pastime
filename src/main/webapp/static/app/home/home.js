define(["jquery", "handlebars",
        "text!./leaguecarousel.html",
        "text!./leaguepreview.html",
        "jqueryui-carousel/carousel",
        "jqueryui-carousel/autoscroll",
        "jqueryui-carousel/continuous"], function($, handlebars, leagueCarouselTemplate, leaguePreviewTemplate) {

  leagueCarouselTemplate = handlebars.compile(leagueCarouselTemplate);

  handlebars.registerPartial("leaguePreview", handlebars.compile(leaguePreviewTemplate));
  handlebars.registerHelper("formatDate", function(iso) {
    var date = new Date(iso);
    return date.format("MMMM d''x");
  });
  
  $(document).ready(function() {
    $.getJSON('leagues/upcoming', function(leagues) {
      var carousel = $(leagueCarouselTemplate({
        leagues: leagues
      }));
      carousel.insertAfter($("#leaguesNearYou h2"));
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