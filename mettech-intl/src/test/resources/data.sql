-- This file allows us to load static data into the test database before tests are run.

-- Passwords are in the format: Password<UserLetter>123. Unless specified otherwise.
-- Encrypted using https://www.javainuse.com/onlineBcrypt
INSERT INTO users (email, first_name, last_name, password, username, email_verified)
    VALUES ('UserA@junit.com', 'UserA-FirstName', 'UserA-LastName', '$2a$10$tuIpzB504EayuGoe77CjG.pO4J2fSOkXSd.u0ecbQludArhvxTWqy', 'UserA', true)
    , ('UserB@junit.com', 'UserB-FirstName', 'UserB-LastName', '$2a$10$YXBhSkj.hJBxW.Wi2HRF0udqe7TWZ6CNG48UU.U6K/TvBW6W9/.MO', 'UserB', false)
    , ('UserC@junit.com', 'UserC-FirstName', 'UserC-LastName', '$2a$10$Rm6YB6LY3VwMuuJAdoRrduIN8bJnx1BKRm2nzjgTHZ7Evp/qKro36', 'UserC', false);
