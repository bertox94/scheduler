CREATE OR REPLACE FUNCTION generate_order_occurrences(
    end_prev DATE
)
    RETURNS VOID AS
$$
DECLARE
    rec          RECORD; -- Variable to hold each row from repeatedorder
    init_date    DATE; -- Variable for calculating the date in the loop
    curr_date    DATE; -- Variable for tracking the current date in the loop
    end_date     DATE; -- Calculated end date
    iter         INT; --iteration number
BEGIN
    delete from public.transaction;
    FOR rec IN SELECT * FROM repeatedorder
        LOOP
            IF rec.f2 = 'days' THEN
                curr_date := curr_date + INTERVAL '1 day' * rec.f1 * iter;
            ELSIF rec.f2 = 'months' THEN
                IF rec.f3 = 'eom' THEN
                    init_date := MAKE_DATE(rec.rinityy, rec.rinitmm, 1);
                    init_date = (date_trunc('month', init_date) + interval '1 month - 1 day')::date;

                    end_date = MAKE_DATE(rec.rfinyy, rec.rfinmm, 1);
                    init_date = (date_trunc('month', end_date) + interval '1 month - 1 day')::date;
                ELSE
                    init_date = MAKE_DATE(rec.rinityy, rec.rinitmm, rec.rdd);
                    end_date = MAKE_DATE(rec.rfinyy, rec.rfinmm, rec.rdd);
                END IF;
            ELSIF rec.f2 = 'years' THEN
                curr_date := curr_date + INTERVAL '1 year' * rec.f1 * iter;
            ELSE
                RAISE EXCEPTION 'Invalid frequency type on order ID %', rec.id;
            END IF;

            end_date= LEAST(end_date,end_prev);

            -- Loop until the planned date exceeds the end date
            iter := 1;
            curr_date = init_date;
            WHILE curr_date <= end_date
                LOOP
                    -- Insert the occurrence into the new table
                    INSERT INTO public.transaction (orderid, descr, executiondate, amount)
                    VALUES (rec.id, rec.descr, curr_date, rec.amount);

                    -- Increment the planned date based on the frequency type
                    IF rec.f2 = 'days' THEN
                        curr_date := curr_date + INTERVAL '1 day' * rec.f1 * iter;
                    ELSIF rec.f2 = 'months' THEN
                        curr_date = init_date + INTERVAL '1 month' * rec.f1 * iter;
                        IF rec.f3 = 'eom' THEN
                            curr_date = (date_trunc('month', curr_date) + interval '1 month - 1 day')::date;
                        END IF;
                    ELSIF rec.f2 = 'years' THEN
                        curr_date := curr_date + INTERVAL '1 year' * rec.f1 * iter;
                    ELSE
                        RAISE EXCEPTION 'Invalid frequency type on order ID %', rec.id;
                    END IF;
                    iter := iter + 1;
                    end_date= LEAST(end_date,end_prev);
                END LOOP;
        END LOOP;

    RAISE NOTICE 'Order occurrences generated successfully.';
END;
$$ LANGUAGE plpgsql;
