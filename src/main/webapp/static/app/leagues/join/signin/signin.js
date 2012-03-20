define(["require", "mvc", "facebook"], function(require, MVC, facebook) {

  var mvc = MVC.create(require);

  var account = mvc.view({
    template: "account",
    init: function() {
      this.handleSelectedAccount = function(val) {
        if ("existing" === val) {
          this.signin();
        } else if ("new" === val) {
          this.signup();
        }      
      };
      this.signin = function() {
        var status = facebook.getLoginStatus();
        status.done(function(response) {
          var signin = mvc.view({
            model: {
              signedIntoFacebook: "connected" === response.status || "not_authorized" === response.status
            },
            template: "signin"
          });
          signin.html($("#account-selection-result"));        
        });
      };
      this.signup = function() {
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
                var facebookScope = "email,user_birthday";
                var login = facebook.login({ scope: facebookScope });
                login.done(function(authResponse) {
                  $.when(facebook.api("/me"), facebook.api("/me/picture")).then(function(me, picture) {
                    var signupForm = { 
                        picture_url: picture,
                        first_name: me.first_name,
                        last_name: me.last_name,
                        gender: me.gender === "male" ? "Male" : "Female",
                        birthday: me.birthday,
                        email: me.email,
                        zip_code: null,
                        password: null,
                        connection: {
                          provider: "facebook",
                          user_id: authResponse.userID,
                          access_token: authResponse.accessToken,
                          scope: facebookScope,
                          expires_in: authResponse.expiresIn,
                        },
                      };
                      var facebookSignup = mvc.view({
                        model: signupForm,
                        template: "signup-facebook",
                        events: {
                          "submit": function() {
                            var xhr = $.post("/signup", signupForm);
                            xhr.done(function(id) {
                              account.trigger("signedin", { id: id });
                            });
                            return false;
                          }
                        }
                      });
                      facebookSignup.html(self.$("#signup-pane"));                  
                  });
                });              
                return false;
              },
              "click button[name='manual']": function() {
                var signup = mvc.view({ 
                  template: "signup"
                });              
                signup.html(this.$("#signup-pane"));
                return false;
              }            
            }
          });      
          signupType.html($("#account-selection-result"));        
        });      
      };      
      this.handleSelectedAccount(this.$("form[input:radio[name=account][checked]").val());      
    },
    events: {
      "change form[input:radio[name=account]": function(event) {
        this.handleSelectedAccount(event.currentTarget.value);
      }
    }
  });
  
  return account;
  
});