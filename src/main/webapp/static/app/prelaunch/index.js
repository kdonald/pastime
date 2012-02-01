define(["jquery", "handlebars", "text!./thanks.html", "polyfiller", "textselect", "facebook"], function($, handlebars, thanksTemplate) {

  $.webshims.debug = false;
  $.webshims.setOptions({
    waitReady: false,
    basePath: "/static/libs/webshims/1.8.7/shims/"
  });
  $.webshims.polyfill();
  
  thanksTemplate = handlebars.compile(thanksTemplate);
  var api = createApi();
  
  $(document).ready(function() {
    var subscribeForm = $("#subscribe form");
    subscribeForm.on("submit", function() {
      var xhr = api.subscribe(subscribeForm);
      xhr.done(showSubscribedDialog);
      return false;
    });
    $("#subscribe form button").removeAttr("disabled");    
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
    thanks.find("span.selectableText").on("click", function() {
      $(this).textselect();
    });
    var result = $("#subscribeResult");
    thanks.find("#thanksTitleBar span").on("click", function() {
      result.fadeOut(500);
    });
    result.html(thanks);    
    result.fadeIn(500);
    FB.XFBML.parse(document.getElementById("thanks"));    
  }
  
});