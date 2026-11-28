USE kaamverse;

INSERT IGNORE INTO complaint (complaint_id, reference_no, maid_id, hire_id, problem_type, urgency, transcript,
    incident_date, status, filed_date) VALUES
    (101, 'CMP-20260101', 2, 2, 'Sexual harassment', 'High',
     'Demo: the employer''s relative made unwanted comments and touched me.', '2026-11-20', 'Filed', '2026-11-21'),
    (102, 'CMP-20260102', 1, 3, 'Verbal abuse', 'Normal',
     'Demo: she shouts at me in front of guests every week.', '2026-09-10', 'Filed', '2026-11-22');

INSERT IGNORE INTO complaint (complaint_id, reference_no, maid_id, hire_id, problem_type, urgency, transcript,
    incident_date, status, filed_date) VALUES
    (103, 'CMP-20260103', 2, 4, 'Threats or intimidation', 'High',
     'Demo: she threatened to stop my pay if I told anyone.', '2026-06-15', 'UnderReview', '2026-11-23');

INSERT INTO investigation_note (complaint_id, officer_id, note_text)
SELECT 103, officer_id, 'Demo: called the maid, details match. Waiting for more evidence.'
FROM officer WHERE username = 'complaints'
  AND NOT EXISTS (SELECT 1 FROM investigation_note WHERE complaint_id = 103);