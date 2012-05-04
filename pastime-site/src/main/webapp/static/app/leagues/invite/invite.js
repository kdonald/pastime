define(["pastime", "require", "jquery"], function(pastime, require, $) {

  var module = function(url) {

    var container = $("<div></div>", {
      id: "invite"
    });

    var signin = pastime.signin(container);
    signin.done(function() {
      var xhr = pastime.get(url);
      xhr.done(function(invite) {
        console.log(invite);
      });
    });

    return container;

  };

  return module;

});