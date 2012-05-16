define(["carousel/carousel", "./seasonStorage", "./season"], function(carousel, seasonStorage, seasonView) {
  return carousel({
    title: "Upcoming Seasons Near You",
    items: seasonStorage.upcomingSeasons,
    itemView: seasonView,
    emptyMessage: "None right now."
  });
});