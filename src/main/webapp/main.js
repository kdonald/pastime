require.config({
  baseUrl: "app",
  paths: {
    "facebook": "../libs/facebook",
    "jquery": "../libs/jquery/1.7.1/jquery",
    "jqueryui": "../libs/jqueryui/1.8.16",
    "jqueryuicarousel": "../libs/jqueryuicarousel/0.8.5",
    "jqueryui" : "../libs/jqueryui/1.8.16",
    "handlebars": "../libs/handlebars/1.0.0.beta6/handlebars",
    "mvc": "../libs/mvc/0.1.0pre/mvc",
    "sammy": "../libs/sammy/0.7.0/sammy",
    "listselect": "../libs/listselect/0.1.0pre/listselect"    
  }
});
require(["require", "jquery", "handlebars", "text!prelaunch/thanks.hb", "jqueryui/dialog", "facebook"], function(require, $, handlebars, thanksTemplate) {
  thanksTemplate = handlebars.compile(thanksTemplate);
  var api = createApi();
  
  $(document).ready(function() {
    var subscribeForm = $("#subscribe form");
    subscribeForm.on("submit", function() {
      var xhr = api.subscribe(subscribeForm);
      xhr.done(showSubscribedDialog);
      return false;
    });
  });

  function createApi() {
    var local = window.location.protocol == "file:";
    if (local) {
      return {
        subscribe: function(form) {
           var deferred = $.Deferred();
           deferred.resolve({ name: form.serializeArray()[0].value, referralLink: "http://pastimebrevard.com?ref=12345"});
           return deferred;
        }
      };
    } else {
      return {
        subscribe: function(form) {
          return $.ajax({type: "POST", data: form.serialize() });
        }
      };
    }
  }
  
  function showSubscribedDialog(data) {
    var thanks = $(thanksTemplate({ name: data.firstName, referralLink: data.referralLink }));  
    thanks.find("a.fb-send-button").on("click", function() {
      FB.ui({
        method: "send",
        link: data.referralLink
      });        
    });
    thanks.dialog({ title: "You're Subscribed!", modal: true, height: 450, width: 450 });
  }
  
});