package com.example.studentrecord.service;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Version;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Explicit Hibernate demonstration used by the reports page and the viva.
 *
 * <p>Spring Data JPA normally hides the plumbing. This service deliberately
 * uses a {@link SessionFactory}, opens a {@link Session}, starts a
 * {@link Transaction}, runs HQL and commits. It makes the roles of those three
 * Hibernate objects visible in the running application.</p>
 */
@Service
public class HibernateDemoService {

    private final EntityManagerFactory entityManagerFactory;

    public HibernateDemoService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public Map<String, Object> sessionFactoryInfo() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("hibernateVersion", Version.getVersionString());
        info.put("sessionFactoryClass", sessionFactory.getClass().getName());
        info.put("sessionFactoryOpen", sessionFactory.isOpen());
        info.put("entityCount", sessionFactory.getMetamodel().getEntities().size());
        info.put("entityNames", sessionFactory.getMetamodel().getEntities().stream()
                .map(EntityType::getName)
                .sorted()
                .toList());
        info.put("jdbcUrl", String.valueOf(entityManagerFactory.getProperties()
                .getOrDefault("jakarta.persistence.jdbc.url", "configured by Spring Boot")));
        info.put("dialect", String.valueOf(entityManagerFactory.getProperties()
                .getOrDefault("hibernate.dialect", "auto-detected")));
        return info;
    }

    /**
     * Runs HQL through a Hibernate Session and an explicit transaction.
     * The transaction is committed on success and rolled back on failure.
     */
    public List<Map<String, Object>> topPerformersViaHibernateSession(double minimumAverage) {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        List<Map<String, Object>> results = new ArrayList<>();

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<Object[]> rows = session.createQuery("""
                                select s.rollNumber, s.firstName, s.lastName, avg(e.marks)
                                from Student s join s.enrollments e
                                where e.marks is not null
                                group by s.id, s.rollNumber, s.firstName, s.lastName
                                having avg(e.marks) >= :minimumAverage
                                order by avg(e.marks) desc
                                """, Object[].class)
                        .setParameter("minimumAverage", minimumAverage)
                        .list();

                for (Object[] row : rows) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("rollNumber", row[0]);
                    item.put("studentName", row[1] + " " + row[2]);
                    item.put("averageMarks", Math.round(((Double) row[3]) * 100.0) / 100.0);
                    results.add(item);
                }
                transaction.commit();
            } catch (RuntimeException ex) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw ex;
            }
        }
        return results;
    }
}
