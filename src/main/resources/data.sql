-- ===========================================
-- RedLedger Test Data
-- ===========================================
-- Passwords are BCrypt hashed. Plain text passwords:
--   admin    -> admin123
--   jsmith   -> password123
--   jdoe     -> password456
-- ===========================================

-- Users (passwords are BCrypt hashed)
INSERT INTO users (id, username, password, email, role, created_at) VALUES
(1, 'admin', '$2b$10$N9pmVzrVnzx3DhqiIU63VOEd2Dglo5lddD33kkVTj2W/gg/GrHh/u', 'admin@redledger.com', 'ROLE_ADMIN', '2024-01-01 00:00:00'),
(2, 'jsmith', '$2b$10$FF5rBkxPwD39Db4fqodDIukFZ9mItg0cjdhEhSIEzB6Cd2g3TbxRa', 'john.smith@email.com', 'ROLE_USER', '2024-01-15 10:30:00'),
(3, 'jdoe', '$2b$10$jEnDFPcrbPX6CVp26lDQa./JG0FlFyRFvm.2lOYwIXnCAM9zI44OG', 'jane.doe@email.com', 'ROLE_USER', '2024-02-01 14:00:00');

-- Accounts
-- Admin accounts
INSERT INTO accounts (id, account_number, account_type, balance, owner_id, created_at) VALUES
(1, 'ACC-1001-CHECKING', 'CHECKING', 50000.00, 1, '2024-01-01 00:00:00'),
(2, 'ACC-1001-SAVINGS', 'SAVINGS', 100000.00, 1, '2024-01-01 00:00:00');

-- John Smith accounts
INSERT INTO accounts (id, account_number, account_type, balance, owner_id, created_at) VALUES
(3, 'ACC-2001-CHECKING', 'CHECKING', 5000.00, 2, '2024-01-15 10:30:00'),
(4, 'ACC-2001-SAVINGS', 'SAVINGS', 15000.00, 2, '2024-01-15 10:30:00');

-- Jane Doe accounts
INSERT INTO accounts (id, account_number, account_type, balance, owner_id, created_at) VALUES
(5, 'ACC-3001-CHECKING', 'CHECKING', 7500.00, 3, '2024-02-01 14:00:00'),
(6, 'ACC-3001-SAVINGS', 'SAVINGS', 25000.00, 3, '2024-02-01 14:00:00');

-- Transactions (sample history)
INSERT INTO transactions (id, source_account_id, destination_account_id, amount, description, status, created_at) VALUES
(1, 3, 5, 250.00, 'Payment for lunch', 'COMPLETED', '2024-03-01 12:00:00'),
(2, 5, 3, 100.00, 'Reimbursement', 'COMPLETED', '2024-03-05 09:30:00'),
(3, 1, 3, 1000.00, 'Bonus payment', 'COMPLETED', '2024-03-10 15:00:00'),
(4, 4, 6, 500.00, 'Savings transfer', 'COMPLETED', '2024-03-15 11:00:00'),
(5, 3, 1, 200.00, 'Service fee', 'COMPLETED', '2024-03-20 16:30:00'),
(6, 5, 4, 75.00, 'Shared expense', 'COMPLETED', '2024-03-25 10:00:00'),
(7, 6, 2, 1500.00, 'Investment transfer', 'COMPLETED', '2024-04-01 08:00:00');

-- Advance sequences past the seeded data so Hibernate-generated IDs don't collide
ALTER TABLE users        ALTER COLUMN id RESTART WITH 100;
ALTER TABLE accounts     ALTER COLUMN id RESTART WITH 100;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 100;
