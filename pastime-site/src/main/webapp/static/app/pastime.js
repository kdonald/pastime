define("pastime", ["require", "jquery", "jquery-cookie"], function(require, $) {

  var siteUrl = "http://localhost:8080";
  var apiUrl = "http://localhost:8081";
  
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
      seasons: apiUrl + "/seasons",
      me: apiUrl + "/me",
      signup: siteUrl + "/signup",
      signin: siteUrl + "/signin"
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
        data: data,
        dataType: "json",
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
  
  return pastime;

});