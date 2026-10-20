package com.example.gradingservice.service;

import com.example.gradingservice.dto.GradeResponse;
import com.example.gradingservice.dto.GpaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GradeCalculatorTest {

    private final GradeCalculator calculator = new GradeCalculator();

    @ParameterizedTest(name = "{0} marks -> {1} ({2} grade points)")
    @CsvSource({
            "100.0, A+, 10.0",
            "90.0,  A+, 10.0",
            "89.9,  A,  9.0",
            "85.0,  A,  9.0",
            "84.9,  B+, 8.0",
            "75.0,  B+, 8.0",
            "74.9,  B,  7.0",
            "65.0,  B,  7.0",
            "64.9,  C,  6.0",
            "55.0,  C,  6.0",
            "54.9,  D,  5.0",
            "45.0,  D,  5.0",
            "44.9,  F,  0.0",
            "0.0,   F,  0.0"
    })
    void appliesGradeBoundaries(double marks, String grade, double gradePoint) {
        GradeResponse response = calculator.evaluate(marks);

        assertThat(response.grade()).isEqualTo(grade);
        assertThat(response.gradePoint()).isEqualTo(gradePoint);
        assertThat(response.passed()).isEqualTo(!"F".equals(grade));
        assertThat(response.evaluatedBy()).isEqualTo("grading-service");
    }

    @Test
    void rejectsMarksOutsideTheValidRange() {
        assertThatThrownBy(() -> calculator.evaluate(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 100");
        assertThatThrownBy(() -> calculator.evaluate(101))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void computesCreditWeightedGpa() {
        // (9*3 + 8*4 + 10*3) / 10 = 8.9
        GpaResponse response = calculator.calculateGpa(
                List.of(9.0, 8.0, 10.0),
                List.of(3, 4, 3));

        assertThat(response.gpa()).isEqualTo(8.9);
        assertThat(response.totalCredits()).isEqualTo(10);
        assertThat(response.courses()).isEqualTo(3);
    }

    @Test
    void rejectsMismatchedGpaInputs() {
        assertThatThrownBy(() -> calculator.calculateGpa(List.of(9.0), List.of(3, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("matching credit");
    }

    @Test
    void exposesSevenOrderedGradeBands() {
        assertThat(calculator.scale())
                .hasSize(7)
                .extracting(band -> band.grade())
                .containsExactly("A+", "A", "B+", "B", "C", "D", "F");
    }
}
