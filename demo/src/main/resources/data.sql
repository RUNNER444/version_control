INSERT INTO permission (id, resource, operation) VALUES (1, 'USER', 'create');
INSERT INTO permission (id, resource, operation) VALUES (2, 'USER', 'read');
INSERT INTO permission (id, resource, operation) VALUES (3, 'SYSTEM', 'admin');

INSERT INTO role (id, name) VALUES (1, 'ROLE_ADMIN');
INSERT INTO role (id, name) VALUES (2, 'ROLE_USER');

INSERT INTO role_permission (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 2);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 3);
INSERT INTO role_permission (role_id, permission_id) VALUES (2, 2);

--sandcat
INSERT INTO users (id, username, password, role_id) 
VALUES (1, 'blauberg', '$2a$10$ncdksZdg3//VfX/0.CNKo.7Wg5jvK2cPbr1ub1kt9Xhu1mRmlONrG', 1);
--qwerty
INSERT INTO users (id, username, password, role_id) 
VALUES (2, 'notimportant', '$2a$10$tk2WgLKdA6dVwfc27ODJ6.BPcX3GdQDwZDeF3SuDclCWxG8dtBRkC', 2);

INSERT INTO app_version (id, version, platform, release_date, changelog, update_type, active) 
VALUES (1, '0.0.1a', 'ANDROID', '2025-01-01 10:00:00', 'Initial Release', 'DEPRECATED', true);
INSERT INTO app_version (id, version, platform, release_date, changelog, update_type, active) 
VALUES (2, '0.0.2a', 'ANDROID', '2025-02-01 12:00:00', 'Bug fixes', 'MANDATORY', true);
INSERT INTO app_version (id, version, platform, release_date, changelog, update_type, active) 
VALUES (3, '0.0.1i', 'IOS', '2025-01-01 10:00:00', 'Initial Release iOS', 'MANDATORY', true);
INSERT INTO app_version (id, version, platform, release_date, changelog, update_type, active) 
VALUES (4, '0.0.3a', 'ANDROID', '2025-03-01 12:00:00', 'Bug fixes', 'MANDATORY', true);
INSERT INTO app_version (id, version, platform, release_date, changelog, update_type, active) 
VALUES (5, '0.0.4a', 'ANDROID', '2025-04-01 12:00:00', 'Bug fixes', 'OPTIONAL', true);
INSERT INTO app_version (id, version, platform, release_date, changelog, update_type, active) 
VALUES (6, '0.0.5a', 'ANDROID', '2025-05-01 12:00:00', 'Bug fixes', 'UNAVAILABLE', true);

INSERT INTO user_device (id, user_id, platform, current_version, last_seen) 
VALUES (1, 2, 'ANDROID', '0.0.1a', '2025-03-01 09:00:00');
INSERT INTO user_device (id, user_id, platform, current_version, last_seen) 
VALUES (2, 1, 'ANDROID', '0.0.1a', '2025-03-05 10:00:00');
INSERT INTO user_device (id, user_id, platform, current_version, last_seen) 
VALUES (3, 2, 'ANDROID', '0.0.2a', '2025-03-01 09:00:00');
INSERT INTO user_device (id, user_id, platform, current_version, last_seen) 
VALUES (4, 2, 'ANDROID', '0.0.3a', '2025-03-01 09:00:00');
INSERT INTO user_device (id, user_id, platform, current_version, last_seen) 
VALUES (5, 2, 'ANDROID', '0.0.4a', '2025-03-01 09:00:00');
INSERT INTO user_device (id, user_id, platform, current_version, last_seen) 
VALUES (6, 2, 'ANDROID', '0.0.5a', '2025-03-01 09:00:00');

SELECT setval('permission_id_seq', (SELECT MAX(id) FROM permission));
SELECT setval('role_id_seq', (SELECT MAX(id) FROM role));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('app_version_id_seq', (SELECT MAX(id) FROM app_version));
SELECT setval('user_device_id_seq', (SELECT MAX(id) FROM user_device));
SELECT setval('token_id_seq', (SELECT MAX(id) FROM token));
SELECT setval('notification_id_seq', (SELECT MAX(id) FROM notification));