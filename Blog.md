# SQL Injection in Java: Practices to Avoid

### Overview
In this tutorial we would be going through the topic of SQL injection in Java and I will try to explain to my fellow developers what SQL Injection is all about. Also we would go through a sample Spring Boot based application which already has SQL Injection vulnerabilities and we will see how easily we can fix those. 

**Prerequisites**

* [Java 8](https://openjdk.java.net/install/)
* [Httpie](https://httpie.org/) - Command line HTTP client
* [Docker Desktop](https://www.docker.com/get-started) - For running Mysql Docker

### Demo Application Setup
You can download the demo application from [Github](https://github.com/gauravkohli/sqlinjection).

Since we need to show vulnerabilities with Stored procedure and 'Principle of least privileges' we are using a Mysql instance running in Docker. This Mysql docker instance already has some vulnerable Store Procedure already created.

Build Docker image for custom Mysql instance

    cd docker/sqlinjection
    docker build -t sqlinjection_demo:latest

Run above created Mysql image 

    docker run -p 3306:3306 --name local-mysql -e MYSQL_ROOT_PASSWORD=11asd097asd -d sqlinjection_demo:latest

Run the demo application

    cd ../../
    ./mvnw clean install
    ./mvnw spring-boot:run
    
### Understanding SQL Injection
SQL Injection is a type of attack which exposes vulnerability in the Database layer of a typical web application. In this attack, the hacker tries to execute SQL queries or statements which the application won't be executing if working normally. 

These queries then allows hacker to get sensitive application data like username/passwords or it could be financial data also. Sometimes the hackers uses this vulnerability to get more information about the underlying database server like which version they are running, which might help pave the way for advanced Database specific SQL injection attacks.

An very important thing to remember here is that these vulnerability are not system or underlying database issues directly, but more of the system design and falls under programmer's responsibility to validate the user input and SQL query/statements for these flaws.  


### How SQL queries are Vulnerable 

So let's see some code which has SQL Injection vulnerable code and how can a hacker exploit that. In this tutorial I would try to show you how vulnerable code would look like while using both JdbcTemplate and JPA. 

Let's start with the basic Datasource library: Here we have a code which is filtering Employees in a organisation:  

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


Same functionality when using JdbcTemplate

    jdbcTemplate.query("select * from employee where name ='" + name + "'",
        (rs, rowNum) ->
            new Employee(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("password"),
                    Role.valueOf(rs.getString("role"))
            )
    );

And when using JPA

    String jql = "from Employee where name = '" + name + "'";
    TypedQuery<Employee> q = em.createQuery(jql, Employee.class);
    return q.getResultList();

So as you can in the above snippets the SQL query which is executed is dynamically generated and does string concatenation to use the parameter value passed to the function. And that is exactly the reason why these code are vulnerable.

Now we would try to use the demo application to execute these methods and then we can see how those can be exploited by hackers

This is how you would normally call the filterUserJdbcUnSafe endpoint to filter employees by name
    
    http http://localhost:8080/filterUserJdbcUnSafe name=="Bilbo" 
    
This is how hacker might use this endpoint

    http http://localhost:8080/filterUserJdbcUnSafe name=="Bilbo' or '1' = '1"  

and this would generate a sql query like "select * from employee where name ='Bilbo' or '1' = '1'" and basically return you list of all the employees instead and here is the response:

    HTTP/1.1 200 
    Connection: keep-alive
    Content-Type: application/json
    Date: Sun, 29 Mar 2020 22:19:44 GMT
    Keep-Alive: timeout=60
    Transfer-Encoding: chunked
    
    [
        {
            "id": 1,
            "name": "Bilbo",
            "role": "MANAGER"
        },
        {
            "id": 2,
            "name": "Frodo",
            "role": "STAFF"
        }
    ]



### How Stored Procedure are Vulnerable
Now you might think, that maybe it's better to use Stored Procedure instead as those won't have these issues. But you are wrong, as I said earlier it's not a Database system, but programmers fault and you can write flawed code using Stored Procedures as well. 

If you end up using EXECUTE statement to execute a dynamic SQL query where the input was not sanitized, you are exposed to SQL injection vulnerability

Let's have a look at a simple Stored procedure doing the same: Filtering list of employees based on name

    DELIMITER $$
    CREATE PROCEDURE `filterByUsernameStoredProcedureUnSafe` (in p_name varchar(1000))
    begin
    SET @SQLString = CONCAT("Select * from employee where name = '", p_name, "'");
    PREPARE test FROM @SQLString;
    EXECUTE test;
    end $$
    DELIMITER ;
    
In the stored procedure above the input variable p_name is concatenated to create the SQL statement to be executed and the hacker can use any of the possible SQL injection techniques to exploit it. 

And this is how you call it using JPA 

    StoredProcedureQuery filterByUsernameProcedure = em.createNamedStoredProcedureQuery("filterByUsernameStoredProcedureUnSafe");
    filterByUsernameProcedure.setParameter("p_name", name);
    return filterByUsernameProcedure.getResultList();
    
And same exploit happens when called with hacky data input 

    http http://localhost:8080/filterUserJpaStoredProcedureUnSafe name=="Bilbo' or '1' = '1"

### Different types of SQL Injection Techniques
You can easily divide the SQL Injection attacks based on different techniques used to exploit the vulnerability. Here we will talk about some of the most common ones.

#### 1. Boolean based
This is the most common technique used while doing SQL Injection attacks wherein you try to get more information from the application, then it was intended to return.

So for instance there is a web endpoint to filter the list of employees in the company and we use that to inject SQL to get list of all employees in the system.

Find all the employees who have name "Bilbo"

    http http://localhost:8080/filterUserJdbcUnSafe name=="Bilbo"
    
And this could be used by hacker to get list of all the employees by adding a boolean clause "or '1' = '1"" like this:

    http http://localhost:8080/filterUserJdbcUnSafe name=="Bilbo' or '1' = '1"

#### 2. Union
In this the hacker adds a UNION SQL clause to the SELECT query which is vulnerable and the response would then contain data from other table if specified. 

    http http://localhost:8080/filterUserGlobalAccessUnSafe name=="Bilbo' union all select 1, concat(review,'-----',rating),review,  'STAFF'  from management.employee_review where '1'='1"
    HTTP/1.1 200 
    Connection: keep-alive
    Content-Type: application/json
    Date: Mon, 30 Mar 2020 14:53:34 GMT
    Keep-Alive: timeout=60
    Transfer-Encoding: chunked
    
    [
        {
            "id": 1,
            "name": "Bilbo",
            "role": "MANAGER"
        },
        {
            "id": 1,
            "name": "Good performance-----5",
            "role": "STAFF"
        },
        {
            "id": 1,
            "name": "okay performance-----3",
            "role": "STAFF"
        }
    ]


For this attack to work you need to know how many columns are been returned by normal query and what are the data types, otherwise the query would fail since column count doesn't match. And the trick to know other table names and column is to do use the 'Inference/Blind' technique and get details from "INFORMATION_SCHEMA" database.

#### 3. Inference/Blind
Inference SQL injection are those where the web responses doesn't give you any confidential data, but their success or failure is used to figure out details from the system. 

So let's assume there's an endpoint for login into a web application, which returns user details if success otherwise nothing and the SQL query would be like this:

    select * from employee where name ='Bilbo' and password ='secret'

But if the SQL query is vulnerable to exploit then the hacker can use the password field to inject SQL CASE statements and use that to get details from other databases/table/columns, like this:

    select * from employee where name ='Bilbo' and password ='secret' and (select CASE WHEN (substring(authentication_string,1,1) = '$' ) THEN true ELSE false END from  mysql.user where User = 'empdb_user') or ''
    
And this would tell hacker that the first character of empdb_user's password is '$'. 

Let's see this in action

    http http://localhost:8080/loginJdbcUnSafe name=="Bilbo" password=="secret' and (select CASE WHEN (substring(authentication_string,1,1) = '$' ) THEN true ELSE false END from  mysql.user where User = 'empdb_user') or '"

and with the help of substring and comparison operator like '>', '<', '=', '!=' and binary search, hacker can easily guess all the characters in the password column. Hacker can easily use this technique in similar fashion to get data out of tables in INFORMATION_SCHEMA database. It's time consuming but hack can use tools like [sqlmap](http://sqlmap.org/) to speed up the attack.

#### 4. Time based (Slowloris DDoS)
In this type of Injection, the hacker tries to introduce a delay function like sleep(time) or Benchmark(count,expr) in the SQL query and as a result the web request would take longer than usual time to respond. 

Normally the web applications have a pool of database connection open and the hacker can use this technique to exhaust all of them pretty quickly. Once all the database connections are queued or sleeping because of delay, the database server would stop accepting new connections. 

    http http://localhost:8080/filterUserJdbcUnSafe name=="Bilbo' + sleep(10)+'"

So this technique is very unique, since using this you are not getting any data out of the application, but using it to do a DDoS attack on the application.

### How to Prevent SQL Injection Vulnerability
Hopefully by now you have a decent idea about what SQL Injection is and how hackers can exploit those to get into database systems and get confidential information. So let's go through some of the available effective techniques to stop them.  

##### 1. Parameterized Queries
Since the main reason for SQL Injection is dynamic generated SQL queries, one of the solution to avoid it is by using PreparedStatement both while using JdbcTemplate or JPA to run your queries along with placeholder or named parameters and never use dynamic sql

JdbcTemplate Prepared Statement example

    PreparedStatementCreator statementCreator = new PreparedStatementCreator() {
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(SELECT_SQL);
            ps.setString(1, name);
            return ps;
        }
    };
    jdbcTemplate.query(statementCreator, (rs, rowNum) ->
        new Employee(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role"))
        ));
    
or in case of JPA above would look like

    String jql = "from Employee where name = :name";
    TypedQuery<Employee> q = em.createQuery(jql, Employee.class).setParameter("name", name);
    return q.getResultList();

And now if we call either of the above implementation with the hackers input:

    http http://localhost:8080/filterUserJdbcSafe name=="Bilbo' or '1' = '1"
    http http://localhost:8080/filterUserJpaSafe name=="Bilbo' or '1' = '1"

the response we receive is a empty list, since the above request created a sql query list "select * from employee where name ="Bilbo' or '1' = '1" " and that would match no employee.

    HTTP/1.1 200 
    Connection: keep-alive
    Content-Type: application/json
    Date: Sun, 29 Mar 2020 22:53:09 GMT
    Keep-Alive: timeout=60
    Transfer-Encoding: chunked
    
    []
    

##### 2. Stored procedure
In case of Stored Procedure it's even more simpler and you need not use EXECUTE with dynamic queries and instead use the named parameter directly in the sql statement.

    DELIMITER $$
    CREATE PROCEDURE `FIND_EMPLOYEE`(in p_name varchar(1000))
    begin
    SELECT * from employee WHERE name = p_name;
    end $$
    DELIMITER ;
    
Let's try this out in demo application
    
    http http://localhost:8080/filterUserJpaStoredProcedureSafe name=="Bilbo' or '1' = '1" 

And this would return a empty list, since now the filter param matches none of the employee's name.


##### 3. Principle of least privilege
Suppose you have multiple databases running on your database server and your vulnerable web application is running with an database account which has access to all these databases. If there's a SQL Injection attack, then the hacker would be able to access all these database and data across them. 

And in these cases, it's always recommended to configure multiple datasources with different database accounts, then you atleast restrict the scope of the attack and minimize the data hacker has access do.

Let's see how you can configure multiple datasources in JdbcTemplate


This will configure a Datasource which has access to all the databases running on the server

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

Let's see how this can increase the hack scope if SQL Injection vulnerability is exploited. 

    http http://localhost:8080/filterUserGlobalAccessUnSafe  name=="Bilbo' union all select 1,concat(review,'----',rating),'something','STAFF' from management.employee_review where '1'='1"
    
filterUserGlobalAccessUnSafe is same function to filter Employees by Name but Datasource defined for the JdbcTemplate has access to all the databases ( including 'management' database ). So because of that, the hacker was able to inject code to fetch data from other databases as well. 

And the response of above request is below and you can see that apart from user 'Bilbo' we also got data from  management.employee_review  table.

    HTTP/1.1 200 
    Connection: keep-alive
    Content-Type: application/json
    Date: Sun, 29 Mar 2020 23:10:41 GMT
    Keep-Alive: timeout=60
    Transfer-Encoding: chunked
    
    [
        {
            "id": 1,
            "name": "Bilbo",
            "role": "MANAGER"
        },
        {
            "id": 1,
            "name": "Good performance----5",
            "role": "STAFF"
        },
        {
            "id": 1,
            "name": "okay performance----3",
            "role": "STAFF"
        }
    ]
    

To fix this issue what we need is to define a separate JdbcTemplate for each confidential database. Let's configure a Datasource which has access only to employees database and see what happens.

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

Now if you run the same exploit with JdbcTemplate configured to access only employees database you would get an error like before

    http http://localhost:8080/filterUserJdbcUnSafe  name=="Bilbo' union all select 1,concat(review,'----',rating),'something','STAFF' from management.employee_review where '1'='1"
    HTTP/1.1 500 
    Connection: close
    Content-Type: application/json
    Date: Sun, 29 Mar 2020 23:16:36 GMT
    Transfer-Encoding: chunked
    
    {
        "error": "Internal Server Error",
        "message": "StatementCallback; bad SQL grammar [select * from employee where name ='Bilbo' union all select 1,concat(review,'----',rating),'something','STAFF' from management.employee_review where '1'='1']; nested exception is java.sql.SQLSyntaxErrorException: SELECT command denied to user 'empdb_user'@'172.17.0.1' for table 'employee_review'",
        "path": "/filterUserJdbcUnSafe",
        "status": 500,
        "timestamp": "2020-03-29T23:16:36.345+0000"
    }



### SQL Injection Testing Tools
In case you have a legacy application or in general you want to have your application tested against SQL Injection, you can always use these open source free tools to test your application out.

* [SQLMap](https://github.com/sqlmapproject/sqlmap)
* [SQLninja](http://sqlninja.sourceforge.net/)
* [Safe3 SQL Injector](https://sourceforge.net/projects/safe3si/)
* [SQLSus](http://sqlsus.sourceforge.net/)
* [Mole](https://sourceforge.net/projects/themole/files/)