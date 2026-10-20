package com.example.studentrecord.config;

import com.example.studentrecord.domain.Course;
import com.example.studentrecord.domain.Enrollment;
import com.example.studentrecord.domain.Instructor;
import com.example.studentrecord.domain.Student;
import com.example.studentrecord.grading.GradeResult;
import com.example.studentrecord.grading.LocalGradeCalculator;
import com.example.studentrecord.repository.CourseRepository;
import com.example.studentrecord.repository.EnrollmentRepository;
import com.example.studentrecord.repository.InstructorRepository;
import com.example.studentrecord.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Seeds a realistic demonstration data set the first time the application runs.
 * If the database already contains students, seeding is skipped.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LocalGradeCalculator gradeCalculator;

    private int enrollmentOffset = 1;

    public DataSeeder(StudentRepository studentRepository,
                      InstructorRepository instructorRepository,
                      CourseRepository courseRepository,
                      EnrollmentRepository enrollmentRepository,
                      LocalGradeCalculator gradeCalculator) {
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.gradeCalculator = gradeCalculator;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (studentRepository.count() > 0) {
            log.info("Demo data already present; seeding skipped.");
            return;
        }

        Instructor anitha = instructor("Anitha", "Raman", "anitha.raman@scholaris.edu",
                "9840012345", "EMP1001", "Professor", "Data Structures & Algorithms");
        Instructor karthik = instructor("Karthik", "Subramanian", "karthik.subramanian@scholaris.edu",
                "9840012346", "EMP1002", "Associate Professor", "Web Technologies");
        Instructor meera = instructor("Meera", "Nair", "meera.nair@scholaris.edu",
                "9840012347", "EMP1003", "Assistant Professor", "Database Systems");
        Instructor rahul = instructor("Rahul", "Verma", "rahul.verma@scholaris.edu",
                "9840012348", "EMP1004", "Assistant Professor", "Software Engineering");

        Course java = course("CS101", "Programming in Java", 4, 1, "B.Sc Computer Science",
                "Object-oriented programming, collections, exceptions and file handling.", karthik);
        Course dataStructures = course("CS102", "Data Structures", 4, 3, "B.Sc Computer Science",
                "Lists, stacks, queues, trees, graphs and algorithm analysis.", anitha);
        Course dbms = course("CS201", "Database Management Systems", 3, 3, "BCA",
                "Relational model, SQL, normalisation and transaction management.", meera);
        Course web = course("CS202", "Web Programming", 3, 5, "B.Sc Computer Science",
                "HTML, CSS, JavaScript, HTTP and server-side web development.", karthik);
        Course maths = course("MA101", "Discrete Mathematics", 3, 1, "BCA",
                "Sets, relations, functions, graph theory and combinatorics.", rahul);
        Course softwareEngineering = course("CS301", "Software Engineering", 3, 5, "B.Sc Computer Science",
                "Requirements, design, testing, agile delivery and software quality.", rahul);

        Student aarav = student("Aarav", "Sharma", "aarav.sharma@student.scholaris.edu",
                "23BCS001", "B.Sc Computer Science", 2023, LocalDate.of(2005, 4, 12));
        Student diya = student("Diya", "Patel", "diya.patel@student.scholaris.edu",
                "23BCS002", "B.Sc Computer Science", 2023, LocalDate.of(2005, 8, 3));
        Student ishaan = student("Ishaan", "Reddy", "ishaan.reddy@student.scholaris.edu",
                "23BCS003", "B.Sc Computer Science", 2023, LocalDate.of(2004, 12, 21));
        Student ananya = student("Ananya", "Iyer", "ananya.iyer@student.scholaris.edu",
                "23BCS004", "B.Sc Computer Science", 2023, LocalDate.of(2005, 2, 17));
        Student vihaan = student("Vihaan", "Gupta", "vihaan.gupta@student.scholaris.edu",
                "23BCS005", "B.Sc Computer Science", 2023, LocalDate.of(2004, 10, 9));
        Student kavya = student("Kavya", "Menon", "kavya.menon@student.scholaris.edu",
                "23BCS006", "BCA", 2023, LocalDate.of(2005, 6, 28));
        Student arjun = student("Arjun", "Nair", "arjun.nair@student.scholaris.edu",
                "23BCS007", "BCA", 2023, LocalDate.of(2005, 1, 15));
        Student sneha = student("Sneha", "Kulkarni", "sneha.kulkarni@student.scholaris.edu",
                "23BCS008", "BCA", 2023, LocalDate.of(2005, 9, 7));
        student("Rohan", "Das", "rohan.das@student.scholaris.edu",
                "23BCS009", "B.Sc Computer Science", 2024, LocalDate.of(2006, 3, 30));

        enroll(aarav, java, 92);
        enroll(aarav, dataStructures, 88);
        enroll(aarav, dbms, 85);
        enroll(aarav, web, 91);
        enroll(aarav, softwareEngineering, 89);

        enroll(diya, java, 84);
        enroll(diya, dataStructures, 79);
        enroll(diya, dbms, 88);
        enroll(diya, web, 86);
        enroll(diya, maths, 78);

        enroll(ishaan, java, 72);
        enroll(ishaan, dataStructures, 68);
        enroll(ishaan, dbms, 75);
        enroll(ishaan, web, 70);
        enroll(ishaan, maths, 65);

        enroll(ananya, java, 95);
        enroll(ananya, dataStructures, 91);
        enroll(ananya, dbms, 93);
        enroll(ananya, web, 96);
        enroll(ananya, softwareEngineering, 94);

        enroll(vihaan, java, 58);
        enroll(vihaan, dataStructures, 62);
        enroll(vihaan, dbms, 55);
        enroll(vihaan, web, 60);
        enroll(vihaan, maths, 57);

        enroll(kavya, java, 88);
        enroll(kavya, dataStructures, 82);
        enroll(kavya, dbms, 90);
        enroll(kavya, web, 85);
        enroll(kavya, softwareEngineering, 87);

        enroll(arjun, java, 65);
        enroll(arjun, dataStructures, 70);
        enroll(arjun, dbms, 68);
        enroll(arjun, web, 72);
        enroll(arjun, maths, 69);

        enroll(sneha, java, 78);
        enroll(sneha, dataStructures, 85);
        enroll(sneha, dbms, 80);
        enroll(sneha, web, 82);
        enroll(sneha, softwareEngineering, 76);

        log.info("Demo data seeded: {} students, {} courses, {} enrollments.",
                studentRepository.count(), courseRepository.count(), enrollmentRepository.count());
    }

    private Instructor instructor(String firstName, String lastName, String email, String phone,
                                  String employeeCode, String designation, String specialization) {
        Instructor instructor = new Instructor();
        instructor.setFirstName(firstName);
        instructor.setLastName(lastName);
        instructor.setEmail(email);
        instructor.setPhone(phone);
        instructor.setEmployeeCode(employeeCode);
        instructor.setDesignation(designation);
        instructor.setSpecialization(specialization);
        return instructorRepository.save(instructor);
    }

    private Course course(String code, String title, int credits, int semester, String department,
                          String description, Instructor instructor) {
        Course course = new Course();
        course.setCode(code);
        course.setTitle(title);
        course.setCredits(credits);
        course.setSemester(semester);
        course.setDepartment(department);
        course.setDescription(description);
        course.setInstructor(instructor);
        return courseRepository.save(course);
    }

    private Student student(String firstName, String lastName, String email, String rollNumber,
                            String department, int admissionYear, LocalDate dateOfBirth) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setRollNumber(rollNumber);
        student.setDepartment(department);
        student.setAdmissionYear(admissionYear);
        student.setDateOfBirth(dateOfBirth);
        student.setActive(true);
        return studentRepository.save(student);
    }

    private void enroll(Student student, Course course, double marks) {
        GradeResult grade = gradeCalculator.evaluate(marks);
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setMarks(marks);
        enrollment.setGrade(grade.grade());
        enrollment.setGradePoint(grade.gradePoint());
        enrollment.setGradedBy("seed-data");
        enrollment.setSemester(course.getSemester());
        enrollment.setEnrolledOn(LocalDate.now().minusDays(enrollmentOffset++));
        enrollmentRepository.save(enrollment);
    }
}
