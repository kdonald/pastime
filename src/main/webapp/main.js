require.config({
  baseUrl: "/app",
  paths: {
    "jquery": "/libs/jquery/1.7.1/jquery",
    "jqueryui" : "/libs/jqueryui/1.8.16",
    "jqueryuicarousel" : "/libs/jqueryuicarousel/0.8.5"
  }
});
require(["jquery", "jqueryuicarousel/autoscroll"], function($) {
	console.log("Loaded");
});