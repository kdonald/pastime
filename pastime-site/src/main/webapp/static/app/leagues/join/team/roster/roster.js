define(["mvc/observable"], function(observable) {

  var rosterPrototype = (function() {
    function addPlayer(player) {
      this.players.push(player);
      this.trigger("playerAdded", player);
      this.trigger("playerCount", this.playerCount());
    }
    function removePlayer(player) {
      var index = this.players.indexOf(player);
      this.players.splice(index, 1);
      this.trigger("playerRemoved", player);
      this.trigger("playerCount", this.playerCount());
    }
    function playerCount() {
      return this.players.length;
    }
    function valid() {
      return this.playerCount() >= this.minPlayers && this.playerCount() <= this.maxPlayers;
    }     
    return Object.create(Object.prototype, {
      addPlayer: { value: addPlayer },
      removePlayer: { value: removePlayer },
      playerCount: { value: playerCount, enumerable: true },
      valid: { value: valid }
    });
  })();

  function create(minPlayers, maxPlayers) {
    return observable(Object.create(rosterPrototype, {
      minPlayers: { value: minPlayers, enumerable: true },
      maxPlayers: { value: maxPlayers, enumerable: true },
      players: { value: [] }
    }));          
  }

  return create;
   
});