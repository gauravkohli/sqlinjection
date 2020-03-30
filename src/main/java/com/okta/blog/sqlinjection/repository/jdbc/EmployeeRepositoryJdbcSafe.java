package com.okta.blog.sqlinjection.repository.jdbc;

import com.okta.blog.sqlinjection.domain.Employee;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EmployeeRepositoryJdbcSafe extends EmployeeRepositoryJdbc {

    @Override
    public List<Employee> filterByUsername(String name) {
        final String SELECT_SQL = "select * from employee where name = ?";

        PreparedStatementCreator statementCreator = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(SELECT_SQL);
                ps.setString(1, name);
                return ps;
            }
        };

        return jdbcTemplate.query(statementCreator, new EmployeeRowMapper());
    }

}
