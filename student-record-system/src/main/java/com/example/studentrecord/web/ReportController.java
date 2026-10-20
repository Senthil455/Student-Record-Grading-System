package com.example.studentrecord.web;

import com.example.studentrecord.service.HibernateDemoService;
import com.example.studentrecord.service.ReportService;
import com.example.studentrecord.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Reporting page that highlights the HQL and Hibernate Session demonstrations. */
@Controller
public class ReportController {

    private final ReportService reportService;
    private final StudentService studentService;
    private final HibernateDemoService hibernateDemoService;

    public ReportController(ReportService reportService,
                            StudentService studentService,
                            HibernateDemoService hibernateDemoService) {
        this.reportService = reportService;
        this.studentService = studentService;
        this.hibernateDemoService = hibernateDemoService;
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(value = "minAverage", defaultValue = "75") double minAverage,
                          Model model) {
        model.addAttribute("minAverage", minAverage);
        model.addAttribute("topPerformers", reportService.topPerformers(minAverage));
        model.addAttribute("courseStats", reportService.courseStatistics());
        model.addAttribute("departmentStats", reportService.departmentStatistics());
        model.addAttribute("gradeDistribution", reportService.gradeDistribution());
        model.addAttribute("studentsWithoutEnrollments", studentService.findWithoutEnrollments());
        model.addAttribute("overallAverage", reportService.overallAverage());
        model.addAttribute("sessionFactoryInfo", hibernateDemoService.sessionFactoryInfo());
        model.addAttribute("hibernateTopPerformers", hibernateDemoService.topPerformersViaHibernateSession(80));
        return "reports";
    }
}
