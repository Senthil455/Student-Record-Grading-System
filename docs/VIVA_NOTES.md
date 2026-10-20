# Viva notes — Scholaris

Short, code-specific answers for the demonstration and viva. Each answer points to the file where the concept is implemented.

---

## A. Spring Boot and MVC

**1. What is Spring Boot?**
Spring Boot is an opinionated layer on top of the Spring Framework. It provides starters, auto-configuration, an embedded web server and production endpoints, so a standalone application can start with a single `main` method. This project uses `spring-boot-starter-web`, `data-jpa`, `thymeleaf`, `validation`, `aop` and `actuator`, all managed by the parent POM (`spring-boot-starter-parent` 3.5.16).

**2. How does the application start?**
`StudentRecordApplication` is annotated with `@SpringBootApplication` (which combines `@Configuration`, `@EnableAutoConfiguration` and `@ComponentScan`) and calls `SpringApplication.run(...)`. It scans `com.example.studentrecord`, creates the `ApplicationContext`, starts Tomcat on port 8080, initialises the H2 datasource and runs `DataSeeder`.

**3. Explain the MVC flow.**
Browser → `DispatcherServlet` → handler mapping finds a `@GetMapping` method in a `@Controller` → controller calls a service → service calls a repository → Thymeleaf resolves the returned view name (for example `students/list`) against `templates/` → HTML is returned. REST controllers skip view resolution and serialize objects to JSON through Jackson.

**4. Why are there both `web/` and `api/` controllers?**
`web/` controllers return Thymeleaf view names and use `RedirectAttributes` for flash messages. `api/` controllers are annotated `@RestController` and return JSON with proper status codes. Separating them keeps HTML concerns out of the REST contract.

**5. What is `@ControllerAdvice` used for here?**
`WebExceptionHandler` maps `ResourceNotFoundException`, validation errors and generic exceptions to the custom 404/500 Thymeleaf pages. `ApiExceptionHandler` (restricted to `com.example.studentrecord.api`) maps exceptions to the JSON error contract. `GlobalModelAttributes` adds `academicYear`, `todayLabel` and `currentYear` to every MVC model.

**6. How is form data bound?**
Thymeleaf uses `th:object="${studentForm}"` and `th:field="*{firstName}"`. Spring binds request parameters to the `StudentForm` bean by name and records validation errors in a `BindingResult`. The controller checks `result.hasErrors()` and returns the same form view with errors.

**7. Why use DTOs instead of exposing entities?**
DTOs (`StudentForm`, `StudentRequest`, `StudentSummary`, `StudentDetail`, `EnrollmentView`) prevent lazy-loading and serialization problems, shape the JSON contract, and avoid accepting fields such as `id` or `createdAt` from clients. They also let the service compute GPA without persisting it.

**8. What does `@Valid` do?**
It triggers Bean Validation on a request body or form. Constraints such as `@NotBlank`, `@Email`, `@Min`, `@Max` and `@Past` are declared on the DTO. Failures raise `MethodArgumentNotValidException`, handled by the advice classes.

**9. How does the app serve static files?**
`src/main/resources/static/css/style.css`, `js/app.js` and `favicon.svg` are served by Spring Boot's default static resource handling. Thymeleaf links use `@{/css/style.css}` so the context path is applied.

**10. What is `application.properties` used for?**
It configures the port, datasource, JPA behaviour, Thymeleaf, grading service URL, seeding, Swagger and Actuator. `application-mysql.properties` overrides only the datasource when the `mysql` profile is active.

---

## B. JPA, Hibernate and ORM mapping

**11. What is JPA? What is Hibernate?**
JPA (Jakarta Persistence API) is the specification: annotations such as `@Entity`, `@Id`, `@OneToMany` and the `EntityManager` contract. Hibernate ORM 6 is the implementation that generates SQL, manages the persistence context and provides `SessionFactory`, `Session` and `Transaction`.

**12. Which entities exist?**
`Person` (abstract), `Student`, `Instructor`, `Course` and `Enrollment` in `domain/`. There are five mapped entities, which is also what `/api/diagnostics/session-factory` reports.

**13. Explain the relationships.**
- `Person` → `Student`: inheritance (1:0..1 physically).
- `Person` → `Instructor`: inheritance.
- `Instructor` 1 → N `Course` (`@OneToMany(mappedBy = "instructor")`, `@ManyToOne` on `Course`).
- `Student` 1 → N `Enrollment`; `Course` 1 → N `Enrollment`.
- `Enrollment` is the join entity that resolves the M:N relationship and stores marks, grade, grade point, semester, date and grading source.

**14. What is `mappedBy`?**
It tells Hibernate that the association is already mapped by the field on the other side. `Student.enrollments` uses `mappedBy = "student"`, so the foreign key lives in `enrollments.student_id`; no extra join table is created.

**15. What does cascade = ALL and orphanRemoval = true do?**
Deleting a student cascades to their enrollments; removing an enrollment from the collection deletes its row. The same applies to `Course.enrollments`. This matches the business rule that a transcript line cannot exist without its student or course.

**16. Why is `fetch = LAZY` used on `@ManyToOne`?**
It avoids loading a student and course for every enrollment unless needed. The repositories use `@EntityGraph` to fetch exactly the associations required by a page, preventing the N+1 query problem. `spring.jpa.open-in-view=false` makes lazy-loading mistakes visible during development.

**17. Which ID generation strategy is used?**
`GenerationType.IDENTITY` on `Person.id`, `Course.id` and `Enrollment.id`. Hibernate omits the ID from the INSERT and reads the auto-increment value generated by H2/MySQL. It is simple and works well with MySQL, but it prevents JDBC batch inserts because each ID must be known immediately.

**18. What is the `@PrePersist` callback used for?**
`Person.onCreate()` sets `createdAt` if missing; `Enrollment.setDefaults()` sets `enrolledOn` to today if missing. This keeps audit fields correct even when a caller forgets them.

**19. Why is `Person` abstract?**
A person must be either a student or an instructor in this domain. Making the class abstract prevents accidental `new Person()` and makes the inheritance intent explicit. JPA still maps it as an entity root.

**20. What is the difference between `save` and `saveAndFlush`?**
`save` persists or merges and returns the managed entity; the SQL may be delayed until flush/commit. `saveAndFlush` forces the SQL immediately. The project uses `save` and lets the transaction commit, except where generated IDs are needed in the response (Hibernate executes the INSERT immediately for IDENTITY anyway).

**21. How is the schema created?**
Default H2: `spring.jpa.hibernate.ddl-auto=update`, so Hibernate creates/updates tables on startup. Tests use `create-drop` against an in-memory H2. MySQL DDL is also provided in `database/schema.sql` for manual setup.

**22. How do you see the SQL?**
Set `spring.jpa.show-sql=true` or `logging.level.org.hibernate.SQL=DEBUG`. The H2 console at `/h2-console` can run the same queries interactively.

**23. What is the persistence context / first-level cache?**
It is the set of managed entities inside one transaction/`EntityManager`. If you fetch the same row twice, Hibernate returns the same Java object without a second SELECT. It also means changes to managed entities are flushed automatically. This caused an earlier bug: a newly saved enrollment was not visible in a student's already-loaded collection until the association was added on both sides (`EnrollmentService.create` now does this).

**24. What is a detached entity?**
An entity whose persistence context has closed. `save` on a detached entity with an ID performs a merge (SELECT + UPDATE), not a pure INSERT. The project works with managed entities inside `@Transactional` service methods to avoid this.

**25. Why is `@Transactional` on services rather than controllers?**
The service defines the unit of work. It can load a student, evaluate marks remotely, save the enrollment and update both sides of the relationship atomically. If the grading call fails unexpectedly, the transaction rolls back; the fallback path is handled before persistence.

---

## C. HQL and inheritance

**26. What is HQL?**
Hibernate Query Language. It uses entity and property names (`Student`, `s.rollNumber`, `s.enrollments`) rather than table and column names. Hibernate translates it to vendor-specific SQL. It supports joins, aggregates, subqueries, parameters and projection interfaces.

**27. Which HQL queries are implemented?**
1. Top performers with `JOIN`, `GROUP BY`, `HAVING avg(...) >= :minimumAverage`, ordered by average.
2. Students without enrollments using `NOT IN (select e.student.id from Enrollment e)`.
3. Department counts with `GROUP BY` and `ORDER BY count`.
4. Course statistics with `avg`, `min`, `max`, `count` grouped by course.
5. Grade distribution with `GROUP BY e.grade`.

They are in `StudentRepository` and `EnrollmentRepository`.

**28. Show the top-performer query.**

```java
@Query("""
    select s.id as id, s.rollNumber as rollNumber, s.firstName as firstName,
           s.lastName as lastName, s.department as department,
           avg(e.marks) as averageMarks, count(e.id) as courseCount
    from Student s join s.enrollments e
    where e.marks is not null
    group by s.id, s.rollNumber, s.firstName, s.lastName, s.department
    having avg(e.marks) >= :minimumAverage
    order by avg(e.marks) desc
    """)
List<StudentAverageProjection> findTopPerformers(double minimumAverage);
```

**29. Why use a projection interface?**
`StudentAverageProjection` exposes only the aggregate columns, so Spring Data can map the result without loading full entities. It is efficient and keeps the report DTO small.

**30. What is the difference between HQL and native SQL?**
HQL is portable across databases and entity-aware; native SQL is database-specific and bypasses some mapping. HQL is preferred here, and the provided MySQL schema is only for manual setup.

**31. Explain the inheritance mapping.**
`Person` uses `@Inheritance(strategy = InheritanceType.JOINED)`. Shared columns (`id`, first/last name, email, phone, date of birth, created_at) live in `persons`. `students` stores `person_id` as both PK and FK, plus roll number, department, admission year, active. `instructors` stores employee code, designation and specialization. Fetching a Student produces a join between `students` and `persons`.

**32. Why JOINED rather than SINGLE_TABLE?**
JOINED is normalised: instructor-only columns are not nullable in the student table, and shared columns are not duplicated. The cost is a join per query. SINGLE_TABLE is faster but creates many nullable columns; TABLE_PER_CLASS duplicates shared fields and complicates polymorphic queries.

**33. Is there a discriminator column?**
With `InheritanceType.JOINED`, Hibernate does not need a discriminator for basic subclass tables in this setup; the presence of a row in `students` or `instructors` identifies the subtype. Polymorphic queries still work through the joins. (If a discriminator were required, `@DiscriminatorColumn` could be added.)

**34. How does the app prove inheritance works?**
`RepositoryHqlTest` persists an instructor and students, queries `Person`/`Student`, and asserts the subtype and shared fields. The Reports page's “Mapped entities 5” and `/api/diagnostics/session-factory` show all five entities.

**35. What is an `@EntityGraph`?**
It tells Spring Data which associations to fetch eagerly for one query, overriding the default lazy strategy. For example, `findDetailedById` fetches `enrollments` and `enrollments.course` in one query, avoiding a `LazyInitializationException` when the view renders.

---

## D. SessionFactory, Session and Transaction

**36. What is `SessionFactory`?**
A heavyweight, thread-safe factory that holds Hibernate metadata and connection settings. One instance exists per datasource/application. In Spring Boot it is created from the `EntityManagerFactory`; `HibernateDemoService.sessionFactoryInfo()` unwraps it with `entityManagerFactory.unwrap(SessionFactory.class)`.

**37. What is a `Session`?**
A lightweight, short-lived unit of work, roughly a JDBC connection plus a first-level cache. It is not thread-safe; open one per request/operation and close it. The demo uses `try (Session session = sessionFactory.openSession())`.

**38. What is a `Transaction`?**
A unit that groups database operations atomically: `beginTransaction()`, work, `commit()`; on failure `rollback()`. In Spring, `@Transactional` does this declaratively via AOP. The demo performs it explicitly to show the mechanics.

**39. Why does the demo use `sessionFactory.openSession()` instead of `getCurrentSession()`?**
`openSession()` creates a new session that we control and close ourselves, making the lifecycle visible in one method. `getCurrentSession()` requires a configured context (usually Spring's transaction-bound session) and hides the lifecycle.

**40. How do Spring Data JPA and Hibernate relate in this project?**
Repositories are proxies implemented by Spring Data. They use an `EntityManager`, which is a Hibernate `Session` underneath. `@Transactional` starts/commits the Hibernate transaction. The explicit demo shows the same machinery without the repository abstraction.

**41. What happens if the HQL fails?**
`HibernateDemoService` catches `RuntimeException`, rolls back if the transaction is active, and rethrows. The REST advice converts it to a 500 response; the AOP aspect logs the failure.

---

## E. Dependency injection and ApplicationContext

**42. What is dependency injection?**
Instead of a class creating its collaborators (`new StudentRepository()`), Spring supplies them through a constructor. Every controller/service in this project uses constructor injection, which makes dependencies explicit and testable.

**43. What is the `ApplicationContext`?**
The Spring IoC container. It instantiates beans, resolves constructor dependencies, applies bean post-processors (including AOP proxies), manages scopes and publishes events. `SpringApplication.run` returns a `ConfigurableApplicationContext`.

**44. What is the difference between `BeanFactory` and `ApplicationContext`?**
`BeanFactory` is the basic container: bean creation and DI. `ApplicationContext` extends it with eager singleton initialisation, internationalisation, event publishing, resource loading and annotation support. Boot applications use `ApplicationContext` by default.

**45. Where is a `@Bean` declared?**
`AppConfig.gradingRestClient(...)` creates and configures the `RestClient` bean with a base URL from properties and short timeouts. This demonstrates Java-based configuration alongside `@Component` scanning.

**46. How are `@Service`, `@Repository` and `@RestController` different?**
All are stereotype annotations that register beans. `@Repository` adds persistence exception translation; `@RestController` combines `@Controller` and `@ResponseBody`; `@Service` marks business logic. The distinction is semantic plus specific behaviour.

**47. What is bean scope?**
Default is singleton (one instance per context). `SessionFactory` is a singleton; `Session` is not a Spring bean here and is created per operation. A prototype bean would be created on every injection.

**48. How would you inject a different grading client in a test?**
Use `@MockBean`/`@MockitoBean` for `GradingClient`, or provide a test `@Configuration` with a stub `RestClient`. `application-test.properties` sets `grading.service.enabled=false`, so tests use the local calculator and do not depend on port 8081.

---

## F. AOP

**49. What is AOP?**
Aspect-Oriented Programming separates cross-cutting concerns (logging, security, transactions) from business code. Spring AOP creates proxies around beans and applies advice at join points.

**50. Where is AOP used?**
`LoggingAspect` has `@Aspect` and `@Around("execution(* com.example.studentrecord.service..*(..))")`. It logs `--> Class.method(..)`, measures duration, logs `<-- ... completed in X ms`, and logs a warning/error if the method throws. Spring's `@Transactional` itself is implemented with AOP.

**51. What is the difference between `@Before`, `@After`, `@Around`?**
`@Before` runs before the method; `@After` runs after (like `finally`); `@Around` wraps the call and controls whether to proceed, so it can measure duration and catch exceptions. This project uses `@Around` for timing.

**52. Why didn't you log controller arguments?**
Arguments may contain personal data and large objects. The aspect logs method signatures and outcomes, not values. This is also a sensible production privacy choice.

**53. Does AOP work on private methods?**
No, Spring AOP proxies only public method calls that go through the proxy. Self-invocation inside the same bean also bypasses the proxy. That is why the aspect targets public service methods called from controllers.

**54. What is a pointcut?**
An expression selecting join points. `execution(* com.example.studentrecord.service..*(..))` selects all public methods in the service package and subpackages. `@Around` binds that pointcut to the advice method.

---

## G. REST, web services and SOAP

**55. What is a web service?**
A software service exposed over a network using a standard protocol, designed to be called by other applications rather than humans. This project's grading service is a REST web service.

**56. What makes an API RESTful?**
Client-server separation, statelessness, cacheability, a uniform interface (resources identified by URIs, standard methods), and (optionally) layered systems. Here: `/api/students/8` is a resource, GET reads it, POST creates, PUT updates, DELETE removes, and each request carries all needed state.

**57. What is the difference between REST and SOAP?**

| REST | SOAP |
| --- | --- |
| Architectural style, usually HTTP/JSON | Protocol with a strict XML envelope |
| Uses HTTP methods and status codes | Usually POST with a WSDL-described operation |
| Lightweight, browser-friendly | Heavyweight, strong contracts/WS-* standards |
| Easy to cache and evolve | Strong typing and built-in security/transactions |
| This project | Not used |

**58. Which HTTP methods are demonstrated?**
GET (lists, detail, transcript, reports, diagnostics), POST (create student/course/enrollment, evaluate grade), PUT (update student/course/marks), DELETE (student/course/enrollment). The Postman collection and `RestApiIntegrationTest` exercise them.

**59. What is idempotency?**
Repeating the same request has the same effect. GET, PUT and DELETE are idempotent by design; POST is not (two POSTs create two resources). The enrollment POST protects against duplicates with a unique constraint (returns 409), even though POST itself is not idempotent.

**60. Why return 201 with a Location header?**
It tells the client that a resource was created and where to find it. `ResponseEntity.created(URI.create("/api/students/" + id)).body(...)` does this.

**61. What is content negotiation?**
The client's `Accept` header selects a representation. These controllers produce JSON; Thymeleaf controllers produce HTML. Spring's message converters handle the JSON serialization.

**62. How is the API documented?**
springdoc-openapi scans the controllers and generates `/v3/api-docs`; Swagger UI at `/swagger-ui.html` renders it and allows “Try it out”. `@Operation` and `@Tag` annotations provide summaries and grouping.

**63. What is the difference between `@Controller` and `@RestController`?**
`@RestController` = `@Controller` + `@ResponseBody`; every method's return value is written to the response body (JSON), not resolved as a view. `web/` controllers return view names; `api/` controllers return objects.

**64. How are errors represented?**
A stable JSON object with `timestamp`, `status`, `error`, `message`, `path` and optional `fieldErrors`. Proper HTTP status codes are used (400/404/409/500), not always 200.

---

## H. Monolith, microservices and Spring Cloud

**65. What is a monolithic architecture?**
One deployable unit containing all features. This project's main application is a monolith: UI, REST API, services and persistence run in one JVM on port 8080.

**66. What is a microservice architecture?**
Independent services organized around business capabilities, each deployable and scalable separately, communicating over the network (often HTTP/REST or messaging). The grading service is a small example: it owns the grading rules, runs on port 8081 and has no database.

**67. Compare monolith and microservices.**

| Monolith | Microservices |
| --- | --- |
| Single deployment, simple local development | Multiple deployments, more operational complexity |
| In-process calls, easy transactions | Network calls, latency and partial failure |
| One database is common | Data ownership per service is preferred |
| Scaling means scaling the whole app | Scale only the hot service |
| Good for small teams/projects | Good for large, independently evolving domains |

**68. Why is grading a separate service here?**
It is a pure, reusable business rule with no persistence, making it a clean demonstration of REST interaction. The main app can still function if it is offline, which shows a realistic resilience concern.

**69. How do the services communicate?**
`GradingClient` uses Spring's synchronous `RestClient` to POST `/api/grade/evaluate` and GET `/api/grade/health`. JSON is deserialized into records. Timeouts are 1.5 s connect / 2.5 s read.

**70. What happens when the grading service is down?**
`RestClientException` is caught, a warning is logged, and `LocalGradeCalculator` produces the same scale result with `gradedBy = "local-fallback"`. The enrollment still saves. The dashboard health card reflects the state.

**71. What is Spring Cloud?**
A set of projects for the operational concerns of distributed systems: configuration, service discovery, API gateway, load balancing, circuit breakers, distributed tracing and messaging. It is not required for this small two-service demo.

**72. Which Spring Cloud feature would you use next?**
- **Spring Cloud Config** for centralized configuration.
- **Eureka** for service discovery instead of hard-coded `localhost:8081`.
- **Spring Cloud Gateway** as a single entry point.
- **Circuit Breaker (Resilience4j)** for retries, timeouts and fallbacks; our manual try/catch is a simplified version.
- **Micrometer Tracing** for distributed tracing.

**73. Why not use Spring Cloud now?**
It adds infrastructure (config server, registry, gateway) that a two-service mini-project does not need and that would be hard to explain in a viva. The assignment asks for a small independent service and REST interaction; that is implemented.

**74. How would you containerize the services?**
The shared `Dockerfile` uses a Maven build stage and a JRE runtime stage. `docker-compose.yml` builds both images, connects them on a Compose network, passes `GRADING_SERVICE_BASE_URL=http://grading-service:8081`, adds health checks and persists H2 data in a named volume.

**75. What is the difference between horizontal and vertical scaling here?**
Vertical: give the JVM more CPU/RAM. Horizontal: run multiple instances behind a load balancer. The grading service is stateless and easy to scale horizontally; the monolith would need sticky sessions or externalized sessions, and the H2 file database would need to move to a shared database such as MySQL.

---

## I. DevOps

**76. What is DevOps?**
A culture and set of practices that unite development and operations to deliver software faster and more reliably: version control, automated builds/tests, CI/CD, infrastructure as code, monitoring and feedback.

**77. How is Git used?**
The repository tracks source, docs, SQL, Postman files and CI config. Commits are small and descriptive; `.gitignore` excludes `target/`, `data/`, logs and IDE files. The remote is GitHub (`origin`).

**78. What is the Git workflow?**
`git status` → `git add` → `git commit -m "..."` → `git push origin main`. For team work: feature branch → pull request → review → merge. The CI workflow runs on every push and pull request to `main`.

**79. What is Maven?**
A build and dependency-management tool. It reads `pom.xml`, downloads dependencies, compiles, runs tests, packages JARs and can run the app. This is a multi-module reactor: the parent POM builds `grading-service` and `student-record-system` in dependency order.

**80. Explain the Maven lifecycle.**
`validate` → `compile` → `test` → `package` → `verify` → `install` → `deploy`. `./mvnw clean verify` deletes `target/`, compiles, runs tests, packages and runs verification. `spring-boot:run` starts a module directly.

**81. What is the Maven Wrapper?**
`mvnw`/`mvnw.cmd` and `.mvn/wrapper/maven-wrapper.properties` download and run a pinned Maven version (3.9.9). Teammates and CI do not need Maven installed; they run `./mvnw`.

**82. What does the GitHub Actions workflow do?**
`.github/workflows/ci.yml` checks out the code, installs JDK 17 with Maven caching, runs `./mvnw -B clean verify`, uploads both JARs as artifacts, then builds both Docker images. It runs on push, pull request and manual dispatch.

**83. What is CI/CD?**
Continuous Integration: merge frequently and automatically build/test every change. Continuous Delivery/Deployment: keep the artifact releasable and deploy automatically (or with approval). The workflow implements CI and produces deployable images; deployment is manual via Compose.

**84. Which DevOps tools are relevant?**
Git/GitHub (version control and collaboration), Maven (build), JUnit (test), GitHub Actions (CI), Docker/Compose (packaging and environment), Spring Boot Actuator (health), and optionally Prometheus/Grafana (monitoring) or Kubernetes (orchestration).

**85. How do you reproduce the environment?**
`./mvnw clean verify` then `docker compose up --build`. The Docker images pin JDK 17 and Maven 3.9.9; the database is created/seeded automatically. This avoids “works on my machine”.

---

## J. Project-specific questions

**86. Why these entities and not a single `Student` table?**
A student is a person; instructors share the same identity fields. JOINED inheritance models that cleanly. Enrollment is a real business entity (marks, grade, date), not just a hidden join table.

**87. How is GPA calculated?**
Credit-weighted average of grade points: `Σ(gradePoint × credits) / Σcredits`, rounded to two decimals. The grading service exposes `/api/grade/gpa`; the main app computes each student's GPA from enrollment grade points. A 4-credit course therefore counts more than a 3-credit course.

**88. How does grade evaluation work?**
Marks are validated (0–100) and mapped to bands: A+ ≥90, A ≥85, B+ ≥75, B ≥65, C ≥55, D ≥45, otherwise F. The scale is owned by `grading-service`; the main app stores the returned grade and point.

**89. What happens if two users enroll the same student in the same course?**
The database has `UNIQUE (student_id, course_id)`. The service also checks with `existsByStudentIdAndCourseId` and throws `DuplicateResourceException`, producing a 409 before the constraint is hit. Both layers are intentional: the service gives a friendly message, the constraint protects data integrity.

**90. Why does deleting a student also delete enrollments?**
The relationship is composition: an enrollment has no meaning without a student (and course). Cascade + orphan removal implement that. The UI warns before deleting.

**91. How is the transcript sorted and computed?**
`EnrollmentRepository.findByStudentIdDetailed` loads enrollments ordered by course code with student/course fetched. `StudentService.findDetail` maps them to `EnrollmentView`, sums credits, counts passed courses and uses the student's average/grade points for GPA.

**92. How does search work?**
`StudentRepository.searchWithEnrollments` uses `lower(...) like lower(concat('%', :query, '%'))` across first name, last name, roll number and department, with an `@EntityGraph` for enrollments. Course search is analogous across code, title and department.

**93. Why is `open-in-view` disabled?**
It prevents the web layer from silently holding a Hibernate session open during rendering. All required data must be fetched in the service/repository, which avoids accidental N+1 queries and makes tests realistic. The `@EntityGraph` queries provide the needed associations.

**94. How is the dashboard built?**
`HomeController` calls several services: counts, overall average, top performers, course stats, department stats, recent enrollments and grading-service health. The template renders KPI cards, tables and bars. No calculation is done in Thymeleaf beyond formatting.

**95. What is the H2 console URL and login?**
`http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:file:./data/studentdb`, user `sa`, empty password. It only works while the main app is running.

**96. How do you reset the demo data?**
Stop the main app, delete `student-record-system/data/`, and start again. `DataSeeder` runs because `studentRepository.count() == 0` and inserts the sample data.

**97. What would you improve next?**
Authentication/authorization (Spring Security), pagination for large lists, Flyway/Liquibase migrations, PostgreSQL/MySQL in production, a circuit breaker (Resilience4j), centralized config/discovery, more granular integration tests with Testcontainers, and observability (Micrometer/Prometheus).

**98. What was the hardest bug?**
The transcript endpoint returned no enrollments because an already-loaded student collection was stale in the persistence context. The fix was to add the new enrollment to both the student's and course's collections in `EnrollmentService.create`, keeping both sides of the bidirectional relationship consistent.

**99. How do you know the application works?**
All 52 automated tests pass; the Postman/Newman collection runs 34 requests with 59 assertions and zero failures; the Swagger transcript request returns HTTP 200; each page is captured in `docs/screenshots/`; both health endpoints return UP.

**100. What is your individual contribution?** _(fill in before the viva)_
Describe concrete work: e.g., “I designed the entity model and JOINED inheritance, implemented the repositories/HQL reports, built the grading microservice and its tests, and produced the UI and documentation.” Be ready to open the relevant files and explain any line.

---

## K. 60-second demonstration script

1. Show the dashboard: 9 students, 6 courses, 40 enrollments, grading service online.
2. Open a student profile: transcript, GPA, pass count.
3. Open a course: roster, average, pass rate, grade distribution.
4. Record a grade for Rohan Das (Reports → Students without enrollments → Enroll, or Enrollments → New). Show the grade badge and `gradedBy`.
5. Open Reports: HQL aggregates, top performers threshold, Hibernate internals.
6. Open Swagger, execute `GET /api/students/8/transcript`, show 200 JSON.
7. Show the Postman collection and the Newman summary (34 requests, 59 assertions).
8. Show the architecture diagram and explain ports 8080/8081, RestClient, fallback.
9. Stop the grading service and show “Local fallback active” on the dashboard; restart it.
10. Show Git history, Maven wrapper, CI workflow and Docker Compose.

---

## L. If the examiner asks you to change something

- **Add a field:** add it to the entity with a column, add it to the form/request DTO, update the service mapping, add it to the template, add a validation test.
- **Add a query:** add `@Query` to the repository with a projection, expose it through a service and controller, add a repository test.
- **Add a REST endpoint:** create a method in the appropriate `api/` controller, return a DTO, use `ResponseEntity`, add a MockMvc test and a Postman request.
- **Add a microservice call:** define a DTO, add a method to `GradingClient`, catch `RestClientException`, provide a fallback, set a timeout.
- **Switch database:** activate the `mysql` profile and set credentials; schema scripts are in `database/`.
