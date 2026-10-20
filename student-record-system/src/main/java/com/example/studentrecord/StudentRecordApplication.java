package com.example.studentrecord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the main MVC + REST application.
 *
 * <p>Run this class from STS, or use:
 * {@code mvn -pl student-record-system spring-boot:run}. The application starts
 * on port 8080 and uses an H2 file database by default.</p>
 */
@SpringBootApplication
public class StudentRecordApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentRecordApplication.class, args);
    }
}
