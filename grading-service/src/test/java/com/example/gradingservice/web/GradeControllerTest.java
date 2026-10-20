package com.example.gradingservice.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void evaluatesMarksThroughRestApi() throws Exception {
        mockMvc.perform(post("/api/grade/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marks\": 87.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("A"))
                .andExpect(jsonPath("$.gradePoint").value(9.0))
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.evaluatedBy").value("grading-service"));
    }

    @Test
    void returnsBadRequestForInvalidMarks() throws Exception {
        mockMvc.perform(post("/api/grade/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marks\": 120}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.marks").exists());
    }

    @Test
    void returnsTheGradingScale() throws Exception {
        mockMvc.perform(get("/api/grade/scale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grade").value("A+"))
                .andExpect(jsonPath("$[6].grade").value("F"));
    }

    @Test
    void calculatesGpaThroughRestApi() throws Exception {
        mockMvc.perform(post("/api/grade/gpa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"gradePoints": [9.0, 8.0, 10.0], "credits": [3, 4, 3]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gpa").value(8.9))
                .andExpect(jsonPath("$.totalCredits").value(10));
    }

    @Test
    void exposesHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/grade/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
