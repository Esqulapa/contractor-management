
SELECT setval('contractor_billing_id_sequence', 1, false);

INSERT INTO contractor_billing(

id,
contractor_id,
worked_hours,
year,
month,
contractor_remuneration,
client_charge,
profit
)
VALUES
(NEXTVAL('contractor_billing_id_sequence'),1,168,2024,'FEBRUARY',5006.4,6720.0,1713.6),
(NEXTVAL('contractor_billing_id_sequence'),2,168,2024,'FEBRUARY',6048.0,8400.0,2352.0),
(NEXTVAL('contractor_billing_id_sequence'),3,168,2024,'FEBRUARY',10001.04,13440.0,3438.96);

