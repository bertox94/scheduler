CREATE OR REPLACE FUNCTION generate_order_occurrences()
RETURNS VOID AS $$
DECLARE
    rec RECORD;                       -- Variable to hold each row from repeatedorder
    planned_date DATE;                -- Variable for calculating each planned date
    current_date DATE;                -- Variable for tracking the current date in the loop
    end_date DATE;                    -- Calculated end date
BEGIN
    FOR rec IN SELECT * FROM repeatedorder LOOP
        -- Initialize dates based on current order record
        current_date := MAKE_DATE(rec.rinityy, rec.rinitmm, rec.rinitdd);
        end_date := MAKE_DATE(rec.rfinyy, rec.rfinmm, rec.rfindd);

        -- Loop until the planned date exceeds the end date
        WHILE current_date <= end_date LOOP
            -- Insert the occurrence into the new table
            INSERT INTO transaction (order_id, amount, planned_date)
            VALUES (rec.id, rec.amount, current_date);

            -- Increment the planned date based on the frequency type
            IF rec.f2 = 'days' THEN
                current_date := current_date + INTERVAL '1 day' * rec.f1;
            ELSIF rec.f2 = 'months' THEN
                current_date := current_date + INTERVAL '1 month' * rec.f1;
            ELSIF rec.f2 = 'years' THEN
                current_date := current_date + INTERVAL '1 year' * rec.f1;
            ELSE
                RAISE EXCEPTION 'Invalid frequency type on order ID %', rec.id;
            END IF;
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Order occurrences generated successfully.';
END;
$$ LANGUAGE plpgsql;