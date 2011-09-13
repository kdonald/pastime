<form id="createTeam" action="/teams" method="post">
  <h3>Create your own team</h3>
  <label>Name <input name="name" type="text" required data-bind="name" /></label>
  <label>Sport <select name="sport" data-bind="sport" data-options="sports" />
  </label>
  <button type="submit">Go</button>
</form>