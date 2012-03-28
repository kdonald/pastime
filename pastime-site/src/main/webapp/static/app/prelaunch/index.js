define(["jquery", "handlebars", "text!./thanks.html", "polyfiller", "textselect"], function($, handlebars, thanksTemplate) {

  $.webshims.debug = false;
  $.webshims.setOptions({
    waitReady: false,
    basePath: "/static/libs/webshims/1.8.7/shims/",
    forms: {
      customMessages: true
    }
  });
  $.webshims.polyfill("forms");
  
  thanksTemplate = handlebars.compile(thanksTemplate);
  var api = createApi();
  
  $(document).ready(function() {
    var subscribeForm = $("#subscribe form");
    subscribeForm.on("submit", function() {
      var xhr = api.subscribe(subscribeForm);
      xhr.done(function(data) {
        showSubscribedDialog(subscribeForm, data);
      });
      return false;
    });
    subscribeForm.find("button").removeAttr("disabled");    
  });

  function createApi() {
    return {
      subscribe: function(form) {
        return $.ajax({type: "POST", data: form.serialize() });
      }
    };
  }
  
  function showSubscribedDialog(subscribeForm, data) {
    var modalOverlay = $("#modalOverlay");
    var thanks = $(thanksTemplate({ name: data.firstName, referralLink: data.referralLink })); 
    thanks.find("div.selectableText").on("click", function() {
      $(this).textselect();
    });
    var result = $("#subscribeResult");
    thanks.find("#thanksTitleBar span").on("click", function() {
      modalOverlay.fadeOut(500);      
      result.fadeOut(500);
      subscribeForm[0].reset();
    });
    result.html(thanks);
    if (FB) {
      FB.XFBML.parse(document.getElementById("thanks"));      
    }
    modalOverlay.fadeIn(500);
    result.fadeIn(500);    
  }
  
});