USE kaamverse;

INSERT IGNORE INTO availability_slot (maid_id, day_of_week, time_slot, is_booked) VALUES
    (1, 'MON', 'MORNING', FALSE), (1, 'MON', 'AFTERNOON', FALSE), (1, 'WED', 'MORNING', FALSE),
    (1, 'FRI', 'EVENING', FALSE), (2, 'TUE', 'MORNING', FALSE), (2, 'WED', 'AFTERNOON', FALSE),
    (2, 'SAT', 'MORNING', FALSE);

INSERT IGNORE INTO review (review_id, hire_id, overall_rating, punctuality, honesty, cleanliness, behaviour,
    would_rehire, comment, is_flagged, status) VALUES
    (1, 3, 5, 5, 5, 4, 5, TRUE, 'Honest and always on time.', FALSE, 'Approved');

UPDATE maid SET average_rating = 5.00 WHERE maid_id = 1;

INSERT IGNORE INTO trust_badge (maid_id, reason, awarded_by_officer_id)
SELECT 1, 'Demo badge for testing', officer_id FROM officer WHERE username = 'moderator';