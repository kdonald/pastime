require.config({
  baseUrl: "/app",
  paths: {
    "jquery": "/libs/jquery/1.7.1/jquery",
    "jqueryui" : "/libs/jqueryui/1.8.16",
    "jqueryuicarousel" : "/libs/jqueryuicarousel/0.8.5"
  }
});
require(["jquery", "jqueryuicarousel/carousel", "jqueryuicarousel/autoscroll", "jqueryuicarousel/continuous"], function($) {
  $(document).ready(function() {
    var el = $('.rs-carousel').carousel();
    el.carousel('option', {itemsPerPage: 1, itemsPerTransition: 1, pagination: false, nextPrevActions: false, autoScroll: true, pause: 4000, continuous: true });
    el.carousel('refresh');
  });
});