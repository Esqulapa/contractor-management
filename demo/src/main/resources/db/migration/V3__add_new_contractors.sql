INSERT INTO contractor (id, first_name, last_name, salary, overtime_multiplier, contractor_price)
VALUES
  (NEXTVAL('contractor_id_sequence'), 'Mark', 'Marucha', 5000.0, 1.5, 10000.0),
  (NEXTVAL('contractor_id_sequence'), 'Arnold', 'Bakon', 10000.0, 1.5, 15000.0),
  (NEXTVAL('contractor_id_sequence'), 'Helen', 'Paździoch', 7000.0, 1.5, 12000.0);