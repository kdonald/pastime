define(["pastime", "mvc", "text!./account.html", "text!./signin.html", "text!./signup-type.html","text!./signup-facebook.html", "text!./signup.html", "facebook"],
    function(pastime, mvc, accountTemplate, signinTemplate, signupTypeTemplate, signupFacebookTemplate, signupTemplate, facebook) {

  var account = mvc.view({
    template: accountTemplate,
    init: function() {
      var self = this;
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
            template: signinTemplate
          // TODO signin event handling
          });
          self.$("#account-selection-result").html(signin.render());
        });
      };
      this.signup = function() {
        var status = facebook.getLoginStatus();
        status.done(function(response) {
          var signupType = mvc.view({
            model: {
              signedIntoFacebook: "connected" === response.status || "not_authorized" === response.status
            },
            template: signupTypeTemplate,
            events: {
              "click button[name='facebook']": function() {
                var self = this;
                var facebookScope = "email,user_birthday";
                var login = facebook.login({
                  scope: facebookScope
                });
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
                      template: signupFacebookTemplate,
                      events: {
                        "submit": function() {
                          var xhr = $.post(pastime.links["signup"], signupForm);
                          xhr.done(function(accessGrant) {
                            pastime.accessGrant = accessGrant;
                            var xhr = pastime.get(pastime.links["me"]);
                            xhr.done(function(me) {
                              pastime.me = me;
                              account.trigger("signedin");
                            });
                          });
                          return false;
                        }
                      }
                    });
                    self.$("#signup-pane").html(facebookSignup.render());
                  });
                });
                return false;
              },
              "click button[name='manual']": function() {
                var signup = mvc.view({
                  template: signupTemplate
                });
                this.$("#signup-pane").html(signup.render());
                return false;
              }
            }
          });
          self.$("#account-selection-result").html(signupType.render());
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