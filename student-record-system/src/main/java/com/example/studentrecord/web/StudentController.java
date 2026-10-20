package com.example.studentrecord.web;

import com.example.studentrecord.dto.StudentDetail;
import com.example.studentrecord.dto.StudentForm;
import com.example.studentrecord.dto.StudentSummary;
import com.example.studentrecord.exception.DuplicateResourceException;
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

import java.time.Year;
import java.util.List;

/** MVC screens for student CRUD. */
@Controller
@RequestMapping("/students")
public class StudentController {

    private static final List<String> DEPARTMENTS = List.of(
            "B.Sc Computer Science", "BCA", "B.Sc Information Technology", "B.Com", "B.A English");

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String query, Model model) {
        List<StudentSummary> students = studentService.findAll(query);
        model.addAttribute("students", students);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("resultCount", students.size());
        return "students/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        StudentForm form = new StudentForm();
        form.setAdmissionYear(Year.now().getValue());
        model.addAttribute("studentForm", form);
        model.addAttribute("departments", DEPARTMENTS);
        model.addAttribute("isEdit", false);
        return "students/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("studentForm") StudentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormOptions(model, false);
            return "students/form";
        }
        try {
            StudentSummary student = studentService.create(form);
            redirectAttributes.addFlashAttribute("success",
                    "Student " + student.getFullName() + " was added successfully.");
            return "redirect:/students/" + student.getId();
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("duplicate", ex.getMessage());
            addFormOptions(model, false);
            return "students/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        StudentDetail detail = studentService.findDetail(id);
        model.addAttribute("detail", detail);
        model.addAttribute("student", detail.getStudent());
        return "students/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        StudentForm form = studentService.findForm(id);
        model.addAttribute("studentForm", form);
        model.addAttribute("studentId", id);
        model.addAttribute("departments", DEPARTMENTS);
        model.addAttribute("isEdit", true);
        return "students/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("studentForm") StudentForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (bindingResult.hasErrors()) {
            addFormOptions(model, true);
            model.addAttribute("studentId", id);
            return "students/form";
        }
        try {
            StudentSummary student = studentService.update(id, form);
            redirectAttributes.addFlashAttribute("success", "Student record updated successfully.");
            return "redirect:/students/" + student.getId();
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("duplicate", ex.getMessage());
            addFormOptions(model, true);
            model.addAttribute("studentId", id);
            return "students/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        studentService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Student and related enrollments were deleted.");
        return "redirect:/students";
    }

    private void addFormOptions(Model model, boolean isEdit) {
        model.addAttribute("departments", DEPARTMENTS);
        model.addAttribute("isEdit", isEdit);
    }
}
