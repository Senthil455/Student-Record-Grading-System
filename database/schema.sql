-- =====================================================================
-- Scholaris: Student Record and Grading System
-- MySQL 8.x schema
--
-- The application creates/updates this schema automatically through
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) when the "mysql"
-- profile is active. This script is provided for documentation, manual
-- database setup and the DB-schema section of the report.
--
-- Usage:
--   mysql -u root -p < database/schema.sql
--   mysql -u root -p student_grading < database/sample-data.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS student_grading
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_grading;

-- ---------------------------------------------------------------------
-- persons: shared columns for the JOINED inheritance hierarchy
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS persons (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    first_name    VARCHAR(60)  NOT NULL,
    last_name     VARCHAR(60)  NOT NULL,
    email         VARCHAR(120) NOT NULL,
    phone         VARCHAR(20)  NULL,
    date_of_birth DATE         NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_person_email UNIQUE (email)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- students: subclass table, primary key is also a foreign key to persons
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS students (
    person_id      BIGINT      NOT NULL,
    roll_number    VARCHAR(20) NOT NULL,
    department     VARCHAR(80) NOT NULL,
    admission_year INT         NOT NULL,
    active         BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (person_id),
    CONSTRAINT uk_student_roll UNIQUE (roll_number),
    CONSTRAINT fk_student_person
        FOREIGN KEY (person_id) REFERENCES persons (id)
        ON DELETE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- instructors: second subclass table of the JOINED hierarchy
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS instructors (
    person_id       BIGINT      NOT NULL,
    employee_code   VARCHAR(20) NOT NULL,
    designation     VARCHAR(80) NULL,
    specialization  VARCHAR(120) NULL,
    PRIMARY KEY (person_id),
    CONSTRAINT uk_instructor_code UNIQUE (employee_code),
    CONSTRAINT fk_instructor_person
        FOREIGN KEY (person_id) REFERENCES persons (id)
        ON DELETE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- courses
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courses (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    course_code   VARCHAR(15)  NOT NULL,
    title         VARCHAR(120) NOT NULL,
    credits       INT          NOT NULL,
    semester      INT          NOT NULL,
    department    VARCHAR(80)  NULL,
    description   VARCHAR(500) NULL,
    instructor_id BIGINT       NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_course_code UNIQUE (course_code),
    CONSTRAINT fk_course_instructor
        FOREIGN KEY (instructor_id) REFERENCES instructors (person_id)
        ON DELETE SET NULL
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- enrollments: join entity with marks / grade information
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS enrollments (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    student_id  BIGINT      NOT NULL,
    course_id   BIGINT      NOT NULL,
    marks       DOUBLE      NULL,
    grade       VARCHAR(3)  NULL,
    grade_point DOUBLE      NULL,
    semester    INT         NOT NULL,
    enrolled_on DATE        NOT NULL,
    graded_by   VARCHAR(30) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_enrollment_student_course UNIQUE (student_id, course_id),
    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id) REFERENCES students (person_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id) REFERENCES courses (id)
        ON DELETE CASCADE
) ENGINE = InnoDB;

-- Helpful indexes for the report queries and list pages.
CREATE INDEX idx_student_department ON students (department);
CREATE INDEX idx_enrollment_course ON enrollments (course_id);
CREATE INDEX idx_enrollment_grade ON enrollments (grade);
CREATE INDEX idx_course_instructor ON courses (instructor_id);
