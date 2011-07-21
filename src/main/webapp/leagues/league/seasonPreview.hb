<h2>{{league.name}}</h2>
<h3>{{name}}</h3>
<ul>
<li>{{format}}</li>
<li>{{games}} games plus playoffs</li>
<li>
  {{#gameDays}}
  played on {{day}} {{time}}
  {{/gameDays}}
</li> 
<li>starts {{startDate}} </li>
</ul>
<a href="join">Join Now</a>
<sections id="venues">
  <h2>Venues</h2>
  <ol>
    {{#venues}}
    <li class="venue">
      <a href="{{path}}">{{name}}</a>
    </li>
    {{/venues}}  
  </ol>  
</section>
<section id="teams">
  <h2>Teams</h2>
  <ol>
    {{#teams}}
    <li class="team">
      <a href="{{path}}">{{name}}</a>
    </li>
    {{/teams}}  
  </ol>
</section>
<section id="freeAgents">
  <h2>Free Agents</h2>
  <ol>
    {{#freeAgents}}
    <li class="player">
      <a href="{{path}}">{{name}}</a>
    </li>
    {{/freeAgents}}  
  </ol>
</section>