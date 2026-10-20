package com.example.studentrecord.web;

import com.example.studentrecord.dto.CourseDetail;
import com.example.studentrecord.dto.CourseForm;
import com.example.studentrecord.dto.CourseSummary;
import com.example.studentrecord.exception.DuplicateResourceException;
import com.example.studentrecord.service.CourseService;
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

import java.util.List;

/** MVC screens for course CRUD and course rosters. */
@Controller
@RequestMapping("/courses")
public class CourseController {

    private static final List<String> DEPARTMENTS = List.of(
            "B.Sc Computer Science", "BCA", "B.Sc Information Technology", "B.Com", "B.A English");

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String query, Model model) {
        List<CourseSummary> courses = courseService.findAll(query);
        model.addAttribute("courses", courses);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("resultCount", courses.size());
        return "courses/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("courseForm", new CourseForm());
        addOptions(model, false);
        return "courses/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("courseForm") CourseForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addOptions(model, false);
            return "courses/form";
        }
        try {
            CourseSummary course = courseService.create(form);
            redirectAttributes.addFlashAttribute("success",
                    "Course " + course.getCode() + " was created successfully.");
            return "redirect:/courses/" + course.getId();
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("duplicate", ex.getMessage());
            addOptions(model, false);
            return "courses/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CourseDetail detail = courseService.findDetail(id);
        model.addAttribute("detail", detail);
        model.addAttribute("course", detail.getCourse());
        return "courses/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CourseForm form = courseService.findForm(id);
        model.addAttribute("courseForm", form);
        model.addAttribute("courseId", id);
        addOptions(model, true);
        return "courses/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("courseForm") CourseForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (bindingResult.hasErrors()) {
            addOptions(model, true);
            model.addAttribute("courseId", id);
            return "courses/form";
        }
        try {
            CourseSummary course = courseService.update(id, form);
            redirectAttributes.addFlashAttribute("success", "Course updated successfully.");
            return "redirect:/courses/" + course.getId();
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("duplicate", ex.getMessage());
            addOptions(model, true);
            model.addAttribute("courseId", id);
            return "courses/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        courseService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Course and related enrollments were deleted.");
        return "redirect:/courses";
    }

    private void addOptions(Model model, boolean isEdit) {
        model.addAttribute("instructors", courseService.findInstructorOptions());
        model.addAttribute("departments", DEPARTMENTS);
        model.addAttribute("isEdit", isEdit);
    }
}
