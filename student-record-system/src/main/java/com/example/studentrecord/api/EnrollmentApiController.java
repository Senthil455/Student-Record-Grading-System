package com.example.studentrecord.api;

import com.example.studentrecord.dto.EnrollmentForm;
import com.example.studentrecord.dto.EnrollmentRequest;
import com.example.studentrecord.dto.EnrollmentView;
import com.example.studentrecord.dto.MarkRequest;
import com.example.studentrecord.service.EnrollmentService;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** REST API for enrollments and mark updates. */
@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollments", description = "Enroll students, record marks and obtain grades")
public class EnrollmentApiController {

    private final EnrollmentService enrollmentService;

    public EnrollmentApiController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    @Operation(summary = "List all enrollments")
    public List<EnrollmentView> findAll() {
        return enrollmentService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one enrollment")
    public EnrollmentView findById(@PathVariable Long id) {
        return enrollmentService.findView(id);
    }

    @PostMapping
    @Operation(summary = "Enroll a student and evaluate marks via the grading service")
    public ResponseEntity<EnrollmentView> create(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentForm form = new EnrollmentForm();
        form.setStudentId(request.studentId());
        form.setCourseId(request.courseId());
        form.setMarks(request.marks());

        EnrollmentView created = enrollmentService.create(form);
        return ResponseEntity.created(URI.create("/api/enrollments/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update marks and re-evaluate the grade")
    public EnrollmentView updateMarks(@PathVariable Long id, @Valid @RequestBody MarkRequest request) {
        return enrollmentService.updateMarks(id, request.marks());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an enrollment")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
