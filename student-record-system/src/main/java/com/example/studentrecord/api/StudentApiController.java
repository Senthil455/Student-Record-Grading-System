package com.example.studentrecord.api;

import com.example.studentrecord.dto.StudentDetail;
import com.example.studentrecord.dto.StudentForm;
import com.example.studentrecord.dto.StudentRequest;
import com.example.studentrecord.dto.StudentSummary;
import com.example.studentrecord.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** REST CRUD API for students. */
@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Create, read, update and delete student records")
public class StudentApiController {

    private final StudentService studentService;

    public StudentApiController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "List or search students")
    public List<StudentSummary> findAll(@RequestParam(value = "q", required = false) String query) {
        return studentService.findAll(query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one student with all enrollments")
    public StudentDetail findById(@PathVariable Long id) {
        return studentService.findDetail(id);
    }

    @GetMapping("/{id}/transcript")
    @Operation(summary = "Get a student's transcript and GPA")
    public StudentDetail transcript(@PathVariable Long id) {
        return studentService.findDetail(id);
    }

    @PostMapping
    @Operation(summary = "Create a student")
    public ResponseEntity<StudentSummary> create(@Valid @RequestBody StudentRequest request) {
        StudentSummary created = studentService.create(toForm(request));
        return ResponseEntity.created(URI.create("/api/students/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a student")
    public StudentSummary update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return studentService.update(id, toForm(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a student and their enrollments")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private StudentForm toForm(StudentRequest request) {
        StudentForm form = new StudentForm();
        form.setFirstName(request.firstName());
        form.setLastName(request.lastName());
        form.setEmail(request.email());
        form.setPhone(request.phone());
        form.setDateOfBirth(request.dateOfBirth());
        form.setRollNumber(request.rollNumber());
        form.setDepartment(request.department());
        form.setAdmissionYear(request.admissionYear());
        form.setActive(request.active() == null || request.active());
        return form;
    }
}
