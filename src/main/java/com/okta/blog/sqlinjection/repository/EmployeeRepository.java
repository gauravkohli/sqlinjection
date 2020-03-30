package com.okta.blog.sqlinjection.repository;

import com.okta.blog.sqlinjection.domain.Employee;
import com.okta.blog.sqlinjection.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EmployeeRepository {

    @Autowired
    private DataSource dataSource;

    public List<Employee> filterByUsername(String name) {

        String sql = "select * from employee where name ='" + name + "'";

        try (Connection c = dataSource.getConnection();
             ResultSet rs = c.createStatement().executeQuery(sql)) {
            List<Employee> employees = new ArrayList<>();
            while (rs.next()) {
                employees.add(new Employee(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                ));
            }
            return employees;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

    }
}
