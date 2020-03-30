package com.okta.blog.sqlinjection;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@org.springframework.context.annotation.Configuration
public class Configuration {

    public static final String JPA_EMPDB_PERSITENCE_UNIT = "JPA_EMPDB_PERSITENCE_UNIT";

    @Autowired
    private Environment env;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource-empdb")
    public DataSourceProperties empdbDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "empdbDataSource")
    @Primary
    public DataSource empdbDataSource() {
        return empdbDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();

    }

    @Bean("empdbJdbcTemplate")
    @Primary
    public JdbcTemplate empdbJdbcTemplate(@Autowired @Qualifier("empdbDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConfigurationProperties("spring.datasource-globalaccess")
    public DataSourceProperties globalAccessDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "globalAccessDataSource")
    public DataSource globalAccessDataSource() {
        return globalAccessDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean("globalAccessJdbcTemplate")
    public JdbcTemplate globalAccessJdbcTemplate(@Autowired @Qualifier("globalAccessDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean empdbEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(empdbDataSource());

        em.setPackagesToScan(new String[]{"com.okta.blog.sqlinjection"});
        em.setPersistenceUnitName(JPA_EMPDB_PERSITENCE_UNIT);

        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        em.setJpaPropertyMap(properties);

        em.afterPropertiesSet();
        return em;
    }

    @Bean
    public PlatformTransactionManager empdbTransactionManager() {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(empdbEntityManager().getObject());
        return transactionManager;
    }

}