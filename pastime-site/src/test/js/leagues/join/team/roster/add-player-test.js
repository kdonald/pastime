define(['leagues/join/team/roster/add-player'], function(addPlayer) {
  describe('add-player', function() {
    it('should be an object', function() {
     expect(addPlayer).to.be.a(Object);
    });
    it('expand with email', function() {
    	var view = addPlayer({});
    	view.render();
    	view.expand("keith@pastime.com");
    	expect(view.$("input[name=email]").val()).to.eql("keith@pastime.com");
    });
    it('expand with name', function() {
    	var view = addPlayer({});
    	view.render();
    	view.expand("Keith Donald");
    	expect(view.$("input[name=name]").val()).to.eql("Keith Donald");
    });    
  });
});