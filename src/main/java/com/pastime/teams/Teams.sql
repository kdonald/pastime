create table Team (id identity,
					name varchar not null, 
					primary key (id));
					
create table TeamMember (team bigint,
					id bigint,
					name varchar not null,
					email varchar,
					primary key (team, id),
					foreign key (team) references Team(id));