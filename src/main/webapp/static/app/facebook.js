define("facebook", ["jquery", "https://connect.facebook.net/en_US/all.js"], function($) {
  
  $("<div id='fb-root'></div>").prependTo($("body"));
  
  FB.init({
    appId : '212373618850030', // App ID
    channelUrl : '//pastime.com/channel.html', // Channel File
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
    FB.login(function(response) {
      if (response.authResponse) {
        deferred.resolve(response);        
      } else {
        deferred.reject();
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


