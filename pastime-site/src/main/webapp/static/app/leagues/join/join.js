define(["pastime", "require", "jquery", "mvc", "text!./franchise.html"], function(pastime, require, $, mvc, franchise) {

  var join = function(season) {

    var container = $("<div></div>", {
      id : "join-season"
    });
    
    function start() {
      var xhr = pastime.get(pastime.me.resources["franchises"].url, { league: season.league.id });
      xhr.done(function(franchises) {
        if (franchises.length == 1) {
          container.html(mvc.view({
            model : franchises[0],
            template : franchise,
            events : {
              "change form[input:radio[name=franchise]" : function(event) {
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
          require([ "./join-type" ], function(joinType) {
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
          require([ "./team/team" ], function(team) {
            container.html(team(season, franchise));
          });
        }      
      });
    }

    if (!pastime.me) {
      require([ "./signin/signin" ], function(signin) {
        signin.on("signedin", function() {
          start();
        });
        container.html(signin.render());
      });
    } else {
      start();
    }
    
    return container;
    
  };

  return join;
  
});