define(["mvc", "text!./container.html", "text!./carousel.html", "text!./empty.html", "jqueryui-carousel"], function(mvc, containerTemplate, carouselTemplate, emptyTemplate) {
  
  var carouselPrototype = mvc.prototype({
    template: carouselTemplate,
    itemsAppender: function(item, list) {
      if (this.carousel) {
        this.carousel.rcarousel("append", item);
      } else {
        list.append(item);
      }
    },
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
  
  var carousel = function(items, itemView) {
    return mvc.create(carouselPrototype, {
      model: { items: items },
      itemsView: itemView
    });
  }
  
  var empty = function(message) {
    return mvc.create({
      id: "empty",
      template: emptyTemplate,
      model: { message: message }
    });
  }
  
  var containerPrototype = mvc.prototype({ 
    template: containerTemplate,
    init: function() {
      if (this.items.size() > 0) {
        this.carousel();
      } else {
        this.empty();
      }
      this.subscribe();
      window.items = this.items;
    },
    subscribe: function() {
      this.items.observer(this).
        on("add", function() {
          if (this._content.id === "empty") {
            this.carousel();
          }          
        })
        .on("remove", function() {
          if (this.items.size() === 0) {
            this.empty();
          }          
        });
      this.on("destroy", function() {
        this.items.observer(this).off();
      });      
    },
    carousel: function() {
      this.content(carousel(this.items, this.itemView));
    },
    empty: function() {
      this.content(empty(this.emptyMessage));      
    },
    content: function(content) {
      if (this._content) {
        this._content.destroy();
      }
      this._content = content;
      this.append(this._content); 
    }
  });
    
  return function(args) {
    return mvc.create(containerPrototype, { 
      model: { title: args.title },
      items: args.items,
      itemView: args.itemView,
      emptyMessage: args.emptyMessage
    });
  }
  
});