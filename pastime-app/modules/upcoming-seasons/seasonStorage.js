define(["jquery", "observable"], function($, observable) {
  
  var testData = [
    {
      id: 1,
      picture: "/brevardparks/south-flag/2012-spring.png",
      format: "7 on 7",
      sport: "Flag Football",
      starts: 2012-07-10,
      venue: {
        name: "Palm Bay Regional Park"
      },
      organization: {
        name: "Brevard Parks and Recreation",       
        logo: "/brevardparks/logo.png"
      }
    },
    {
      id: 2,
      picture: "/brevardparks/south-flag/2012-spring.png",
      format: "6 on 6",
      sport: "Indoor Volleyball",
      starts: 2012-08-07,
      venue: {
        name: "Max Rodes Park"
      },
      organization: {
        name: "Brevard Parks and Recreation",      
        logo: "/brevardparks/logo.png"
      }
    }    
  ];

  var upcomingSeasons = observable({});
   
  function upcomingSeasons() {
    return upcomingSeasons;
  }

  return Object.create(Object.prototype, {
    upcomingSeasons: { value: upcomingSeasons }    
  });
     
});