package com.example.studentrecord.repository;

import com.example.studentrecord.domain.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    List<Instructor> findAllByOrderByFirstNameAscLastNameAsc();

    Optional<Instructor> findByEmployeeCodeIgnoreCase(String employeeCode);
}
