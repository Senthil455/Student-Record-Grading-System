package com.example.studentrecord.web;

import com.example.studentrecord.dto.CourseStat;
import com.example.studentrecord.dto.TopPerformer;
import com.example.studentrecord.service.CourseService;
import com.example.studentrecord.service.EnrollmentService;
import com.example.studentrecord.service.ReportService;
import com.example.studentrecord.service.StudentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/** Dashboard / home page. */
@Controller
public class HomeController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ReportService reportService;
    private final String gradingServiceUrl;

    public HomeController(StudentService studentService,
                          CourseService courseService,
                          EnrollmentService enrollmentService,
                          ReportService reportService,
                          @Value("${grading.service.base-url:http://localhost:8081}") String gradingServiceUrl) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.reportService = reportService;
        this.gradingServiceUrl = gradingServiceUrl;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<TopPerformer> topPerformers = reportService.topPerformers(75);
        List<CourseStat> courseStats = reportService.courseStatistics();

        model.addAttribute("studentCount", studentService.count());
        model.addAttribute("activeStudentCount", studentService.activeCount());
        model.addAttribute("courseCount", courseService.count());
        model.addAttribute("enrollmentCount", enrollmentService.count());
        model.addAttribute("gradedCount", enrollmentService.gradedCount());
        model.addAttribute("averageMarks", enrollmentService.overallAverage());
        model.addAttribute("topPerformers", topPerformers.size() > 5 ? topPerformers.subList(0, 5) : topPerformers);
        model.addAttribute("courseStats", courseStats.size() > 5 ? courseStats.subList(0, 5) : courseStats);
        model.addAttribute("recentEnrollments", enrollmentService.findRecent());
        model.addAttribute("departmentStats", reportService.departmentStatistics());
        model.addAttribute("gradingServiceOnline", enrollmentService.isGradingServiceOnline());
        model.addAttribute("gradingServiceUrl", gradingServiceUrl);
        return "dashboard";
    }
}
