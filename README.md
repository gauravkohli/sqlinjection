# SQL Injection in Java: Practices to Avoid

### Requirements
* Docker desktop
* Java 8


### Start Mysql in Docker
Since we need to show vulnerabilities with Stored procedure and 'Principle of least privileges' we are using a Mysql
instance running in Docker

Build Docker image 

    cd docker/sqlinjection
    docker build -t sqlinjection_demo:latest

Run docker 

    docker run -p 3306:3306 --name local-mysql -e MYSQL_ROOT_PASSWORD=11asd097asd -d sqlinjection_demo:latest

### Run the application

    cd ../../
    ./mvnw spring-boot:run