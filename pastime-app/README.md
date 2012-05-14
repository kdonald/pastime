Pastime
==========

play sports together.

Project Team
------------
* Keith Donald

Testing
-------
Test out the app by opening index.html in your web browser.
We recommend using the latest version of the Google Chrome web browser.

For page transitions to work you'll need to launch Chrome with the "-allow-file-access-from-files" option. Read how to do this here:
* http://moonlitscript.com/2012/01/13/chrome-local-dev/

Debugging
---------
See what's going on behind the scenes while testing by switching on "Chrome Developer Tools" in the View -> Developer menu.
The "Elements" tab lets you inspect the HTML content of the current page.
The "Resources" tab lets you view everything the current page has loaded; including images, JavaScript files, css files, and other files.
The "Network" tab lets you see and inspect any network activity that occurs.
The "Scripts" tab lets you review and debug JavaScript loaded by the page.
The "Console" tab allows you to view any logging the application performs and also interact with the state of the application.

See https://developers.google.com/chrome-developer-tools/ for more information on Chrome Developer Tools.

Project Directory Structure
----------
* / - Files directly related to the 'index.html' (or starting page) of the application.
* /libs - Generic libraries and infrastructure used by the application; for example, JQuery Mobile.
* /modules - Application functionality organized into modules; for example, the "About" module and the "Messages" module.
* /styles - Cascading Style Sheets (CSS) that style page content (make it look pretty).
* /specs - Automated tests that verify the application works. Also contains learning exercises.

Technologies Used
------------
* Modular JavaScript with AMD and RequireJS
* DOM manipulation and ajax with jQuery
* Client-side templating with Handlebars
* Client-side page routing with PathJS; includes a custom "router" extension we developed atop PathJS.
* MVC with our own custom library that was inspired by Backbone and AgilityJS.