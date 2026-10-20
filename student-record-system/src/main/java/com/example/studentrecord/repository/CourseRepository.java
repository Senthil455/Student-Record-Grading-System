package com.example.studentrecord.repository;

import com.example.studentrecord.domain.Course;
import com.example.studentrecord.domain.Instructor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<Course> findByCodeIgnoreCase(String code);

    List<Course> findAllByOrderByCodeAsc();

    long countByInstructor(Instructor instructor);

    @EntityGraph(attributePaths = {"instructor", "enrollments"})
    @Query("select distinct c from Course c order by c.code")
    List<Course> findAllWithDetails();

    @EntityGraph(attributePaths = {"instructor", "enrollments"})
    @Query("""
            select distinct c
            from Course c
            where lower(c.code) like lower(concat('%', :query, '%'))
               or lower(c.title) like lower(concat('%', :query, '%'))
               or lower(c.department) like lower(concat('%', :query, '%'))
            order by c.code
            """)
    List<Course> searchWithDetails(@Param("query") String query);

    @EntityGraph(attributePaths = {"instructor", "enrollments", "enrollments.student"})
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"instructor"})
    @Query("select c from Course c order by c.code")
    List<Course> findAllWithInstructor();
}
