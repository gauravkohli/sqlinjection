package com.okta.blog.sqlinjection.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Data
@Entity
@Table(name = "employee")
@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(name = "filterByUsernameStoredProcedureUnSafe",
                procedureName = "filterByUsernameStoredProcedureUnSafe",
                resultClasses = Employee.class,
                parameters = {
                        @StoredProcedureParameter(
                                name = "p_name",
                                type = String.class,
                                mode = ParameterMode.IN)}),
        @NamedStoredProcedureQuery(name = "filterByUsernameStoredProcedureSafe",
                procedureName = "filterByUsernameStoredProcedureSafe",
                resultClasses = Employee.class,
                parameters = {
                        @StoredProcedureParameter(
                                name = "p_name",
                                type = String.class,
                                mode = ParameterMode.IN)})
})
public class Employee {

    private @Id
    @GeneratedValue
    Long id;
    private String name;

    @ToString.Exclude
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    Employee() {
    }

    public Employee(String name, String password, Role role) {
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public Employee(long id, String name, String password, Role role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }
}