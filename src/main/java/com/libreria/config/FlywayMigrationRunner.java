package com.libreria.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// Corre después de que Hibernate (ddl-auto=update) ya creó las tablas; con spring.flyway.enabled=false
// se evita que Spring Boot haga esperar al EntityManagerFactory a que Flyway migre primero.
@Component
@RequiredArgsConstructor
public class FlywayMigrationRunner implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();
    }
}
