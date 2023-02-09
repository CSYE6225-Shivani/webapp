# webapp

**Tech-Stack used:**

**Back-end technology:** Spring Boot, Hibernate & Spring Data JPA

**Database:** Postgres

---------------------------------------------------------------

**Prerequisites for building and deploying your application locally.**

1. Install Java 8. 
   
   ***Link:*** https://www.oracle.com/java/technologies/downloads/

2. Install Maven Version 3.6.3
   
   ***Link**:* https://maven.apache.org/install.html

3. Install Postgres
   
   ***Link**:* https://www.postgresql.org/download/ 

4. Install Postman or any REST API testing tool.   
  ***Link**:* https://www.postman.com/downloads/

-------
**Build and Deploy instructions for the web application**
1. Clone GitHub repository to Desktop:

   git clone git@github.com:CSYE6225-Shivani/webapp.git


2. Go inside the project directory:
   cd/Desktop/webapp/UserWebApp


3. Run command - mvn clean install

   
4. mvn spring-boot:run -Dspring-boot.run.arguments="--HOST_NAME=localhost --DB_PORT=5432 --API_PORT=8080 --DB_NAME=userdatabase  --DB_USERNAME=shivanichavan --DB_PASSWORD=password"


5. Open POSTMAN and test below endpoints

-------------
REST API Endpoints (Change according to Port):
1. Test Healthy Endpoint

   http://localhost:8080/healthz


2. Create a User

   http://localhost:8080/v1/user


3. Get User Details

   http://localhost:8080/v1/user/{userId}


4. Update User

   http://localhost:8080/v1/user/{userId}


5. Create Product

   http://localhost:8080/v1/product


6. Get Product Details

   http://localhost:8080/v1/product/{productId}


7. Delete product

   http://localhost:8080/v1/product/{productId}


8. PUT - Update product

   http://localhost:8080/v1/product/{productId}


9. PATCH - Update product

   http://localhost:8080/v1/product/{productId}