define(["require"], function(require) {

  return function(routes) {
    routes.get("/leagues/:state/:org/:league/:year/:season", function(context) {
      require(["./season"], function(season) {
        season(context);
      });
    });
  };
      
});