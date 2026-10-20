package com.example.studentrecord.web;

import com.example.studentrecord.dto.EnrollmentForm;
import com.example.studentrecord.dto.EnrollmentView;
import com.example.studentrecord.dto.MarkForm;
import com.example.studentrecord.exception.DuplicateResourceException;
import com.example.studentrecord.service.CourseService;
import com.example.studentrecord.service.EnrollmentService;
import com.example.studentrecord.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** MVC screens for enrolling students and recording marks. */
@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;
    private final CourseService courseService;

    public EnrollmentController(EnrollmentService enrollmentService,
                                StudentService studentService,
                                CourseService courseService) {
        this.enrollmentService = enrollmentService;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("enrollments", enrollmentService.findAll());
        model.addAttribute("gradingServiceOnline", enrollmentService.isGradingServiceOnline());
        return "enrollments/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(value = "studentId", required = false) Long studentId,
                             @RequestParam(value = "courseId", required = false) Long courseId,
                             Model model) {
        EnrollmentForm form = new EnrollmentForm();
        form.setStudentId(studentId);
        form.setCourseId(courseId);
        model.addAttribute("enrollmentForm", form);
        addOptions(model);
        return "enrollments/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("enrollmentForm") EnrollmentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addOptions(model);
            return "enrollments/form";
        }
        try {
            EnrollmentView enrollment = enrollmentService.create(form);
            redirectAttributes.addFlashAttribute("success",
                    enrollment.getStudentName() + " was enrolled in " + enrollment.getCourseCode()
                            + " with grade " + enrollment.getGrade() + ".");
            return "redirect:/enrollments";
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("duplicate", ex.getMessage());
            addOptions(model);
            return "enrollments/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        EnrollmentView enrollment = enrollmentService.findView(id);
        MarkForm form = new MarkForm();
        form.setMarks(enrollment.getMarks());
        model.addAttribute("markForm", form);
        model.addAttribute("enrollment", enrollment);
        return "enrollments/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("markForm") MarkForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("enrollment", enrollmentService.findView(id));
            return "enrollments/edit";
        }
        EnrollmentView enrollment = enrollmentService.updateMarks(id, form.getMarks());
        redirectAttributes.addFlashAttribute("success",
                "Marks updated. New grade: " + enrollment.getGrade() + ".");
        return "redirect:/enrollments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        enrollmentService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Enrollment removed.");
        return "redirect:/enrollments";
    }

    private void addOptions(Model model) {
        model.addAttribute("students", studentService.findOptions());
        model.addAttribute("courses", courseService.findOptions());
        model.addAttribute("gradingServiceOnline", enrollmentService.isGradingServiceOnline());
    }
}
