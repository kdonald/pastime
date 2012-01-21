require.config({
  paths: {
    "facebook": "../libs/facebook",
    "jquery": "../libs/jquery/1.7.1/jquery",
    "jqueryui": "../libs/jqueryui/1.8.16",
    "handlebars": "../libs/handlebars/1.0.0.beta6/handlebars",
    "text": "../libs/requirejs-text/1.0.2/text"
  }
});
require(["require", "jquery", "handlebars", "text!thanks.html", "jqueryui/dialog", "facebook"], function(require, $, handlebars, thanksTemplate) {
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
    return {
      subscribe: function(form) {
        return $.ajax({type: "POST", data: form.serialize() });
      }
    };
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