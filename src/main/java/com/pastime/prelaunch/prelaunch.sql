CREATE SCHEMA prelaunch;

CREATE TABLE prelaunch.subscriptions (id serial CONSTRAINT pk_subscriptions PRIMARY KEY,
  email varchar(320) NOT NULL UNIQUE,
  first_name varchar(64) NOT NULL, 
  last_name varchar(64),
  referral_code char(6) NOT NULL UNIQUE,
  referred_by integer,
  created timestamp NOT NULL CONSTRAINT df_subscriptions_created DEFAULT now(),
  CONSTRAINT fk_subscriptions_referred_by FOREIGN KEY (referred_by) REFERENCES prelaunch.subscriptions(id)
);

GRANT USAGE ON SCHEMA prelaunch TO pastime;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA prelaunch TO pastime;
GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA prelaunch TO pastime;


CREATE TABLE prelaunch.referralCount (
  count integer;
);

CREATE TABLE prelaunch.subscriberReferralCounts (id serial,
  count smallint NOT NULL,
  CONSTRAINT fk_referalls_subscriptions FOREIGN KEY (id) REFERENCES subscriptions(id)
);