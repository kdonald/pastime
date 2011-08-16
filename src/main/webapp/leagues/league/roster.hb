<section id="roster">
  <h4>Roster</h4>
  <ul id="players">
    <!-- Players added dynamically -->
  </ul>
  <input id="newPlayer" type="text" placeholder="Add a new player" />
  <div id="summary">{{minPlayers}} min {{maxPlayers}} max <span class="counter" data-bind="playerCount">0</span>/{{maxPlayers}}</div>
</section>