ALTER TABLE contractor_billing
RENAME COLUMN payment TO contractor_remuneration;

ALTER TABLE contractor_billing
ADD COLUMN client_charge NUMERIC(19, 2),
ADD COLUMN profit NUMERIC(19, 2);