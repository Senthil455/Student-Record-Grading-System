package com.example.studentrecord.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

/** OpenAPI metadata displayed by Swagger UI at /swagger-ui.html. */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Scholaris - Student Record and Grading System API",
                version = "1.0.0",
                description = """
                        REST API for managing students, courses, enrollments and grade reports.
                        The API demonstrates GET, POST, PUT and DELETE methods, validation,
                        consistent error responses and integration with the grading microservice.
                        """,
                contact = @Contact(name = "Web Programming Mini Project"),
                license = @License(name = "MIT")
        )
)
public class OpenApiConfig {
}
