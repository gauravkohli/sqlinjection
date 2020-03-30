package com.okta.blog.sqlinjection.controller;

import com.okta.blog.sqlinjection.domain.Employee;
import com.okta.blog.sqlinjection.repository.EmployeeRepository;
import com.okta.blog.sqlinjection.repository.jdbc.EmployeeRepositoryJdbcSafe;
import com.okta.blog.sqlinjection.repository.jdbc.EmployeeRepositoryJdbcUnSafe;
import com.okta.blog.sqlinjection.repository.jdbc.GlobalRepositoryJdbc;
import com.okta.blog.sqlinjection.repository.jpa.EmployeeRepositoryJpaSafe;
import com.okta.blog.sqlinjection.repository.jpa.EmployeeRepositoryJpaUnSafe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeRepositoryJdbcUnSafe repositoryJdbcUnSafe;

    @Autowired
    private EmployeeRepositoryJdbcSafe repositoryJdbcSafe;

    @Autowired
    private EmployeeRepositoryJpaUnSafe repositoryJpaUnSafe;

    @Autowired
    private EmployeeRepositoryJpaSafe repositoryJpaSafe;

    @Autowired
    private GlobalRepositoryJdbc globalRepositoryJdbc;

    @GetMapping("/filterUserUnSafe")
    public List<Employee> filterByUsernameUnSafe(@RequestParam(value = "name") String name) {
        return employeeRepository.filterByUsername(name);
    }

    @GetMapping("/filterUserJdbcUnSafe")
    public List<Employee> filterByUsernameJdbcUnSafe(@RequestParam(value = "name") String name) {
        return repositoryJdbcUnSafe.filterByUsername(name);
    }

    @GetMapping("/filterUserJdbcSafe")
    public List<Employee> filterByUsernameJdbcSafe(@RequestParam(value = "name") String name) {
        return repositoryJdbcSafe.filterByUsername(name);
    }

    @GetMapping("/filterUserJpaUnSafe")
    public List<Employee> filterByUsernameJpaUnSafe(@RequestParam(value = "name") String name) {
        return repositoryJpaUnSafe.filterByUsername(name);
    }

    @GetMapping("/filterUserJpaSafe")
    public List<Employee> filterByUsernameJpaSafe(@RequestParam(value = "name") String name) {
        return repositoryJpaSafe.filterByUsername(name);
    }

    @GetMapping("/filterUserJpaStoredProcedureUnSafe")
    public List<Employee> filterByUsernameJpaStoredProcedureUnSafe(@RequestParam(value = "name") String name) {
        return repositoryJpaUnSafe.filterByUsernameStoredProcedure(name);
    }


    @GetMapping("/filterUserJpaStoredProcedureSafe")
    public List<Employee> filterByUsernameJpaStoredProcedureSafe(@RequestParam(value = "name") String name) {
        return repositoryJpaSafe.filterByUsernameStoredProcedure(name);
    }

    @GetMapping("/loginJdbcUnSafe")
    public Employee loginJdbcUnSafe(@RequestParam(value = "name") String name, @RequestParam(value = "password") String password) {
        return globalRepositoryJdbc.login(name, password);
    }

    @GetMapping("/filterUserGlobalAccessUnSafe")
    public List<Employee> filterByUsernameGlobalAccessUnSafe(@RequestParam(value = "name") String name) {
        return globalRepositoryJdbc.filterByUsername(name);
    }

}