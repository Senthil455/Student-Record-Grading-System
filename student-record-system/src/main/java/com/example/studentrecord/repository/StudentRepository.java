package com.example.studentrecord.repository;

import com.example.studentrecord.domain.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByRollNumberIgnoreCase(String rollNumber);

    boolean existsByRollNumberIgnoreCaseAndIdNot(String rollNumber, Long id);

    Optional<Student> findByRollNumberIgnoreCase(String rollNumber);

    List<Student> findAllByOrderByRollNumberAsc();

    long countByActiveTrue();

    @EntityGraph(attributePaths = {"enrollments", "enrollments.course"})
    @Query("select distinct s from Student s order by s.rollNumber")
    List<Student> findAllWithEnrollments();

    @EntityGraph(attributePaths = {"enrollments", "enrollments.course"})
    @Query("""
            select distinct s
            from Student s
            where lower(s.firstName) like lower(concat('%', :query, '%'))
               or lower(s.lastName) like lower(concat('%', :query, '%'))
               or lower(s.rollNumber) like lower(concat('%', :query, '%'))
               or lower(s.department) like lower(concat('%', :query, '%'))
            order by s.rollNumber
            """)
    List<Student> searchWithEnrollments(@Param("query") String query);

    @EntityGraph(attributePaths = {"enrollments", "enrollments.course"})
    Optional<Student> findDetailedById(Long id);

    /**
     * HQL query 1: top performers. The aggregate is filtered with HAVING and
     * ordered so the report can display the highest averages first.
     */
    @Query("""
            select s.id as id,
                   s.rollNumber as rollNumber,
                   s.firstName as firstName,
                   s.lastName as lastName,
                   s.department as department,
                   avg(e.marks) as averageMarks,
                   count(e.id) as courseCount
            from Student s join s.enrollments e
            where e.marks is not null
            group by s.id, s.rollNumber, s.firstName, s.lastName, s.department
            having avg(e.marks) >= :minimumAverage
            order by avg(e.marks) desc
            """)
    List<StudentAverageProjection> findTopPerformers(@Param("minimumAverage") double minimumAverage);

    /**
     * HQL query 2: students who have not enrolled in any course. A subquery is
     * used instead of a LEFT JOIN so the intent is explicit.
     */
    @Query("""
            select s
            from Student s
            where s.id not in (select e.student.id from Enrollment e)
            order by s.rollNumber
            """)
    List<Student> findStudentsWithoutEnrollments();

    /** HQL query 3: student count grouped by department. */
    @Query("""
            select s.department as department, count(s.id) as studentCount
            from Student s
            group by s.department
            order by count(s.id) desc, s.department asc
            """)
    List<DepartmentCountProjection> countByDepartment();

    interface StudentAverageProjection {
        Long getId();

        String getRollNumber();

        String getFirstName();

        String getLastName();

        String getDepartment();

        Double getAverageMarks();

        Long getCourseCount();
    }

    interface DepartmentCountProjection {
        String getDepartment();

        Long getStudentCount();
    }
}
