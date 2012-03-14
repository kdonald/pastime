define("facebook", ["jquery", "https://connect.facebook.net/en_US/all.js"], function($) {
  
  $("<div id='fb-root'></div>").prependTo($("body"));
  
  FB.init({
    appId : '212373618850030', // App ID
    channelUrl : '//pastime.com/channel.html', // Channel File
    status : false, // check login status
    cookie : false, // enable cookies to allow the server to access the session
    xfbml : false // parse XFBML
  });
  
});


