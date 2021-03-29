CREATE TABLE post (
    id SERIAL PRIMARY KEY,
    name TEXT
);

CREATE TABLE candidate (
    id SERIAL PRIMARY KEY,
    name TEXT
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name TEXT,
    email TEXT,
    password TEXT
);

CREATE TABLE cities(
    id SERIAL PRIMARY KEY,
    city_name TEXT
);

alter table candidate add column city_id integer references cities(id)