CREATE DATABASE pastime; -- \connect pastime
CREATE USER pastime; -- \password hitmen

CREATE SCHEMA prelaunch;
GRANT USAGE ON SCHEMA prelaunch TO pastime;

CREATE TABLE prelaunch.subscriptions (id serial CONSTRAINT pk_subscriptions PRIMARY KEY,
  email varchar(320) NOT NULL UNIQUE,
  first_name varchar(64) NOT NULL, 
  last_name varchar(64) NOT NULL,
  referral_code char(6) NOT NULL UNIQUE,
  referred_by integer,
  created timestamp NOT NULL CONSTRAINT df_subscriptions_created DEFAULT now(),
  unsubscribed boolean NOT NULL CONSTRAINT df_subscriptions_unsubscribed DEFAULT false,
  CONSTRAINT fk_subscriptions_referred_by FOREIGN KEY (referred_by) REFERENCES prelaunch.subscriptions(id)
);
GRANT USAGE ON ALL SEQUENCES IN SCHEMA prelaunch TO pastime;
GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA prelaunch TO pastime;