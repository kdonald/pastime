define(["mvc", "text!./container.html", "text!./carousel.html", "text!./empty.html", "jqueryui-carousel"], function(mvc, containerTemplate, carouselTemplate, emptyTemplate) {
  
  var carousel = function(items) {
    items.appender = function(item, list) {
      if (this.carousel) {
        this.carousel.rcarousel("append", item);
      } else {
        list.append(item);
      }
    }
    return mvc.create({
      id: "carousel",
      template: carouselTemplate,
      model: { items: items },
      init: function() {
        this.carousel = this.root.rcarousel({
          visible: 1,
          step: 1,
          speed: 700,
          auto: { enabled: true },
          height: 150,
          width: 470			  
        });      
      }
    });
  }
  
  var empty = function(message) {
    return mvc.create({
      id: "empty",
      template: emptyTemplate,
      model: {
        message: message
      }
    });
  }
  
  var container = mvc.create({ 
    template: containerTemplate,
    init: function() {
      this.initContent();
      this.subscribe();
    },
    initContent: function() {
      if (this.items.source.size()) {
        this.initCarousel();
      } else {
        this.initEmpty();
      }
    },
    initCarousel: function() {
      this.content(carousel(this.items));
    },
    initEmpty: function() {
      this.content(empty(this.emptyMessage));      
    },
    subscribe: function() {
      var self = this;
      this.items.source.on("add", function(item) {
        if (self.empty()) {
          self.initCarousel();
        }
      });      
    },
    content: function(content) {
      if (this._content) {
        this._content.destroy();        
      }
      this._content = content;
      this.append(this._content); 
    },
    empty: function() {
      return this._content.id === "empty";
    }
  });
    
  return function(args) {
    return mvc.extend(container, { 
      model: { title: args.title },
      items: { 
        source: args.items, 
        itemView: args.itemView,
      },
      emptyMessage: args.emptyMessage
    });
  }
  
});