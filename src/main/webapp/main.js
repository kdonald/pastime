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
  var api = createApi();
  $(document).ready(function() {
    var subscribeForm = $("#subscribe form");
    subscribeForm.on("submit", function(event) {
      var xhr = api.subscribe(subscribeForm);
      xhr.done(function(data) {
        var html = thanksTemplate({ name: data.name, referralLink: data.referralLink });        
        $(html).dialog({ title: "You're Subscribed!", modal: true, height: 325, width: 510 });
        FB.XFBML.parse();
      });
      return false;
    });
  });
});