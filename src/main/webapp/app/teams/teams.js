define(["require", "jquery", "mvc", "api"], function(require, $, MVC, api) {

  var mvc = MVC.create(require), view = undefined;
  
  function init(context) {
    view = mvc.view({
      template: "teams"
    });
    render(context);
  }

  function render(context) {
    view.render(function(root) {
      context.$element().children().detach();
      context.$element().append(root);
    });
  }

  return function(routes) {
    routes.get("/teams", function(context) {
      if (view === undefined) {
        init(context);
      } else {
        render(context);
      }      
    });
  };
      
});