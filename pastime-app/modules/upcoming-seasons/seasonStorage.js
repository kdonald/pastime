define(["jquery", "collections/list"], function($, list) {
    
  var upcomingSeasons = list.create()
    .add({
      id: 1,
      name: "South Brevard Adult Flag Football",
      picture: "/brevardparks/south-flag/2012-summer.png",
      format: "7 on 7",
      sport: "Flag Football",
      starts: "2012-07-10",
      venue: {
       name: "Palm Bay Regional Park"
      },
      organization: {
       name: "Brevard Parks and Recreation",       
       logo: "/brevardparks/logo.png"
      },
      link: "/brevardparks/south-flag/2012-summer"
    })
    .add({
      id: 2,
      name: "Max Rodes Indoor Volleyball",
      picture: "/brevardparks/south-indoor/2012-summer.png",
      format: "6 on 6",
      sport: "Indoor Volleyball",
      starts: "2012-08-07",
      venue: {
        name: "Max Rodes Park"
      },
      organization: {
        name: "Brevard Parks and Recreation",      
        logo: "/brevardparks/logo.png"
      },
      link: "/brevardparks/south-indoor/2012-summer"       
    })
    .add({
      id: 3,
      name: "Palm Bay Pony Softball",
      picture: "/palmbay/pony-softball/2012-fall.png",
      format: "Youth 5-7",
      sport: "Softball",
      starts: "2012-08-22",
      venue: {
        name: "Liberty Park"
      },
      organization: {
        name: "City of Palm Bay",       
        logo: "/palmbay/logo.png"
      },
      link: "/palmbay/pony-softball/2012-fall"       
    });
    
  return {
    upcomingSeasons: upcomingSeasons
  }
     
});