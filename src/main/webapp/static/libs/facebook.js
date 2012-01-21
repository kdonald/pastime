define(["http://connect.facebook.net/en_US/all.js"], function() {
  FB.init({
    appId      : "212373618850030",
    channelUrl : "//localhost:8080/channel.html",
    status     : true,
    cookie     : true,
    xfbml      : true
  });  
});