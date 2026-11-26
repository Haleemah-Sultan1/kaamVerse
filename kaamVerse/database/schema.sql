-- KaamVerse schema. Safe to run again: no tables or rows are dropped.
-- The upgrade section at the end brings Batch 1 tables up to date.
CREATE DATABASE IF NOT EXISTS kaamverse CHARACTER SET utf8mb4;
USE kaamverse;

CREATE TABLE IF NOT EXISTS officer (
    officer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    role ENUM('WAGE_STANDARDS','VERIFICATION','WELFARE_CASE','REVIEW_MODERATOR','COMPLAINT_REVIEW') NOT NULL
);

CREATE TABLE IF NOT EXISTS locality (
    locality_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS street (
    street_id INT AUTO_INCREMENT PRIMARY KEY,
    locality_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    UNIQUE (locality_id, name),
    FOREIGN KEY (locality_id) REFERENCES locality(locality_id)
);

CREATE TABLE IF NOT EXISTS service (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS maid (
    maid_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    father_name VARCHAR(100) NOT NULL,
    cnic VARCHAR(15) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    pin VARCHAR(64) NOT NULL,
    cnic_image_path VARCHAR(255),
    status ENUM('PendingVerification','Verified','Rejected') NOT NULL DEFAULT 'PendingVerification',
    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0,
    street_id INT NOT NULL,
    registered_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    FOREIGN KEY (street_id) REFERENCES street(street_id)
);

CREATE TABLE IF NOT EXISTS maid_service (
    maid_id INT NOT NULL,
    service_id INT NOT NULL,
    years INT NOT NULL,
    PRIMARY KEY (maid_id, service_id),
    FOREIGN KEY (maid_id) REFERENCES maid(maid_id),
    FOREIGN KEY (service_id) REFERENCES service(service_id)
);

CREATE TABLE IF NOT EXISTS verification_request (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    maid_id INT NOT NULL,
    status ENUM('Pending','Matched','Rejected') NOT NULL DEFAULT 'Pending',
    rejection_reason VARCHAR(500),
    verified_by_officer_id INT,
    verified_date_time DATETIME,
    submitted_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    FOREIGN KEY (maid_id) REFERENCES maid(maid_id),
    FOREIGN KEY (verified_by_officer_id) REFERENCES officer(officer_id)
);

CREATE TABLE IF NOT EXISTS availability_slot (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    maid_id INT NOT NULL,
    day_of_week ENUM('MON','TUE','WED','THU','FRI','SAT','SUN') NOT NULL,
    time_slot ENUM('MORNING','AFTERNOON','EVENING') NOT NULL,
    is_booked BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (maid_id, day_of_week, time_slot),
    FOREIGN KEY (maid_id) REFERENCES maid(maid_id)
);

CREATE TABLE IF NOT EXISTS family_member (
    family_member_id INT AUTO_INCREMENT PRIMARY KEY,
    maid_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    relation VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    cnic_or_bform VARCHAR(15),
    is_dependent BOOLEAN NOT NULL DEFAULT FALSE,
    school VARCHAR(150),
    contact_number VARCHAR(20),
    FOREIGN KEY (maid_id) REFERENCES maid(maid_id)
);

CREATE TABLE IF NOT EXISTS emergency_contact (
    contact_id INT AUTO_INCREMENT PRIMARY KEY,
    maid_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    relation VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    FOREIGN KEY (maid_id) REFERENCES maid(maid_id)
);

CREATE TABLE IF NOT EXISTS hiring_woman (
    hiring_woman_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    pin VARCHAR(64) NOT NULL,
    is_hiring_suspended BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS hire (
    hire_id INT AUTO_INCREMENT PRIMARY KEY,
    maid_id INT NOT NULL,
    hiring_woman_id INT NOT NULL,
    working_hours VARCHAR(100) NOT NULL,
    request_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    start_date DATE,
    end_date DATE,
    status ENUM('Pending','Active','Ended','Rejected') NOT NULL DEFAULT 'Pending',
    calculated_wage DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (maid_id) REFERENCES maid(maid_id),
    FOREIGN KEY (hiring_woman_id) REFERENCES hiring_woman(hiring_woman_id)
);

CREATE TABLE IF NOT EXISTS hire_service (
    hire_id INT NOT NULL,
    service_id INT NOT NULL,
    quantity DECIMAL(8,2) NOT NULL,
    unit ENUM('PER_HOUR','PER_DAY','PER_VISIT') NOT NULL,
    wage DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (hire_id, service_id),
    FOREIGN KEY (hire_id) REFERENCES hire(hire_id),
    FOREIGN KEY (service_id) REFERENCES service(service_id)
);

CREATE TABLE IF NOT EXISTS wage_band (
    band_id INT AUTO_INCREMENT PRIMARY KEY,
    service_id INT NOT NULL,
    min_rate DECIMAL(10,2) NOT NULL,
    max_rate DECIMAL(10,2) NOT NULL,
    unit ENUM('PER_HOUR','PER_DAY','PER_VISIT') NOT NULL,
    effective_date DATE NOT NULL,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK (min_rate > 0 AND max_rate >= min_rate),
    FOREIGN KEY (service_id) REFERENCES service(service_id)
);

CREATE TABLE IF NOT EXISTS review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    hire_id INT NOT NULL UNIQUE,
    overall_rating INT NOT NULL,
    punctuality INT NOT NULL,
    honesty INT NOT NULL,
    cleanliness INT NOT NULL,
    behaviour INT NOT NULL,
    would_rehire BOOLEAN NOT NULL DEFAULT FALSE,
    comment VARCHAR(1000),
    is_flagged BOOLEAN NOT NULL DEFAULT FALSE,
    status ENUM('Pending','Approved','Rejected') NOT NULL DEFAULT 'Pending',
    created_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    CHECK (overall_rating BETWEEN 1 AND 5),
    CHECK (punctuality BETWEEN 1 AND 5),
    CHECK (honesty BETWEEN 1 AND 5),
    CHECK (cleanliness BETWEEN 1 AND 5),
    CHECK (behaviour BETWEEN 1 AND 5),
    FOREIGN KEY (hire_id) REFERENCES hire(hire_id)
);

CREATE TABLE IF NOT EXISTS trust_badge (
    badge_id INT AUTO_INCREMENT PRIMARY KEY,
    maid_id INT NOT NULL UNIQUE,
    awarded_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    reason VARCHAR(500) NOT NULL,
    awarded_by_officer_id INT NOT NULL,
    FOREIGN KEY (maid_id) REFERENCES maid(maid_id),
    FOREIGN KEY (awarded_by_officer_id) REFERENCES officer(officer_id)
);

CREATE TABLE IF NOT EXISTS funding_request (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    maid_id INT NOT NULL,
    tracking_no VARCHAR(30) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    amount_needed DECIMAL(10,2) NOT NULL,
    amount_received DECIMAL(10,2) NOT NULL DEFAULT 0,
    event_date DATE NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status ENUM('Pending','PartiallyFunded','Funded') NOT NULL DEFAULT 'Pending',
    created_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    CHECK (amount_needed > 0),
    FOREIGN KEY (maid_id) REFERENCES maid(maid_id)
);

CREATE TABLE IF NOT EXISTS funding_recipient (
    request_id INT NOT NULL,
    hiring_woman_id INT NOT NULL,
    PRIMARY KEY (request_id, hiring_woman_id),
    FOREIGN KEY (request_id) REFERENCES funding_request(request_id),
    FOREIGN KEY (hiring_woman_id) REFERENCES hiring_woman(hiring_woman_id)
);

CREATE TABLE IF NOT EXISTS contribution (
    contribution_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    hiring_woman_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    note VARCHAR(500),
    contribution_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    acknowledgement_no VARCHAR(30) NOT NULL UNIQUE,
    CHECK (amount > 0),
    FOREIGN KEY (request_id) REFERENCES funding_request(request_id),
    FOREIGN KEY (hiring_woman_id) REFERENCES hiring_woman(hiring_woman_id)
);

CREATE TABLE IF NOT EXISTS complaint (
    complaint_id INT AUTO_INCREMENT PRIMARY KEY,
    reference_no VARCHAR(30) NOT NULL UNIQUE,
    maid_id INT NOT NULL,
    hire_id INT NOT NULL,
    problem_type VARCHAR(100) NOT NULL,
    urgency VARCHAR(30) NOT NULL,
    transcript TEXT,
    audio_path VARCHAR(255),
    evidence_path VARCHAR(255),
    incident_date DATE NOT NULL,
    status ENUM('Filed','UnderReview','Escalated','Closed') NOT NULL DEFAULT 'Filed',
    outcome VARCHAR(500),
    clarification_message VARCHAR(1000),
    filed_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    FOREIGN KEY (maid_id) REFERENCES maid(maid_id),
    FOREIGN KEY (hire_id) REFERENCES hire(hire_id)
);

CREATE TABLE IF NOT EXISTS investigation_note (
    note_id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL,
    officer_id INT NOT NULL,
    note_text TEXT NOT NULL,
    note_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaint(complaint_id),
    FOREIGN KEY (officer_id) REFERENCES officer(officer_id)
);

CREATE TABLE IF NOT EXISTS referral_report (
    referral_id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL UNIQUE,
    authority VARCHAR(150) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    generated_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    FOREIGN KEY (complaint_id) REFERENCES complaint(complaint_id)
);

CREATE TABLE IF NOT EXISTS audit_log_entry (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    officer_id INT NOT NULL,
    action VARCHAR(255) NOT NULL,
    logged_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (officer_id) REFERENCES officer(officer_id)
);

CREATE TABLE IF NOT EXISTS notification (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_type ENUM('MAID','HIRING_WOMAN','OFFICER') NOT NULL,
    recipient_id INT NOT NULL,
    message_text VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notification_recipient (recipient_type, recipient_id, is_read)
);

-- Upgrade from Batch 1 (does nothing on an up-to-date database)
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'hire_service' AND column_name = 'quantity') = 0,
    'ALTER TABLE hire_service ADD COLUMN quantity DECIMAL(8,2) NOT NULL, ADD COLUMN unit ENUM(\'PER_HOUR\',\'PER_DAY\',\'PER_VISIT\') NOT NULL, ADD COLUMN wage DECIMAL(10,2) NOT NULL', 'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'hire' AND column_name = 'unit') > 0,
    'ALTER TABLE hire DROP COLUMN unit', 'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'maid' AND column_name = 'phone' AND non_unique = 0) = 0,
    'ALTER TABLE maid ADD UNIQUE (phone)', 'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'hiring_woman' AND column_name = 'phone' AND non_unique = 0) = 0,
    'ALTER TABLE hiring_woman ADD UNIQUE (phone)', 'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
