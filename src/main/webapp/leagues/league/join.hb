<h2>{{season.league.name}}</h2>
<h3>{{season.name}}</h3>
<p>To join our league, the first thing you need to do is create your team roster.</p>
<p>
  We see that you are the owner of the <a href="{{franchise.path}}">{{franchise.name}}</a> flag football franchise.
  We assume you wish to join as {{franchise.name}}.
</p>
<h4>Franchise Players</h4>
<ul id="franchisePlayers">
  {{#franchise.activePlayers}}
  <li class="player">{{name}}</li>
  {{/franchise.activePlayers}}
</ul>
<h4>Roster</h4>
<ul id="roster" title="Roster"></ul>
<input id="newPlayer" type="text" placeholder="Add a new player" />
<div id="rosterSummary">7 min 16 max 0/16</div>
<a href="join?newteam">Join as a new team</a> instead of {{franchise.name}}</a>
<form>
  <button id="next">Next</button>
</form>
