INSERT INTO contractor (
    id,
    first_name,
    last_name,
    contract_type,
    monthly_earnings,
    hourly_rate,
    monthly_hour_limit,
    contractor_hour_price,
    is_overtime_paid,
    overtime_multiplier
    )
VALUES
  (NEXTVAL('contractor_id_sequence'), 'Mark', 'Marucha', 'CONTRACT_OF_EMPLOYMENT' ,5006.4, 29.8,168,40.0,true, 1.5),
  (NEXTVAL('contractor_id_sequence'), 'Arnold', 'Bakon', 'CONTRACT_OF_MANDATE' ,6048.0, 36.0,168, 50.0,false, 1.5),
  (NEXTVAL('contractor_id_sequence'), 'Helen', 'Paździoch', 'CONTRACT_B2B' ,10001.04, 59.53,168, 80.0,true, 1.5);