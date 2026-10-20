package com.example.studentrecord.api;

import com.example.studentrecord.dto.CourseDetail;
import com.example.studentrecord.dto.CourseForm;
import com.example.studentrecord.dto.CourseRequest;
import com.example.studentrecord.dto.CourseSummary;
import com.example.studentrecord.service.CourseService;
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

/** REST CRUD API for courses. */
@RestController
@RequestMapping("/api/courses")
@Tag(name = "Courses", description = "Create, read, update and delete courses")
public class CourseApiController {

    private final CourseService courseService;

    public CourseApiController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "List or search courses")
    public List<CourseSummary> findAll(@RequestParam(value = "q", required = false) String query) {
        return courseService.findAll(query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course with its roster")
    public CourseDetail findById(@PathVariable Long id) {
        return courseService.findDetail(id);
    }

    @PostMapping
    @Operation(summary = "Create a course")
    public ResponseEntity<CourseSummary> create(@Valid @RequestBody CourseRequest request) {
        CourseSummary created = courseService.create(toForm(request));
        return ResponseEntity.created(URI.create("/api/courses/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course")
    public CourseSummary update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, toForm(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course and its enrollments")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CourseForm toForm(CourseRequest request) {
        CourseForm form = new CourseForm();
        form.setCode(request.code());
        form.setTitle(request.title());
        form.setCredits(request.credits());
        form.setSemester(request.semester());
        form.setDepartment(request.department());
        form.setDescription(request.description());
        form.setInstructorId(request.instructorId());
        return form;
    }
}
