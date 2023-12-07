
CREATE SEQUENCE contractor_id_sequence
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807 ;

CREATE TABLE contractor
    (
    id INTEGER DEFAULT nextval('contractor_id_sequence') PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    salary DOUBLE PRECISION
    );

CREATE SEQUENCE contractor_billing_id_sequence
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807 ;

CREATE TABLE contractor_billing
    (
    id INTEGER DEFAULT nextval('contractor_billing_id_sequence') PRIMARY KEY,
    contractor_id INTEGER REFERENCES contractor(id),
    worked_hours DOUBLE PRECISION,
    year INTEGER,
    month SMALLINT CHECK (month >= 0 AND month <= 11),
    payment NUMERIC(19, 2),

    CONSTRAINT fk_contractor_billing_contractor
            FOREIGN KEY (contractor_id)
            REFERENCES contractor_management.contractor (id)
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
    );

CREATE SEQUENCE user_id_sequence
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE TABLE app_user
    (
        id INTEGER DEFAULT nextval('user_id_sequence') PRIMARY KEY,
        username VARCHAR(255),
        password VARCHAR(255),
        roles VARCHAR(255),
        enabled BOOLEAN NOT NULL

    );