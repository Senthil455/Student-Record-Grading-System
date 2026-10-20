package com.example.studentrecord.api;

import com.example.studentrecord.service.HibernateDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Educational endpoints that expose the Hibernate SessionFactory and an HQL
 * query executed with an explicit Session and Transaction.
 */
@RestController
@RequestMapping("/api/diagnostics")
@Tag(name = "Hibernate diagnostics", description = "Demonstrates SessionFactory, Session, Transaction and HQL")
public class DiagnosticsApiController {

    private final HibernateDemoService hibernateDemoService;

    public DiagnosticsApiController(HibernateDemoService hibernateDemoService) {
        this.hibernateDemoService = hibernateDemoService;
    }

    @GetMapping("/session-factory")
    @Operation(summary = "Inspect the Hibernate SessionFactory")
    public Map<String, Object> sessionFactory() {
        return hibernateDemoService.sessionFactoryInfo();
    }

    @GetMapping("/hibernate/top-performers")
    @Operation(summary = "Run HQL in an explicit Hibernate Session and Transaction")
    public List<Map<String, Object>> topPerformers(
            @RequestParam(defaultValue = "80") double minAverage) {
        return hibernateDemoService.topPerformersViaHibernateSession(minAverage);
    }
}
