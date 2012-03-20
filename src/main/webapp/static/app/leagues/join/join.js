define([ "require", "jquery", "mvc"], function(require, $, MVC) {

  var mvc = MVC.create(require);
  
  function signedIn() {
    return true;
  }

  $(document).ready(function() {

    var container = $("#main");

    // TODO consider replacing this async call to load the league with an initializer that accepts the league
    function start() {
      var xhr = $.getJSON(document.URL);
      xhr.done(function(league) {
        join(league);
      });
    }
    
    function join(league) {
      var xhr = $.get("/me/franchises", {
        league: league.league_id
      });
      xhr.done(function(franchises) {
        console.log(franchises);
        console.log(franchises.length);
        if (franchises.length > 1) {
          throw Error("not yet implemented");
        } else if (franchises.length == 1) {
          mvc.view({
            model : franchises[0],
            template : "franchise",
            events : {
              "change form[input:radio[name=franchise]" : function(event) {
                var val = event.currentTarget.value;
                if ("yes" === val) {
                  team({ franchise: franchises[0].id });
                } else if ("no" === val) {
                  joinType();
                }
              }
            }
          }).renderAt(container);
        } else {
          joinType();
        }
      });

      function joinType() {
        require([ "./join-type" ], function(joinType) {
          joinType.on("team", function() {
            team();
          });
          joinType.on("freeagent", function() {
            console.log("freeagent join type");
          });
          joinType.renderAt(container);
        });
      }
      
      function team(options) {
        require([ "./team/team" ], function(team) {
          team.options(options).renderAt(container);
        });     
      }
      
    }
    
    if (!signedIn()) {
      require([ "./signin/signin" ], function(account) {
        account.on("signedin", function(id) {
          start();
        });
        account.renderAt(container);
      });
    } else {
      start();
    }

  });

});