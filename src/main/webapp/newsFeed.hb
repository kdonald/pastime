<h2>News Feed</h2>
<ol>
{{#articles}}
  <li>
    <article class="{{type}}">
      <header>
        <h1>{{{author.name}}} - {{time}}</h1>
      </header>
      <div>
        {{{body}}}
      </div>
    </article>
  </li>
{{/articles}}  
</ol>