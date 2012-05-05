define(["pastime", "require", "jquery", "mvc/view", "text!./answer.html"], function(pastime, require, $, view, answerTemplate) {

  var module = function(inviteLink) {

    var container = $("<div></div>", {
      id: "invite"
    });

    var signin = pastime.signin(container);
    signin.done(function() {
      var xhr = pastime.get(inviteLink);
      xhr.done(function(invite) {
        container.html(view.create({
          model: invite,
          template: answerTemplate,
          events: {
            "click button[name=accept]": function() {
              pastime.post(invite.links['accept']);
              return false;
            },
            "click button[name=decline]": function() {
              pastime.post(invite.links['decline']);              
              return false;
            }        
          }
        }).render());        
      });
    });

    return container;

  };

  return module;

});