-- KaamVerse demo data (test data only). Run after schema.sql.
-- Safe to run again: rows that already exist are skipped.
USE kaamverse;

-- Officers. Password for all: officer123 (stored as SHA-256 hex).
INSERT IGNORE INTO officer (name, username, password_hash, role) VALUES
    ('Demo Wage Officer',         'wage',       SHA2('officer123', 256), 'WAGE_STANDARDS'),
    ('Demo Verification Officer', 'verify',     SHA2('officer123', 256), 'VERIFICATION'),
    ('Demo Welfare Officer',      'welfare',    SHA2('officer123', 256), 'WELFARE_CASE'),
    ('Demo Review Moderator',     'moderator',  SHA2('officer123', 256), 'REVIEW_MODERATOR'),
    ('Demo Complaint Officer',    'complaints', SHA2('officer123', 256), 'COMPLAINT_REVIEW');

INSERT IGNORE INTO locality (locality_id, name) VALUES (1, 'Gulshan-e-Iqbal'), (2, 'North Nazimabad');
INSERT IGNORE INTO street (street_id, locality_id, name) VALUES
    (1, 1, 'Street 1'), (2, 1, 'Street 2'), (3, 2, 'Street 1');
INSERT IGNORE INTO service (service_id, name) VALUES (1, 'Cleaning'), (2, 'Cooking'), (3, 'Laundry');

-- Current wage bands: wage = max_rate x quantity
INSERT IGNORE INTO wage_band (band_id, service_id, min_rate, max_rate, unit, effective_date) VALUES
    (1, 1, 200, 300, 'PER_HOUR',  '2026-01-01'),
    (2, 2, 800, 1200, 'PER_DAY',  '2026-01-01'),
    (3, 3, 300, 500, 'PER_VISIT', '2026-01-01');

-- PINs are listed in README.md
INSERT IGNORE INTO maid (maid_id, name, father_name, cnic, phone, address, pin, status, street_id) VALUES
    (1, 'Demo Maid One',   'Demo Father', '00000-0000001-0', '03000000001', 'House 1', SHA2('1111', 256), 'Verified', 1),
    (2, 'Demo Maid Two',   'Demo Father', '00000-0000002-0', '03000000002', 'House 2', SHA2('2222', 256), 'Verified', 2),
    (3, 'Demo Maid Three', 'Demo Father', '00000-0000003-0', '03000000003', 'House 3', SHA2('3333', 256), 'PendingVerification', 3);

INSERT IGNORE INTO maid_service (maid_id, service_id, years) VALUES
    (1, 1, 3), (1, 2, 5), (1, 3, 2), (2, 1, 1), (2, 3, 4), (3, 2, 2);

INSERT IGNORE INTO verification_request (request_id, maid_id, status, verified_by_officer_id, verified_date_time)
SELECT 1, 1, 'Matched', officer_id, NOW() FROM officer WHERE username = 'verify';
INSERT IGNORE INTO verification_request (request_id, maid_id, status, verified_by_officer_id, verified_date_time)
SELECT 2, 2, 'Matched', officer_id, NOW() FROM officer WHERE username = 'verify';
INSERT IGNORE INTO verification_request (request_id, maid_id, status) VALUES (3, 3, 'Pending');

INSERT IGNORE INTO hiring_woman (hiring_woman_id, name, phone, address, pin) VALUES
    (1, 'Demo Hiring Woman One', '03110000001', 'Flat 1', SHA2('4444', 256)),
    (2, 'Demo Hiring Woman Two', '03110000002', 'Flat 2', SHA2('5555', 256));

-- calculated_wage is the sum of the hire_service wages below
INSERT IGNORE INTO hire (hire_id, maid_id, hiring_woman_id, working_hours, request_date, start_date, end_date, status, calculated_wage) VALUES
    (1, 1, 1, '9am to 12pm', '2026-11-20', NULL,         NULL,         'Pending', 2400),
    (2, 2, 1, '2pm to 4pm',  '2026-10-01', '2026-10-05', NULL,         'Active',  3400),
    (3, 1, 2, '9am to 1pm',  '2026-08-01', '2026-08-05', '2026-09-30', 'Ended',   5400);

INSERT IGNORE INTO hire_service (hire_id, service_id, quantity, unit, wage) VALUES
    (1, 1, 4, 'PER_HOUR',  1200),   -- 300 x 4
    (1, 2, 1, 'PER_DAY',   1200),   -- 1200 x 1
    (2, 1, 8, 'PER_HOUR',  2400),   -- 300 x 8
    (2, 3, 2, 'PER_VISIT', 1000),   -- 500 x 2
    (3, 2, 2, 'PER_DAY',   2400),   -- 1200 x 2
    (3, 3, 6, 'PER_VISIT', 3000);   -- 500 x 6
