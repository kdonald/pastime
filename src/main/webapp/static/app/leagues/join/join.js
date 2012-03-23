define([ "require", "jquery", "mvc", "text!./franchise.html", "jquery-cookie"], function(require, $, mvc, franchise) {

  function signedIn() {
    return $.cookie("auth_token") != null;
  }

  $(document).ready(function() {

    var join = function(league) {
      var xhr = $.get("/me/franchises", {
        league : league.league_id
      });
      xhr.done(function(franchises) {
        if (franchises.length > 1) {
          throw Error("not yet implemented");
        } else if (franchises.length == 1) {
          container.html(mvc.view({
            model : franchises[0],
            template : franchise,
            events : {
              "change form[input:radio[name=franchise]" : function(event) {
                var val = event.currentTarget.value;
                if ("yes" === val) {
                  team(franchises[0].id);
                } else if ("no" === val) {
                  joinType();
                }
              }
            }
          }).render());
        } else {
          joinType();
        }

        function joinType() {
          require([ "./join-type" ], function(joinType) {
            joinType.on("team", function() {
              team();
            });
            joinType.on("freeagent", function() {
              console.log("freeagent join type");
            });
            container.html(joinType.render());
          });
        }

        function team(franchise) {
          require([ "./team/team" ], function(team) {
            container.html(team(league, franchise));
          });
        }
      });
    };

    function start() {
      // TODO consider passing league in instead of loading here      
      var xhr = $.getJSON(document.URL);
      xhr.done(function(league) {
        join(league);
      });
    }

    var container = $("#main");

    if (!signedIn()) {
      require([ "./signin/signin" ], function(account) {
        account.on("signedin", function(id) {
          start();
        });
        container.html(account.render());
      });
    } else {
      start();
    }

  });

});