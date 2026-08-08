-- Owner: M1. Applied after V1__init_access_schema.sql.
-- Applied migrations must NEVER be edited; add a new V{n} file for changes.
--
-- Java-side constant source: com.kartyavya.access.config.SeedData
-- RoutingSeedValidationIT verifies that these literal strings match SeedData constants at test time.
-- Any drift between this file and SeedData.java will cause RoutingSeedValidationIT to fail loudly.

-- ────────────────────────────────────────────────────────────────────────────────
-- Departments (4 frozen departments)
-- Guard: WHERE NOT EXISTS makes the inserts idempotent on re-run.
-- ────────────────────────────────────────────────────────────────────────────────

INSERT INTO departments (name, contact_email, enabled, created_at, updated_at)
SELECT 'Roads & Infrastructure', 'roads@kartyavya.local', true, NOW(6), NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM departments WHERE LOWER(TRIM(name)) = LOWER(TRIM('Roads & Infrastructure'))
);

INSERT INTO departments (name, contact_email, enabled, created_at, updated_at)
SELECT 'Sanitation', 'sanitation@kartyavya.local', true, NOW(6), NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM departments WHERE LOWER(TRIM(name)) = LOWER(TRIM('Sanitation'))
);

INSERT INTO departments (name, contact_email, enabled, created_at, updated_at)
SELECT 'Water Supply', 'water@kartyavya.local', true, NOW(6), NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM departments WHERE LOWER(TRIM(name)) = LOWER(TRIM('Water Supply'))
);

INSERT INTO departments (name, contact_email, enabled, created_at, updated_at)
SELECT 'General Administration', 'general@kartyavya.local', true, NOW(6), NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM departments WHERE LOWER(TRIM(name)) = LOWER(TRIM('General Administration'))
);

-- ────────────────────────────────────────────────────────────────────────────────
-- Routing Rules (5 frozen rules, resolved via deterministic ORDER BY id ASC LIMIT 1)
-- Guard: WHERE NOT EXISTS on category makes each insert idempotent.
-- ────────────────────────────────────────────────────────────────────────────────

INSERT INTO routing_rules (category, department_id, active, created_at, updated_at)
SELECT 'POTHOLE',
       (SELECT id FROM departments WHERE LOWER(TRIM(name)) = LOWER(TRIM('Roads & Infrastructure')) ORDER BY id ASC LIMIT 1),
       true, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM routing_rules WHERE category = 'POTHOLE');

INSERT INTO routing_rules (category, department_id, active, created_at, updated_at)
SELECT 'STREETLIGHT',
       (SELECT id FROM departments WHERE LOWER(TRIM(name)) = LOWER(TRIM('Roads & Infrastructure')) ORDER BY id ASC LIMIT 1),
       true, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM routing_rules WHERE category = 'STREETLIGHT');

INSERT INTO routing_rules (category, department_id, active, created_at, updated_at)
SELECT 'GARBAGE',
       (SELECT id FROM departments WHERE LOWER(TRIM(name)) = LOWER(TRIM('Sanitation')) ORDER BY id ASC LIMIT 1),
       true, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM routing_rules WHERE category = 'GARBAGE');

INSERT INTO routing_rules (category, department_id, active, created_at, updated_at)
SELECT 'WATER_LEAKAGE',
       (SELECT id FROM departments WHERE LOWER(TRIM(name)) = LOWER(TRIM('Water Supply')) ORDER BY id ASC LIMIT 1),
       true, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM routing_rules WHERE category = 'WATER_LEAKAGE');

INSERT INTO routing_rules (category, department_id, active, created_at, updated_at)
SELECT 'OTHER',
       (SELECT id FROM departments WHERE LOWER(TRIM(name)) = LOWER(TRIM('General Administration')) ORDER BY id ASC LIMIT 1),
       true, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM routing_rules WHERE category = 'OTHER');
