package com.okta.blog.sqlinjection.repository.jdbc;

import com.okta.blog.sqlinjection.domain.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GlobalRepositoryJdbc {

    @Autowired
    @Qualifier("globalAccessJdbcTemplate")
    protected JdbcTemplate jdbcTemplate;

    public List<Employee> filterByUsername(String name) {
        return jdbcTemplate.query("select * from employee where name ='" + name + "'", new EmployeeRowMapper());
    }

    public Employee login(String name, String password) {

        String sql = "select * from employee where name ='" + name + "' and password ='" + password + "'";
        List<Employee> employees = jdbcTemplate.query(sql, new EmployeeRowMapper());

        if (employees.size() == 1) {
            return employees.get(0);
        } else {
            return null;
        }
    }


}
