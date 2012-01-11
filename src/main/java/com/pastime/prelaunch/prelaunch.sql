CREATE SCHEMA prelaunch;

CREATE TABLE prelaunch.subscriptions (id serial CONSTRAINT pk_subscriptions PRIMARY KEY,
  email varchar(320) NOT NULL UNIQUE,
  firstName varchar(64) NOT NULL, 
  lastName varchar(64) NOT NULL,
  referallCode char(6) NOT NULL UNIQUE,
  referredBy integer,
  created timestamp NOT NULL,
  CONSTRAINT fk_subscriptions_referredBy FOREIGN KEY (referredBy) REFERENCES prelaunch.subscriptions(id)
);

CREATE TABLE prelaunch.referralCount (
  count integer;
);

CREATE TABLE prelaunch.subscriberReferralCounts (id serial,
  count smallint NOT NULL,
  CONSTRAINT fk_referalls_subscriptions FOREIGN KEY (id) REFERENCES subscriptions(id)
);