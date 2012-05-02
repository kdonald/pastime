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
    function addInvite(invite) {
      this.invites.push(invite);
      this.trigger("inviteAdded", invite);
    }
    function removeInvite(invite) {
      var index = this.invites.indexOf(invite);
      this.invites.splice(index, 1);
      this.trigger("inviteRemoved", invite);
    }    
    return Object.create(Object.prototype, {
      addPlayer: { value: addPlayer },
      removePlayer: { value: removePlayer },
      playerCount: { value: playerCount, enumerable: true },
      valid: { value: valid },
      addInvite: { value: addInvite },
      removeInvite: { value: removeInvite }
    });
  })();

  function create(minPlayers, maxPlayers) {
    return observable(Object.create(rosterPrototype, {
      minPlayers: { value: minPlayers, enumerable: true },
      maxPlayers: { value: maxPlayers, enumerable: true },
      players: { value: [] },
      invites: { value: [] }
    }));          
  }

  return create;
   
});