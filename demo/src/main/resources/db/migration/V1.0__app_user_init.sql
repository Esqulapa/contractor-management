INSERT INTO contractor_management.app_user (id, username, password, roles, enabled)
VALUES (NEXTVAL('user_id_sequence'), 'user', '$2a$12$IWiLN.VUf.hQjPYs.88MTeUCicrAHNblhV1AhxJ68RV.Kky6YqaC6', 'admin', true),
(NEXTVAL('user_id_sequence'), 'user2', '$2a$12$IWiLN.VUf.hQjPYs.88MTeUCicrAHNblhV1AhxJ68RV.Kky6YqaC6', 'user', true);