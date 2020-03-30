CREATE USER 'globalaccess_user'@'%' IDENTIFIED BY '789as8asjk';
GRANT ALL ON *.* TO 'globalaccess_user'@'%';

CREATE USER 'empdb_user'@'%' IDENTIFIED BY '87a98asjhas8';
GRANT ALL ON employees.* TO 'empdb_user'@'%';

CREATE DATABASE employees;
USE employees;

CREATE TABLE `employee` (
  `id` bigint NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

DELIMITER $$
CREATE PROCEDURE `filterByUsernameStoredProcedureUnSafe` (in p_name varchar(1000))
begin
SET @SQLString = CONCAT("Select * from employee where name = '", p_name, "'");
PREPARE test FROM @SQLString;
EXECUTE test;
end $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE `filterByUsernameStoredProcedureSafe`(in p_name varchar(1000))
begin
SELECT * from employee WHERE name = p_name;
end $$
DELIMITER ;


CREATE DATABASE management;
USE management;

CREATE TABLE `employee_review` (
  `id` bigint NOT NULL,
  `employee_id` bigint NOT NULL,
  `review` varchar(2148) NOT NULL,
  `rating` enum ('1','2','3','4','5') NOT NULL,
  PRIMARY KEY (`id`)
);

insert into employee_review values (1,1,"Good performance", 5);
insert into employee_review values (2,2,"okay performance", 3);