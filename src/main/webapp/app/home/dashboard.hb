<article id="dashboard">
  <section id="newsFeed">
    <h2>News Feed</h2>
    <ol>
      {{#newsItems}}
      <li>
        {{> newsItem}}
      </li>
      {{/newsItems}}  
    </ol>
  </section>
  <section id="yourLeagues">
    <h2>Your Leagues ({{yourLeagues.length}})</h2>
    <ul>
      {{#yourLeagues}}
      <li class="{{sport}}">
        <a href="{{path}}">{{name}}</a>
      </li>
      {{/yourLeagues}}
    </ul>
  </section>
  <section id="watchedLeagues">
    <h2>Leagues You're Watching ({{watchedLeagues.length}})</h2>
    <ul>
      {{#watchedLeagues}}
      <li class="{{sport}}">
        <a href="{{path}}">{{name}}</a>
      </li>
      {{/watchedLeagues}}
    </ul>
  </section>
</article>
