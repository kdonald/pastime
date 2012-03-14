define(["require", "jquery", "mvc", "facebook"], function(require, $, MVC) {

  var mvc = MVC.create(require);
  
  $(document).ready(function() {

    $("form[input:radio[name=account]").on("change", function(event) {
      evalAccount($(this).val());
    });

    evalAccount($("form[input:radio[name=account][checked]").val());
    
    function evalAccount(val) {
      console.log(val);
      if ("existing" === val) {
        signin();
      } else if ("new" === val) {
        signup();
      }      
    }

    function signin() {
      FB.getLoginStatus(function(response) {
        var signin = mvc.view({
          model: {
            signedIntoFacebook: "connected" === response.status || "not_authorized" === response.status
          },
          template: "signin"
        });
        signin.render(function(root) {
          selectionHtml(root);
        });        
      });
    }
    
    function signup() {
      FB.getLoginStatus(function(response) {
        var signupType = mvc.view({
          model: {
            signedIntoFacebook: "connected" === response.status || "not_authorized" === response.status,
            facebookUser: "Keith Donald"
          },
          template: "signup-type",
          events: {
            "click button[name='facebook']": function() {
              FB.login(function(response) {
                if (response.authResponse) {
                  console.log('User signed into Pastime using Facebook...');
                } else {
                  console.log('User cancelled login or did not fully authorize.');
                }
              }, { scope: 'email,user_birthday,user_location' });              
              return false;
            },
            "click button[name='manual']": function() {
              var signup = mvc.view({ 
                template: "signup"
              });              
              var container = this.$("#signupContent");
              signup.render(function(root) {
                container.html(root);
              });
              return false;
            }            
          }
        });      
        signupType.render(function(root) {
          selectionHtml(root);
        });        
      });      
    }
    
    function selectionHtml(root) {
      $("#selectionContent").html(root);      
    }
    
  });
  
});