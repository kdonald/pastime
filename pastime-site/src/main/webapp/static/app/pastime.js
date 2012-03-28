define("pastime", ["jquery"], function($) {

  var addAuthorizationHeader = function(xhr) {
    if (pastime.accessGrant) {
      xhr.setRequestHeader("Authorization", pastime.accessGrant.access_token);
    }
  };

  var pastime = {
    accessGrant: {
      access_token: "123456789"
    },
    links: {
      leagues: "http://api.pastime.com/leagues",
      me: "http://api.pastime.com/me",
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
    }
  };
  
  return pastime;
  
});