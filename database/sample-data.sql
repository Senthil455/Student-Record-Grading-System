-- =====================================================================
-- Scholaris: demonstration data (9 students, 4 instructors, 6 courses, 40 enrollments)
-- MySQL 8.x. Run after database/schema.sql:
--   mysql -u root -p student_grading < database/sample-data.sql
--
-- Login accounts are intentionally not part of the schema; this project is
-- a record-management mini-project and does not implement authentication.
-- =====================================================================

USE student_grading;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE courses;
TRUNCATE TABLE students;
TRUNCATE TABLE instructors;
TRUNCATE TABLE persons;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- Instructors (persons.id 1-4 + instructors rows)
-- ---------------------------------------------------------------------
INSERT INTO persons (id, first_name, last_name, email, phone, date_of_birth, created_at) VALUES
  (1, 'Anitha', 'Raman', 'anitha.raman@scholaris.edu', '9840012345', NULL, '2026-07-01 09:00:00'),
  (2, 'Karthik', 'Subramanian', 'karthik.subramanian@scholaris.edu', '9840012346', NULL, '2026-07-01 09:00:00'),
  (3, 'Meera', 'Nair', 'meera.nair@scholaris.edu', '9840012347', NULL, '2026-07-01 09:00:00'),
  (4, 'Rahul', 'Verma', 'rahul.verma@scholaris.edu', '9840012348', NULL, '2026-07-01 09:00:00');

INSERT INTO instructors (person_id, employee_code, designation, specialization) VALUES
  (1, 'EMP1001', 'Professor', 'Data Structures & Algorithms'),
  (2, 'EMP1002', 'Associate Professor', 'Web Technologies'),
  (3, 'EMP1003', 'Assistant Professor', 'Database Systems'),
  (4, 'EMP1004', 'Assistant Professor', 'Software Engineering');

-- ---------------------------------------------------------------------
-- Students (persons.id 5-13 + students rows)
-- ---------------------------------------------------------------------
INSERT INTO persons (id, first_name, last_name, email, phone, date_of_birth, created_at) VALUES
  (5, 'Aarav', 'Sharma', 'aarav.sharma@student.scholaris.edu', NULL, '2005-04-12', '2026-07-15 10:00:00'),
  (6, 'Diya', 'Patel', 'diya.patel@student.scholaris.edu', NULL, '2005-08-03', '2026-07-15 10:00:00'),
  (7, 'Ishaan', 'Reddy', 'ishaan.reddy@student.scholaris.edu', NULL, '2004-12-21', '2026-07-15 10:00:00'),
  (8, 'Ananya', 'Iyer', 'ananya.iyer@student.scholaris.edu', NULL, '2005-02-17', '2026-07-15 10:00:00'),
  (9, 'Vihaan', 'Gupta', 'vihaan.gupta@student.scholaris.edu', NULL, '2004-10-09', '2026-07-15 10:00:00'),
  (10, 'Kavya', 'Menon', 'kavya.menon@student.scholaris.edu', NULL, '2005-06-28', '2026-07-15 10:00:00'),
  (11, 'Arjun', 'Nair', 'arjun.nair@student.scholaris.edu', NULL, '2005-01-15', '2026-07-15 10:00:00'),
  (12, 'Sneha', 'Kulkarni', 'sneha.kulkarni@student.scholaris.edu', NULL, '2005-09-07', '2026-07-15 10:00:00'),
  (13, 'Rohan', 'Das', 'rohan.das@student.scholaris.edu', NULL, '2006-03-30', '2026-07-15 10:00:00');

INSERT INTO students (person_id, roll_number, department, admission_year, active) VALUES
  (5, '23BCS001', 'B.Sc Computer Science', 2023, TRUE),
  (6, '23BCS002', 'B.Sc Computer Science', 2023, TRUE),
  (7, '23BCS003', 'B.Sc Computer Science', 2023, TRUE),
  (8, '23BCS004', 'B.Sc Computer Science', 2023, TRUE),
  (9, '23BCS005', 'B.Sc Computer Science', 2023, TRUE),
  (10, '23BCS006', 'BCA', 2023, TRUE),
  (11, '23BCS007', 'BCA', 2023, TRUE),
  (12, '23BCS008', 'BCA', 2023, TRUE),
  (13, '23BCS009', 'B.Sc Computer Science', 2024, TRUE);

-- ---------------------------------------------------------------------
-- Courses
-- ---------------------------------------------------------------------
INSERT INTO courses (id, course_code, title, credits, semester, department, description, instructor_id) VALUES
  (1, 'CS101', 'Programming in Java', 4, 1, 'B.Sc Computer Science', 'Object-oriented programming, collections, exceptions and file handling.', 2),
  (2, 'CS102', 'Data Structures', 4, 3, 'B.Sc Computer Science', 'Lists, stacks, queues, trees, graphs and algorithm analysis.', 1),
  (3, 'CS201', 'Database Management Systems', 3, 3, 'BCA', 'Relational model, SQL, normalisation and transaction management.', 3),
  (4, 'CS202', 'Web Programming', 3, 5, 'B.Sc Computer Science', 'HTML, CSS, JavaScript, HTTP and server-side web development.', 2),
  (5, 'MA101', 'Discrete Mathematics', 3, 1, 'BCA', 'Sets, relations, functions, graph theory and combinatorics.', 4),
  (6, 'CS301', 'Software Engineering', 3, 5, 'B.Sc Computer Science', 'Requirements, design, testing, agile delivery and software quality.', 4);

-- ---------------------------------------------------------------------
-- Enrollments (40 awarded results; Rohan Das has no enrollment on purpose,
-- so the "students without enrollments" HQL query returns a real row)
-- ---------------------------------------------------------------------
INSERT INTO enrollments (id, student_id, course_id, marks, grade, grade_point, semester, enrolled_on, graded_by) VALUES
  (1, 5, 1, 92.00, 'A+', 10.00, 1, '2026-10-19', 'seed-data'),
  (2, 5, 2, 88.00, 'A', 9.00, 3, '2026-10-18', 'seed-data'),
  (3, 5, 3, 85.00, 'A', 9.00, 3, '2026-10-17', 'seed-data'),
  (4, 5, 4, 91.00, 'A+', 10.00, 5, '2026-10-16', 'seed-data'),
  (5, 5, 6, 89.00, 'A', 9.00, 5, '2026-10-15', 'seed-data'),
  (6, 6, 1, 84.00, 'B+', 8.00, 1, '2026-10-14', 'seed-data'),
  (7, 6, 2, 79.00, 'B+', 8.00, 3, '2026-10-13', 'seed-data'),
  (8, 6, 3, 88.00, 'A', 9.00, 3, '2026-10-12', 'seed-data'),
  (9, 6, 4, 86.00, 'A', 9.00, 5, '2026-10-11', 'seed-data'),
  (10, 6, 5, 78.00, 'B+', 8.00, 1, '2026-10-10', 'seed-data'),
  (11, 7, 1, 72.00, 'B', 7.00, 1, '2026-10-09', 'seed-data'),
  (12, 7, 2, 68.00, 'B', 7.00, 3, '2026-10-08', 'seed-data'),
  (13, 7, 3, 75.00, 'B+', 8.00, 3, '2026-10-07', 'seed-data'),
  (14, 7, 4, 70.00, 'B', 7.00, 5, '2026-10-06', 'seed-data'),
  (15, 7, 5, 65.00, 'B', 7.00, 1, '2026-10-05', 'seed-data'),
  (16, 8, 1, 95.00, 'A+', 10.00, 1, '2026-10-04', 'seed-data'),
  (17, 8, 2, 91.00, 'A+', 10.00, 3, '2026-10-03', 'seed-data'),
  (18, 8, 3, 93.00, 'A+', 10.00, 3, '2026-10-02', 'seed-data'),
  (19, 8, 4, 96.00, 'A+', 10.00, 5, '2026-10-01', 'seed-data'),
  (20, 8, 6, 94.00, 'A+', 10.00, 5, '2026-09-30', 'seed-data'),
  (21, 9, 1, 58.00, 'C', 6.00, 1, '2026-09-29', 'seed-data'),
  (22, 9, 2, 62.00, 'C', 6.00, 3, '2026-09-28', 'seed-data'),
  (23, 9, 3, 55.00, 'C', 6.00, 3, '2026-09-27', 'seed-data'),
  (24, 9, 4, 60.00, 'C', 6.00, 5, '2026-09-26', 'seed-data'),
  (25, 9, 5, 57.00, 'C', 6.00, 1, '2026-09-25', 'seed-data'),
  (26, 10, 1, 88.00, 'A', 9.00, 1, '2026-09-24', 'seed-data'),
  (27, 10, 2, 82.00, 'B+', 8.00, 3, '2026-09-23', 'seed-data'),
  (28, 10, 3, 90.00, 'A+', 10.00, 3, '2026-09-22', 'seed-data'),
  (29, 10, 4, 85.00, 'A', 9.00, 5, '2026-09-21', 'seed-data'),
  (30, 10, 6, 87.00, 'A', 9.00, 5, '2026-09-20', 'seed-data'),
  (31, 11, 1, 65.00, 'B', 7.00, 1, '2026-09-19', 'seed-data'),
  (32, 11, 2, 70.00, 'B', 7.00, 3, '2026-09-18', 'seed-data'),
  (33, 11, 3, 68.00, 'B', 7.00, 3, '2026-09-17', 'seed-data'),
  (34, 11, 4, 72.00, 'B', 7.00, 5, '2026-09-16', 'seed-data'),
  (35, 11, 5, 69.00, 'B', 7.00, 1, '2026-09-15', 'seed-data'),
  (36, 12, 1, 78.00, 'B+', 8.00, 1, '2026-09-14', 'seed-data'),
  (37, 12, 2, 85.00, 'A', 9.00, 3, '2026-09-13', 'seed-data'),
  (38, 12, 3, 80.00, 'B+', 8.00, 3, '2026-09-12', 'seed-data'),
  (39, 12, 4, 82.00, 'B+', 8.00, 5, '2026-09-11', 'seed-data'),
  (40, 12, 6, 76.00, 'B+', 8.00, 5, '2026-09-10', 'seed-data');

-- ---------------------------------------------------------------------
-- Quick verification
-- ---------------------------------------------------------------------
SELECT 'persons' AS table_name, COUNT(*) AS row_count FROM persons
UNION ALL SELECT 'students', COUNT(*) FROM students
UNION ALL SELECT 'instructors', COUNT(*) FROM instructors
UNION ALL SELECT 'courses', COUNT(*) FROM courses
UNION ALL SELECT 'enrollments', COUNT(*) FROM enrollments;
