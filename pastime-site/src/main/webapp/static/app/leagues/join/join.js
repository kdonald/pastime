define(["pastime", "require", "jquery", "mvc/view", "text!./franchise.html"], function(pastime, require, $, view, franchise) {

  var join = function(season) {

    var container = $("<div></div>", {
      id: "join-season"
    });

    var signin = pastime.signin(container);
    signin.done(function() {
      var xhr = pastime.get(pastime.me.links["franchises"], {
        league: season.league.id
      });
      xhr.done(function(franchises) {
        if (franchises.length == 1) {
          container.html(view.create({
            model: franchises[0],
            template: franchise,
            events: {
              "change form[input:radio[name=franchise]": function(event) {
                var val = event.currentTarget.value;
                if ("yes" === val) {
                  team(franchises[0]);
                } else if ("no" === val) {
                  joinType();
                }
              }
            }
          }).render());
        } else if (franchises.length > 1) {
          throw Error("TODO - not yet implemented");
        } else {
          joinType();
        }

        function joinType() {
          require(["./join-type"], function(joinType) {
            joinType.on("team", function() {
              team();
            });
            joinType.on("freeagent", function() {
              throw Error("TODO - not yet implemented");
            });
            container.html(joinType.render());
          });
        }

        function team(franchise) {
          require(["./team/team"], function(team) {
            container.html(team(season, franchise));
          });
        }
      });
    });

    return container;

  };

  return join;

});