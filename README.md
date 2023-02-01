# webapp

Prerequisites for building and deploying your application locally.

1. Install Java 8. 
    *Link:* https://www.oracle.com/java/technologies/downloads/

2. Install Maven Version 3.6.3
   *Link:* https://maven.apache.org/install.html

3. Install Postgres
   *Link:* https://www.postgresql.org/download/ 


Build and Deploy instructions for the web application. 

Inside the Spring Boot project directory "UserWebApp", run the below commands:
1. mvn clean install 
2. mvn spring-boot:run -Dspring-boot.run.arguments="--HOST_NAME=localhost --DB_PORT=5432 --API_PORT=port_no --DB_NAME=databaseName  --DB_USERNAME=roleName --DB_PASSWORD=password"


REST API Endpoints (Change according to Port):
1. Test Healthy Endpoint
   http://localhost:8080/healthz

2. Create a User
   http://localhost:8080/v1/user

3. Get User Details
   http://localhost:8080/v1/user/{username}

4. Update User
   http://localhost:8080/v1/user/{username}