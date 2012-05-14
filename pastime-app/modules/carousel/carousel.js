define(["mvc", "text!./carousel.html", "text!./carousel-item.html", "jqueryui-carousel"], function(mvc, template, itemTemplate) {

  var view = mvc.create({ 
    template: template,
    init: function() {
      this.$("ul.carousel").rcarousel({
        visible: 1,
        step: 1,
			  speed: 700,
			  auto: {
			    enabled: true
			  },
			  width: 470,
			  height: 150
      });      
    }
  });
  
  return function(args) {
    return mvc.extend(view, { 
      model: {
        title: args.title,
        items: { source: args.items, prototype: args.itemView }
      }
    });
  } 
});