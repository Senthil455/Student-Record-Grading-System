package com.example.studentrecord.grading;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalGradeCalculatorTest {

    private final LocalGradeCalculator calculator = new LocalGradeCalculator();

    @ParameterizedTest(name = "{0} marks -> {1}")
    @CsvSource({
            "100.0, A+", "90.0, A+", "89.99, A", "85.0, A",
            "84.99, B+", "75.0, B+", "74.99, B", "65.0, B",
            "64.99, C", "55.0, C", "54.99, D", "45.0, D",
            "44.99, F", "0.0, F"
    })
    void appliesTheSameBoundariesAsTheGradingService(double marks, String expectedGrade) {
        GradeResult result = calculator.evaluate(marks);

        assertThat(result.grade()).isEqualTo(expectedGrade);
        assertThat(result.source()).isEqualTo("local-fallback");
        assertThat(result.gradePoint()).isBetween(0.0, 10.0);
    }

    @Test
    void rejectsInvalidMarks() {
        assertThatThrownBy(() -> calculator.evaluate(101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 100");
    }
}
