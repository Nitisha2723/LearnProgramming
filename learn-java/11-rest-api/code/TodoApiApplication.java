package com.example.todoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Todo REST API application.
 *
 * <p>Spring Boot auto-configures:
 * <ul>
 *   <li>Embedded Tomcat server on port 8080</li>
 *   <li>Jackson JSON serialization/deserialization</li>
 *   <li>Spring MVC dispatcher servlet</li>
 *   <li>Bean validation (Jakarta Validation)</li>
 * </ul>
 *
 * <p>Run this class to start the application, then test with:
 * <pre>
 *   curl http://localhost:8080/api/v1/todos
 *   curl -X POST http://localhost:8080/api/v1/todos \
 *     -H "Content-Type: application/json" \
 *     -d '{"title": "Learn Spring Boot"}'
 * </pre>
 */
@SpringBootApplication
public class TodoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoApiApplication.class, args);
    }
}
