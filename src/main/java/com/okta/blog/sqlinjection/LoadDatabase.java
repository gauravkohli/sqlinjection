package com.okta.blog.sqlinjection;

import com.okta.blog.sqlinjection.domain.Employee;
import com.okta.blog.sqlinjection.domain.Role;
import com.okta.blog.sqlinjection.repository.jpa.EmployeeRepositoryJpaSafe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(EmployeeRepositoryJpaSafe repository) {
        return args -> {
            log.info("Preloading " + repository.save(new Employee("Bilbo", "secret", Role.MANAGER)));
            log.info("Preloading " + repository.save(new Employee("Frodo", "secret2", Role.STAFF)));
        };
    }
}