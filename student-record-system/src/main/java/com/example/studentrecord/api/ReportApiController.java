package com.example.studentrecord.api;

import com.example.studentrecord.dto.CourseStat;
import com.example.studentrecord.dto.DepartmentStat;
import com.example.studentrecord.dto.GradeCount;
import com.example.studentrecord.dto.TopPerformer;
import com.example.studentrecord.service.CourseService;
import com.example.studentrecord.service.EnrollmentService;
import com.example.studentrecord.service.ReportService;
import com.example.studentrecord.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only report endpoints backed by HQL aggregate queries. */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Aggregate academic reports and dashboard totals")
public class ReportApiController {

    private final ReportService reportService;
    private final StudentService studentService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public ReportApiController(ReportService reportService,
                               StudentService studentService,
                               CourseService courseService,
                               EnrollmentService enrollmentService) {
        this.reportService = reportService;
        this.studentService = studentService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/top-performers")
    @Operation(summary = "Students whose average marks are at or above a threshold (HQL)")
    public List<TopPerformer> topPerformers(@RequestParam(defaultValue = "75") double minAverage) {
        return reportService.topPerformers(minAverage);
    }

    @GetMapping("/course-performance")
    @Operation(summary = "Average, highest and lowest marks per course (HQL)")
    public List<CourseStat> coursePerformance() {
        return reportService.courseStatistics();
    }

    @GetMapping("/department-distribution")
    @Operation(summary = "Student count by department (HQL)")
    public List<DepartmentStat> departmentDistribution() {
        return reportService.departmentStatistics();
    }

    @GetMapping("/grade-distribution")
    @Operation(summary = "Number of enrollments per grade (HQL)")
    public List<GradeCount> gradeDistribution() {
        return reportService.gradeDistribution();
    }

    @GetMapping("/summary")
    @Operation(summary = "Dashboard totals")
    public Map<String, Object> summary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("students", studentService.count());
        summary.put("activeStudents", studentService.activeCount());
        summary.put("courses", courseService.count());
        summary.put("enrollments", enrollmentService.count());
        summary.put("gradedEnrollments", enrollmentService.gradedCount());
        summary.put("overallAverage", reportService.overallAverage());
        return summary;
    }
}
