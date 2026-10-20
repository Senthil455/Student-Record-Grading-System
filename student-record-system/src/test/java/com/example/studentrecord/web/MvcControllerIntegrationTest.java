package com.example.studentrecord.web;

import com.example.studentrecord.dto.StudentForm;
import com.example.studentrecord.dto.StudentSummary;
import com.example.studentrecord.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Verifies that Thymeleaf pages render and HTML form validation works. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MvcControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Test
    void dashboardRenders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("Good records make good decisions.")))
                .andExpect(content().string(containsString("Scholaris")));
    }

    @Test
    void studentListAndProfileRenderCreatedRecord() throws Exception {
        StudentSummary student = studentService.create(sampleForm("24BCS201", "mvc.student@scholaris.edu"));

        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(view().name("students/list"))
                .andExpect(content().string(containsString("MVC Student")))
                .andExpect(content().string(containsString("24BCS201")));

        mockMvc.perform(get("/students/{id}", student.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("students/detail"))
                .andExpect(content().string(containsString("Academic transcript")));
    }

    @Test
    void showsValidationMessagesForEmptyStudentForm() throws Exception {
        mockMvc.perform(post("/students")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", "bad")
                        .param("rollNumber", "")
                        .param("department", "")
                        .param("admissionYear", "1990"))
                .andExpect(status().isOk())
                .andExpect(view().name("students/form"))
                .andExpect(content().string(containsString("First name is required.")))
                .andExpect(content().string(containsString("Enter a valid email address.")));
    }

    @Test
    void returns404PageForMissingStudent() throws Exception {
        mockMvc.perform(get("/students/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("We could not find that record.")));
    }

    private StudentForm sampleForm(String rollNumber, String email) {
        StudentForm form = new StudentForm();
        form.setFirstName("MVC");
        form.setLastName("Student");
        form.setEmail(email);
        form.setPhone("9876543210");
        form.setDateOfBirth(LocalDate.of(2005, 5, 10));
        form.setRollNumber(rollNumber);
        form.setDepartment("B.Sc Computer Science");
        form.setAdmissionYear(2024);
        form.setActive(true);
        return form;
    }
}
