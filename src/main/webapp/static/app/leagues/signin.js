define(["require", "jquery", "mvc", "facebook"], function(require, $, MVC, facebook) {

  var mvc = MVC.create(require);

  $(document).ready(function() {

    $("form[input:radio[name=account]").on("change", function(event) {
      onAccountSelection($(this).val());
    });

    onAccountSelection($("form[input:radio[name=account][checked]").val());
    
    function onAccountSelection(val) {
      if ("existing" === val) {
        signin();
      } else if ("new" === val) {
        signup();
      }      
    }

    function signin() {
      var status = facebook.getLoginStatus();
      status.done(function(response) {
        var signin = mvc.view({
          model: {
            signedIntoFacebook: "connected" === response.status || "not_authorized" === response.status
          },
          template: "signin"
        });
        signin.html($("#accountSelectionResult"));        
      });
    }
    
    function signup() {
      var status = facebook.getLoginStatus();
      status.done(function(response) {
        var signupType = mvc.view({
          model: {
            signedIntoFacebook: "connected" === response.status || "not_authorized" === response.status,
            facebookUser: "Keith Donald"
          },
          template: "signup-type",
          events: {
            "click button[name='facebook']": function() {
              var self = this;
              var login = facebook.login({ scope: 'email,user_birthday' });
              login.done(function() {
                $.when(facebook.api("/me"), facebook.api("/me/picture")).then(function(me, picture) {
                  var signupForm = { 
                      picture_url: picture,
                      first_name: me.first_name,
                      last_name: me.last_name,
                      gender: me.gender,
                      birthday: me.birthday,
                      email: me.email,
                      zip_code: null,
                      password: null,
                    };
                    var facebookSignup = mvc.view({
                      model: signupForm,
                      template: "signup-facebook",
                      events: {
                        "submit": function() {
                          var xhr = $.post("/signup", signupForm);
                          console.log(xhr);
                          return false;
                        }
                      }
                    });
                    facebookSignup.html(self.$("#signupPane"));                  
                });
              });              
              return false;
            },
            "click button[name='manual']": function() {
              var signup = mvc.view({ 
                template: "signup"
              });              
              signup.html(this.$("#signupPane"));
              return false;
            }            
          }
        });      
        signupType.html($("#accountSelectionResult"));        
      });      
    }
    
  });
  
});