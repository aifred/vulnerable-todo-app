-- Demo seed data, loaded automatically alongside schema.sql on startup
-- (H2 in-memory DB, recreated fresh every run).
--
-- Password hashes are SHA-256(password + "supersecret123"), matching
-- CryptoUtil#hashPassword. Plaintext demo passwords, for logging in:
--   alice / alicepass
--   bob   / bobsecret
--   carol / carol123
--   dave  / dave2023
--   admin / admin123

SET @alice = 'alice';
SET @bob   = 'bob';
SET @carol = 'carol';
SET @dave  = 'dave';
SET @admin = 'admin';

MERGE INTO users (username, password_hash) VALUES
    (@alice, 'da33cca5a2625307f4b3d0a646dd59266f578297c8e08cb0c7b2801bda7e0441'),
    (@bob,   'caf7b270ca3e50485f9cc5370621a8079020f5e49cdfdafe0bdf0bc440ca7d99'),
    (@carol, 'c7879abe51ebbf5ecfa99253ae3b74aa0eec99322dbd800c2ecd7371aae73d4e'),
    (@dave,  'ffdf0edf01d200b29119c5d97357211bc51dd527076b5d711fbdc67fd69d0038'),
    (@admin, '2839f6a25fba96b20541b5cd7e56e809d4642dd8012d936fc9be20f13d9419e3');

MERGE INTO profiles (username, bio, avatar_url, favorite_color) VALUES
    (@alice, 'Product manager who lives in spreadsheets and to-do lists.', 'https://i.pravatar.cc/150?u=alice', 'teal'),
    (@bob,   'Backend engineer. If it compiles, it ships.', 'https://i.pravatar.cc/150?u=bob', 'navy'),
    (@carol, 'Designer, plant parent, chronic list-maker.', 'https://i.pravatar.cc/150?u=carol', 'coral'),
    (@dave,  'QA engineer. I break things so you do not have to.', 'https://i.pravatar.cc/150?u=dave', 'slate'),
    (@admin, 'System administrator.', 'https://i.pravatar.cc/150?u=admin', 'crimson');

INSERT INTO todos (title, description, done, owner, attachment_path)
SELECT * FROM (VALUES
    ('Draft Q3 roadmap', 'Outline priorities for product review meeting.', FALSE, @alice, NULL),
    ('Review design mockups', 'Feedback due before Friday standup.', FALSE, @alice, 'uploads/alice/mockups.pdf'),
    ('Book flight to conference', 'Compare fares, prefer direct flights.', TRUE, @alice, NULL),
    ('Follow up with marketing', 'They need the feature list for the launch email.', FALSE, @alice, NULL),
    ('Renew domain name', 'Expires next month, auto-renew failed last time.', TRUE, @alice, NULL),
    ('Fix flaky integration test', 'TodoControllerTest#testCreate fails intermittently on CI.', FALSE, @bob, NULL),
    ('Upgrade Spring Boot version', 'Check changelog for breaking changes first.', FALSE, @bob, NULL),
    ('Set up nightly backup job', 'Cron job keeps failing silently, add alerting.', FALSE, @bob, 'uploads/bob/backup-notes.txt'),
    ('Pair with Carol on onboarding flow', NULL, TRUE, @bob, NULL),
    ('Clean up unused database indexes', 'Found three that are never hit by the query planner.', TRUE, @bob, NULL),
    ('Redesign empty states', 'Current ones just show a blank white box.', FALSE, @carol, NULL),
    ('User interview recap', 'Five sessions done, write up common themes.', FALSE, @carol, 'uploads/carol/interview-notes.docx'),
    ('Update style guide', 'New color palette needs to be documented.', TRUE, @carol, NULL),
    ('Prep demo for stakeholders', 'Walkthrough of the new dashboard widgets.', FALSE, @carol, NULL),
    ('Write regression test plan', 'Cover login, todo CRUD, and profile update flows.', FALSE, @dave, NULL),
    ('Retest attachment upload bug', 'Confirm the path traversal fix actually holds.', FALSE, @dave, NULL),
    ('Automate smoke tests', 'Wire into the build pipeline before next release.', TRUE, @dave, NULL),
    ('Log defects from last sprint', 'Triage with the team on Monday.', FALSE, @dave, NULL),
    ('Rotate service account credentials', 'Quarterly security housekeeping task.', FALSE, @admin, NULL),
    ('Review access logs for anomalies', 'Spot-check after last week''s incident.', TRUE, @admin, NULL)
) AS v(title, description, done, owner, attachment_path)
WHERE NOT EXISTS (SELECT 1 FROM todos t WHERE t.title = v.title AND t.owner = v.owner);
