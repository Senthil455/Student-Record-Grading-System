package com.example.gradingservice.web;

import com.example.gradingservice.dto.GradeBandResponse;
import com.example.gradingservice.dto.GradeRequest;
import com.example.gradingservice.dto.GradeResponse;
import com.example.gradingservice.dto.GpaRequest;
import com.example.gradingservice.dto.GpaResponse;
import com.example.gradingservice.service.GradeCalculator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API of the grading microservice.
 *
 * <p>This service is stateless: it receives marks, applies the grading scale
 * and returns the result. The main application persists the returned values.</p>
 */
@RestController
@RequestMapping("/api/grade")
@Tag(name = "Grade evaluation", description = "Stateless grading rules exposed as a REST microservice")
public class GradeController {

    private final GradeCalculator gradeCalculator;

    public GradeController(GradeCalculator gradeCalculator) {
        this.gradeCalculator = gradeCalculator;
    }

    @GetMapping("/scale")
    @Operation(summary = "Get the institutional grading scale")
    public List<GradeBandResponse> scale() {
        return gradeCalculator.scale();
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Convert marks into a letter grade and grade point")
    public GradeResponse evaluate(@Valid @RequestBody GradeRequest request) {
        return gradeCalculator.evaluate(request.marks());
    }

    @PostMapping("/gpa")
    @Operation(summary = "Calculate a credit-weighted GPA")
    public GpaResponse gpa(@Valid @RequestBody GpaRequest request) {
        return gradeCalculator.calculateGpa(request.gradePoints(), request.credits());
    }

    @GetMapping("/health")
    @Operation(summary = "Lightweight health endpoint for the main application")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "grading-service");
        body.put("port", 8081);
        return ResponseEntity.ok(body);
    }

    @GetMapping
    @Operation(summary = "Service information")
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "grading-service");
        body.put("description", "Stateless REST service for the institutional grading scale");
        body.put("endpoints", List.of(
                "GET  /api/grade/scale",
                "POST /api/grade/evaluate",
                "POST /api/grade/gpa",
                "GET  /api/grade/health",
                "GET  /actuator/health"
        ));
        return body;
    }
}
