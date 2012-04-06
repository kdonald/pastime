define("facebook", ["jquery", "https://connect.facebook.net/en_US/all.js"], function($) {
  
  var appId = " 130304913761039"; // production: 212373618850030
  
  $("<div id='fb-root'></div>").prependTo($("body"));
  
  FB.init({
    appId : appId, // App ID
    // channelUrl : '//pastime.com/channel.html', // Channel File (production only)
    status : false, // check login status
    cookie : false, // enable cookies to allow the server to access the session
    xfbml : false // parse XFBML
  });
  
  function getLoginStatus() {
    var deferred = $.Deferred();
    FB.getLoginStatus(function(response) {
      deferred.resolve(response);
    });
    return deferred.promise();    
  }
  
  function login(permissions) {
    var deferred = $.Deferred();    
    var status = getLoginStatus();
    status.done(function(response) {
      if (response.authResponse) {
        deferred.resolve(response.authResponse);
      } else {
        FB.login(function(response) {
          if (response.authResponse) {
            deferred.resolve(response.authResponse);        
          } else {
            deferred.reject();
          }
        }, permissions);        
      }
    });
    return deferred.promise();
  }
  
  function api(resource) {
    var deferred = $.Deferred();    
    FB.api(resource, function(response) {
      deferred.resolve(response); 
    });
    return deferred.promise();    
  }
  
  return {
    getLoginStatus: getLoginStatus,
    login: login,
    api: api
  };
  
});


