package com.example.studentrecord.repository;

import com.example.studentrecord.domain.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByGradeIsNotNull();

    @EntityGraph(attributePaths = {"student", "course"})
    @Query("select e from Enrollment e order by e.enrolledOn desc, e.id desc")
    List<Enrollment> findAllDetailed();

    @EntityGraph(attributePaths = {"student", "course"})
    @Query("select e from Enrollment e where e.student.id = :studentId order by e.course.code")
    List<Enrollment> findByStudentIdDetailed(@Param("studentId") Long studentId);

    @EntityGraph(attributePaths = {"student", "course"})
    @Query("select e from Enrollment e where e.course.id = :courseId order by e.student.rollNumber")
    List<Enrollment> findByCourseIdDetailed(@Param("courseId") Long courseId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<Enrollment> findTop5ByOrderByIdDesc();

    @Query("select avg(e.marks) from Enrollment e where e.marks is not null")
    Double findOverallAverage();

    /** HQL query 4: aggregate performance per course, used by the reports page. */
    @Query("""
            select e.course.code as code,
                   e.course.title as title,
                   avg(e.marks) as averageMarks,
                   count(e.id) as enrollmentCount,
                   max(e.marks) as highestMarks,
                   min(e.marks) as lowestMarks
            from Enrollment e
            where e.marks is not null
            group by e.course.id, e.course.code, e.course.title
            order by avg(e.marks) desc
            """)
    List<CourseStatProjection> findCourseStatistics();

    @Query("""
            select e.grade as grade, count(e.id) as gradeCount
            from Enrollment e
            where e.grade is not null
            group by e.grade
            order by e.grade
            """)
    List<GradeCountProjection> countByGrade();

    interface CourseStatProjection {
        String getCode();

        String getTitle();

        Double getAverageMarks();

        Long getEnrollmentCount();

        Double getHighestMarks();

        Double getLowestMarks();
    }

    interface GradeCountProjection {
        String getGrade();

        Long getGradeCount();
    }
}
