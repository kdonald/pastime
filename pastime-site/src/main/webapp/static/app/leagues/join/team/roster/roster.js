define([], function() {

  var rosterPrototype = (function() {
    function addPlayer(player) {
      this.players.push(player);
      this.addListeners.forEach(function(listener) {
        listener(player);
      });
      invokeChangeListeners(this.changeListeners, this.playerCount());  
    }
    function removePlayer(player) {
      var index = this.players.indexOf(player);
      this.players.splice(index, 1);
      this.removeListeners.forEach(function(listener) {
        listener(player);
      });
      invokeChangeListeners(this.changeListeners, this.playerCount());
    }
    function playerCount() {
      return this.players.length;
    }
    function valid() {
      return this.playerCount() >= this.minPlayers && this.playerCount() <= this.maxPlayers;
    }         
    function playerAdd(listener) {
      this.addListeners.push(listener);
    }
    function playerRemove(listener) {
      this.removeListeners.push(listener);
    }    
    function addInvite(invite) {
      console.log("Adding invite:");
      console.log(invite);
    }
    function change(property, listener) {
      this.changeListeners.push(listener);
    }    
    function invokeChangeListeners(listeners, value) {
      listeners.forEach(function(listener) {
        listener(value);
      });
    }
    return Object.create(Object.prototype, {
      addPlayer: { value: addPlayer },
      removePlayer: { value: removePlayer },
      playerCount: { value: playerCount, enumerable: true },
      valid: { value: valid },
      playerAdd: { value: playerAdd },
      playerRemove: { value: playerRemove },
      addInvite: { value: addInvite },
      change: { value: change },
    });
  })();

  function create(minPlayers, maxPlayers) {
    return Object.create(rosterPrototype, {
      minPlayers: { value: minPlayers, enumerable: true },
      maxPlayers: { value: maxPlayers, enumerable: true },
      players: { value: [] },
      addListeners: { value: [] },
      removeListeners: { value: [] },
      changeListeners: { value: [] }
    });          
  }

  return create;
   
});