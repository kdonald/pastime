define("pastime", ["require", "jquery", "jquery-cookie"], function(require, $) {

  function cookie() {
    return $.cookie("access_token");
  }

  var addAuthorizationHeader = function(xhr) {
    if (pastime.grant) {
      xhr.setRequestHeader("Authorization", pastime.grant.access_token);
    }
  };

  var pastime = {
    links: {
      seasons: "http://api.pastime.com/seasons",
      me: "http://api.pastime.com/me",
      signup: "http://pastime.com/signup",
      signin: "http://pastime.com/signin"
    },
    get: function(url, params) {
      return $.ajax({
        type: "GET",
        url: url,
        data: params,
        dataType: "json",
        beforeSend: addAuthorizationHeader
      });
    },
    post: function(url, data) {
      return $.ajax({
        type: "POST",
        url: url,
        data: params,
        beforeSend: addAuthorizationHeader
      });
    },
    signin: function(container) {
      var deferred = $.Deferred();
      if (this.signedIn()) {
        var xhr = this.get(this.links["me"]);
        xhr.done(function(me) {
          pastime.me = me;
          deferred.resolve();
        });
      } else {
        require(["signin/signin"], function(signin) {
          signin.on("signedin", function() {
            deferred.resolve();
          });
          container.html(signin.render());
        });
      }
      return deferred;
    },
    signedIn: function() {
      return cookie() != null;
    }
  };

  if (pastime.signedIn()) {
    // needed to make api.pastime.com calls
    pastime.grant = {
      access_token: cookie()
    };
  }

  console.log(pastime);
  
  return pastime;

});