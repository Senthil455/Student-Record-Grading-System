package com.example.studentrecord.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end REST tests using MockMvc. The test profile disables the remote
 * grading service, so enrollment grading exercises the local fallback.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void performsTheFullStudentCourseEnrollmentWorkflow() throws Exception {
        long studentId = createStudent("24BCS101", "test.student@scholaris.edu");
        long courseId = createCourse("TEST101", "Testing Spring Boot", 4, 5);

        MvcResult enrollmentResult = mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentId": %d, "courseId": %d, "marks": 87.5}
                                """.formatted(studentId, courseId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/enrollments/")))
                .andExpect(jsonPath("$.grade").value("A"))
                .andExpect(jsonPath("$.gradePoint").value(9.0))
                .andExpect(jsonPath("$.gradedBy").value("local-fallback"))
                .andReturn();

        long enrollmentId = json(enrollmentResult).get("id").asLong();

        mockMvc.perform(put("/api/enrollments/{id}", enrollmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marks\": 92}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("A+"))
                .andExpect(jsonPath("$.gradePoint").value(10.0));

        mockMvc.perform(get("/api/students/{id}/transcript", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student.rollNumber").value("24BCS101"))
                .andExpect(jsonPath("$.enrollments[0].courseCode").value("TEST101"))
                .andExpect(jsonPath("$.enrollments[0].grade").value("A+"))
                .andExpect(jsonPath("$.gpa").value(10.0));

        mockMvc.perform(get("/api/reports/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students").isNumber())
                .andExpect(jsonPath("$.enrollments").isNumber());

        mockMvc.perform(delete("/api/enrollments/{id}", enrollmentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/enrollments/{id}", enrollmentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsValidationErrorsForInvalidStudentPayload() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "", "lastName": "", "email": "not-an-email",
                                 "rollNumber": "", "department": "", "admissionYear": 1990}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.admissionYear").exists());
    }

    @Test
    void rejectsDuplicateEmailAndRollNumber() throws Exception {
        createStudent("24BCS102", "duplicate.student@scholaris.edu");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("24BCS103", "duplicate.student@scholaris.edu")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("email")));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("24BCS102", "another.student@scholaris.edu")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Roll number")));
    }

    @Test
    void returnsNotFoundForUnknownStudent() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void exposesHibernateDiagnostics() throws Exception {
        mockMvc.perform(get("/api/diagnostics/session-factory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionFactoryOpen").value(true))
                .andExpect(jsonPath("$.entityCount").value(5));

        mockMvc.perform(get("/api/diagnostics/hibernate/top-performers").param("minAverage", "0"))
                .andExpect(status().isOk());
    }

    private long createStudent(String rollNumber, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson(rollNumber, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rollNumber").value(rollNumber))
                .andReturn();
        return json(result).get("id").asLong();
    }

    private long createCourse(String code, String title, int credits, int semester) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "%s", "title": "%s", "credits": %d, "semester": %d,
                                 "department": "B.Sc Computer Science", "description": "Test course"}
                                """.formatted(code, title, credits, semester)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(code))
                .andReturn();
        return json(result).get("id").asLong();
    }

    private String studentJson(String rollNumber, String email) {
        return """
                {"firstName": "Test", "lastName": "Student", "email": "%s",
                 "phone": "9876543210", "dateOfBirth": "2005-01-15",
                 "rollNumber": "%s", "department": "B.Sc Computer Science",
                 "admissionYear": 2024, "active": true}
                """.formatted(email, rollNumber);
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
