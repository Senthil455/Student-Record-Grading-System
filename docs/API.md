# Scholaris REST API

Complete reference for the two services in this repository.

| Service | Base URL | Swagger UI |
| --- | --- | --- |
| Main application | `http://localhost:8080` | <http://localhost:8080/swagger-ui.html> |
| Grading microservice | `http://localhost:8081` | <http://localhost:8081/swagger-ui.html> |

All request and response bodies use **JSON** (`Content-Type: application/json`). Dates use ISO-8601 (`2026-10-20`). IDs are database-generated `Long` values.

---

## 1. Conventions

### 1.1 HTTP status codes

| Status | Meaning in this API |
| --- | --- |
| `200 OK` | Successful GET or PUT |
| `201 Created` | Successful POST; the `Location` header points to the new resource |
| `204 No Content` | Successful DELETE |
| `400 Bad Request` | Bean Validation failed, malformed JSON, or invalid marks/query parameter |
| `404 Not Found` | The requested student/course/enrollment does not exist |
| `409 Conflict` | Duplicate email, roll number, course code, employee code, or duplicate enrollment |
| `500 Internal Server Error` | Unexpected server failure |

### 1.2 Error contract

Every error from the main application has the same shape:

```json
{
  "timestamp": "2026-10-20T09:31:42.512Z",
  "status": 400,
  "error": "Validation failed",
  "message": "One or more request fields are invalid.",
  "path": "/api/students",
  "fieldErrors": {
    "firstName": "First name is required.",
    "email": "Email is required."
  }
}
```

`fieldErrors` is present only for validation failures. Common errors:

| Status | `error` | Example message |
| --- | --- | --- |
| 404 | `Not found` | `Student with ID 999 was not found.` |
| 409 | `Conflict` | `A student with email x@y.com already exists.` |
| 409 | `Conflict` | `Roll number 23BCS001 is already in use.` |
| 400 | `Bad request` | `Marks must be between 0 and 100.` |
| 400 | `Bad request` | `The request body or path variable could not be read.` |

The grading service uses the same shape without `fieldErrors` (it includes it for validation errors too).

### 1.3 Search

Student and course list endpoints accept an optional `q` parameter:

```bash
curl 'http://localhost:8080/api/students?q=ananya'
curl 'http://localhost:8080/api/courses?q=CS1'
```

Matching is case-insensitive across names, roll number, department, course code and title. With no `q`, the endpoint returns the full list ordered by roll number / course code.

---

## 2. Students — `/api/students`

### `GET /api/students`

Returns a list of `StudentSummary` objects with calculated course count, average marks and GPA.

```bash
curl http://localhost:8080/api/students
```

```json
[
  {
    "id": 8,
    "rollNumber": "23BCS004",
    "firstName": "Ananya",
    "lastName": "Iyer",
    "email": "ananya.iyer@student.scholaris.edu",
    "phone": null,
    "dateOfBirth": "2005-02-17",
    "department": "B.Sc Computer Science",
    "admissionYear": 2023,
    "active": true,
    "courseCount": 5,
    "averageMarks": 93.8,
    "gpa": 10.0,
    "fullName": "Ananya Iyer",
    "initials": "AI"
  }
]
```

### `GET /api/students/{id}`

Returns a `StudentDetail`: the summary, transcript lines, credit totals and pass count.

```bash
curl http://localhost:8080/api/students/8
```

```json
{
  "student": {
    "id": 8,
    "rollNumber": "23BCS004",
    "firstName": "Ananya",
    "lastName": "Iyer",
    "email": "ananya.iyer@student.scholaris.edu",
    "dateOfBirth": "2005-02-17",
    "department": "B.Sc Computer Science",
    "admissionYear": 2023,
    "active": true,
    "courseCount": 5,
    "averageMarks": 93.8,
    "gpa": 10.0,
    "fullName": "Ananya Iyer",
    "initials": "AI"
  },
  "enrollments": [
    {
      "id": 16,
      "studentId": 8,
      "studentName": "Ananya Iyer",
      "rollNumber": "23BCS004",
      "courseId": 1,
      "courseCode": "CS101",
      "courseTitle": "Programming in Java",
      "credits": 4,
      "semester": 1,
      "marks": 95.0,
      "grade": "A+",
      "gradePoint": 10.0,
      "enrolledOn": "2026-10-04",
      "gradedBy": "seed-data",
      "initials": "AI",
      "passed": true,
      "gradeCss": "grade-a"
    }
  ],
  "totalCredits": 17.0,
  "gradedCourses": 5,
  "passedCourses": 5,
  "gpa": 10.0
}
```

`GET /api/students/{id}/transcript` is an alias with the same response, provided for readability.

### `POST /api/students`

Creates a student. Required fields: `firstName`, `lastName`, `email`, `rollNumber`, `department`, `admissionYear`. `active` defaults to `true`.

```bash
curl -i -X POST http://localhost:8080/api/students \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Nila",
    "lastName": "Sundar",
    "email": "nila.sundar@student.scholaris.edu",
    "phone": "9840012399",
    "dateOfBirth": "2005-11-02",
    "rollNumber": "23BCS010",
    "department": "B.Sc Computer Science",
    "admissionYear": 2023,
    "active": true
  }'
```

Response: `201 Created`, `Location: /api/students/14`, body is the created `StudentSummary`.

### `PUT /api/students/{id}`

Full update using the same body as POST. Missing optional fields are set to `null`; `active` defaults to `true` if omitted.

```bash
curl -X PUT http://localhost:8080/api/students/14 \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Nila",
    "lastName": "Sundar",
    "email": "nila.sundar@student.scholaris.edu",
    "rollNumber": "23BCS010",
    "department": "BCA",
    "admissionYear": 2023,
    "active": true
  }'
```

### `DELETE /api/students/{id}`

Deletes the student and cascades to their enrollments (`204 No Content`).

```bash
curl -i -X DELETE http://localhost:8080/api/students/14
```

### Validation rules

| Field | Rule |
| --- | --- |
| `firstName`, `lastName` | not blank, max 60 |
| `email` | not blank, valid email, max 120, unique |
| `phone` | optional, 7–20 digits/`+ - ( ) space` |
| `dateOfBirth` | optional, must be in the past |
| `rollNumber` | not blank, max 20, unique (stored upper-case) |
| `department` | not blank, max 80 |
| `admissionYear` | 2000–2100 |
| `active` | optional boolean, defaults to `true` |

---

## 3. Courses — `/api/courses`

### `GET /api/courses`

```bash
curl http://localhost:8080/api/courses
```

```json
[
  {
    "id": 1,
    "code": "CS101",
    "title": "Programming in Java",
    "credits": 4,
    "semester": 1,
    "department": "B.Sc Computer Science",
    "description": "Object-oriented programming, collections, exceptions and file handling.",
    "instructorName": "Karthik Subramanian",
    "enrollmentCount": 8,
    "averageMarks": 79.0
  }
]
```

### `GET /api/courses/{id}`

Returns the course plus instructor and roster:

```json
{
  "course": { "...": "CourseSummary" },
  "instructor": { "id": 2, "fullName": "Karthik Subramanian", "designation": "Associate Professor" },
  "enrollments": [ { "studentName": "Aarav Sharma", "rollNumber": "23BCS001", "marks": 92.0, "grade": "A+" } ],
  "gradeDistribution": [ { "grade": "A+", "count": 2 } ],
  "passRate": 100.0
}
```

### `POST /api/courses`

Required: `code`, `title`, `credits` (1–10), `semester` (1–8). `instructorId` is optional and must reference an existing instructor.

```bash
curl -i -X POST http://localhost:8080/api/courses \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "CS401",
    "title": "Cloud and DevOps",
    "credits": 3,
    "semester": 7,
    "department": "B.Sc Computer Science",
    "description": "Containers, CI/CD and cloud deployment.",
    "instructorId": 2
  }'
```

Response: `201 Created` with the created `CourseSummary`.

### `PUT /api/courses/{id}`

Same body as POST. Duplicate course codes (case-insensitive) return `409 Conflict`.

### `DELETE /api/courses/{id}`

Deletes the course and its enrollments; returns `204 No Content`.

---

## 4. Enrollments — `/api/enrollments`

### `GET /api/enrollments`

```bash
curl http://localhost:8080/api/enrollments
```

```json
[
  {
    "id": 1,
    "studentId": 5,
    "studentName": "Aarav Sharma",
    "rollNumber": "23BCS001",
    "courseId": 1,
    "courseCode": "CS101",
    "courseTitle": "Programming in Java",
    "credits": 4,
    "semester": 1,
    "marks": 92.0,
    "grade": "A+",
    "gradePoint": 10.0,
    "enrolledOn": "2026-10-19",
    "gradedBy": "seed-data",
    "initials": "AS",
    "passed": true,
    "gradeCss": "grade-a"
  }
]
```

### `GET /api/enrollments/{id}`

Returns a single `EnrollmentView` or `404`.

### `POST /api/enrollments`

Enrolls a student and asks the grading microservice to convert the marks. The pair `(studentId, courseId)` must be unique.

```bash
curl -i -X POST http://localhost:8080/api/enrollments \
  -H 'Content-Type: application/json' \
  -d '{
    "studentId": 13,
    "courseId": 5,
    "marks": 82.5
  }'
```

Response: `201 Created` with `Location: /api/enrollments/41`. The returned `grade` is `B+`, `gradePoint` is `8.0`, and `gradedBy` is `grading-service` when port 8081 is running.

### `PUT /api/enrollments/{id}`

Updates only the marks and recalculates the grade.

```bash
curl -X PUT http://localhost:8080/api/enrollments/41 \
  -H 'Content-Type: application/json' \
  -d '{ "marks": 91 }'
```

The response now shows grade `A+`, grade point `10.0`.

### `DELETE /api/enrollments/{id}`

Removes the enrollment (`204 No Content`). The student and course remain.

### Rules

| Rule | Result |
| --- | --- |
| `marks` missing or outside 0–100 | `400 Bad Request` |
| Student or course does not exist | `404 Not Found` |
| Student already enrolled in the course | `409 Conflict` |
| Grading service offline | Request still succeeds; `gradedBy` becomes `local-fallback` |

---

## 5. Reports — `/api/reports`

All report endpoints are read-only and most are backed by explicit HQL aggregate queries.

| Endpoint | Query parameters | Returns |
| --- | --- | --- |
| `GET /api/reports/summary` | — | Student, course and enrollment totals + overall average |
| `GET /api/reports/top-performers` | `minAverage` (default `75`) | Students at or above the average, ranked |
| `GET /api/reports/course-performance` | — | Per course: average, highest, lowest marks, enrollment count |
| `GET /api/reports/department-distribution` | — | Student count per department |
| `GET /api/reports/grade-distribution` | — | Number of enrollments per letter grade |

Examples:

```bash
curl http://localhost:8080/api/reports/summary
curl 'http://localhost:8080/api/reports/top-performers?minAverage=85'
curl http://localhost:8080/api/reports/course-performance
```

```json
{
  "students": 9,
  "activeStudents": 9,
  "courses": 6,
  "enrollments": 40,
  "gradedEnrollments": 40,
  "overallAverage": 78.7
}
```

```json
[
  { "id": 8, "rollNumber": "23BCS004", "fullName": "Ananya Iyer", "department": "B.Sc Computer Science", "averageMarks": 93.8, "courseCount": 5 },
  { "id": 5, "rollNumber": "23BCS001", "fullName": "Aarav Sharma", "department": "B.Sc Computer Science", "averageMarks": 89.0, "courseCount": 5 }
]
```

```json
[
  { "code": "CS301", "title": "Software Engineering", "averageMarks": 86.5, "enrollmentCount": 4, "highestMarks": 94.0, "lowestMarks": 76.0 }
]
```

---

## 6. Hibernate diagnostics — `/api/diagnostics`

These endpoints exist to demonstrate Hibernate internals during the viva.

### `GET /api/diagnostics/session-factory`

```json
{
  "hibernateVersion": "6.6.53.Final",
  "sessionFactoryClass": "org.hibernate.internal.SessionFactoryImpl",
  "sessionFactoryOpen": true,
  "entityCount": 5,
  "entityNames": ["Course", "Enrollment", "Instructor", "Person", "Student"],
  "jdbcUrl": "configured by Spring Boot",
  "dialect": "auto-detected"
}
```

### `GET /api/diagnostics/hibernate/top-performers?minAverage=80`

Runs HQL inside an explicit `SessionFactory.openSession()` + `Transaction`:

```json
[
  { "rollNumber": "23BCS004", "studentName": "Ananya Iyer", "averageMarks": 93.8 },
  { "rollNumber": "23BCS001", "studentName": "Aarav Sharma", "averageMarks": 89.0 }
]
```

---

## 7. Grading microservice — `http://localhost:8081`

### `GET /api/grade/scale`

```json
[
  { "grade": "A+", "minimumMarks": 90.0, "maximumMarks": 100.0, "gradePoint": 10.0, "passed": true, "remark": "Outstanding" },
  { "grade": "A",  "minimumMarks": 85.0, "maximumMarks": 90.0,  "gradePoint": 9.0,  "passed": true, "remark": "Excellent" }
]
```

Bands: `A+` 90–100, `A` 85–89.99, `B+` 75–84.99, `B` 65–74.99, `C` 55–64.99, `D` 45–54.99, `F` below 45.

### `POST /api/grade/evaluate`

```bash
curl -X POST http://localhost:8081/api/grade/evaluate \
  -H 'Content-Type: application/json' \
  -d '{ "marks": 92 }'
```

```json
{
  "marks": 92.0,
  "grade": "A+",
  "gradePoint": 10.0,
  "passed": true,
  "evaluatedBy": "grading-service",
  "evaluatedAt": "2026-10-20T09:35:12.345Z"
}
```

Invalid marks (`-1`, `101`, missing) return `400` with a `fieldErrors` entry.

### `POST /api/grade/gpa`

Credits are weights; arrays must be the same length.

```bash
curl -X POST http://localhost:8081/api/grade/gpa \
  -H 'Content-Type: application/json' \
  -d '{ "gradePoints": [10, 9, 8], "credits": [4, 4, 3] }'
```

```json
{
  "gpa": 9.09,
  "totalCredits": 11,
  "courses": 3,
  "evaluatedBy": "grading-service",
  "evaluatedAt": "2026-10-20T09:36:00.000Z"
}
```

### Health

```bash
curl http://localhost:8081/api/grade/health
# {"status":"UP","service":"grading-service","port":8081}
```

---

## 8. Postman

1. Open Postman → **Import**.
2. Import [`postman/Scholaris.postman_collection.json`](../postman/Scholaris.postman_collection.json) and [`postman/Scholaris.postman_environment.json`](../postman/Scholaris.postman_environment.json).
3. Select the **Scholaris Local** environment.
4. Run requests individually, or use the Collection Runner.

The collection includes:

- GET, POST, PUT and DELETE requests with real bodies.
- Variables `{{baseUrl}}`, `{{gradingUrl}}`, `{{studentId}}`, `{{courseId}}`, `{{enrollmentId}}`.
- A pre-request script that generates a unique suffix for create requests (`{{$timestamp}}`), so the collection can be run repeatedly.
- Tests asserting `200`, `201`, `204` or `400` as appropriate.

> `DELETE` requests change the seeded data. Reset by stopping the main app and deleting `student-record-system/data/` before a fresh demonstration.

---

## 9. API testing evidence

- Swagger UI for the main app: [`screenshots/swagger-main.png`](screenshots/swagger-main.png)
- Executed transcript request with a real `200` response: [`screenshots/swagger-api-test.png`](screenshots/swagger-api-test.png)
- Grading service Swagger: [`screenshots/swagger-grading.png`](screenshots/swagger-grading.png)
- Raw curl transcript, status codes and validation/conflict cases: [`api-test-output.txt`](api-test-output.txt)
- Automated REST integration test: `student-record-system/src/test/java/com/example/studentrecord/api/RestApiIntegrationTest.java`
