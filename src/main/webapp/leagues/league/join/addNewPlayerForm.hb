<section id="addNewPlayerForm">
  <h5>Add a new player</h5>
  <form>
    <p>
      Please complete a few more details.
      The person will be added to the roster and receive an email inviting them to the team.
    </p>
    <label>
      Full Name <input name="name" type="text" data-bind="name" required />
    </label>
    <label>
      E-mail <input name="email" type="email" data-bind="email" required />  
    </label>
    <button class="add">Add</button>
    <button class="cancel">Cancel</button>
  </form>
</section>