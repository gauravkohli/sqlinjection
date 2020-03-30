package com.okta.blog.sqlinjection.repository.jpa;

import com.okta.blog.sqlinjection.domain.Employee;
import org.springframework.stereotype.Repository;

import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class EmployeeRepositoryJpaSafe extends EmployeeRepositoryJpa {

    @Override
    public List<Employee> filterByUsername(String name) {
        String jql = "from Employee where name = :name";
        TypedQuery<Employee> q = em.createQuery(jql, Employee.class).setParameter("name", name);
        return q.getResultList();
    }

    @Override
    public List<Employee> filterByUsernameStoredProcedure(String name) {
        StoredProcedureQuery filterByUsernameProcedure = em.createNamedStoredProcedureQuery("filterByUsernameStoredProcedureSafe");
        filterByUsernameProcedure.setParameter("p_name", name);
        return filterByUsernameProcedure.getResultList();
    }


}
