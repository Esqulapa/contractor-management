# contractor-management

## What I Learned

During the development of this project, I learned:

- Java
- Spring Boot framework
- Database management
- Docker for containerization
- Flyway for database schema versioning and migration
- REST API development
- Postman

## How to Run Locally

To run this application, follow these steps:

###  Clone this repository to your local machine:
  ```bash
   git clone https://github.com/Esqulapa/contractor-management.git
   ```

###  When opening project:

click at  

![img.png](data/img.png)

and let gradle build project

###  Navigate to the `/docker` directory:
  ```bash
cd contractor-management/docker  
   ```

and do 
  ```bash
docker-compose up -d   
   ```

### You can start the application:

  ```bash
./gradlew bootRun   
   ```


### To perform requests, I recommend using Postman. In the `/data` directory, there is a collection of requests to import. 
```bash
data/contractor-management.postman_collection.json
```
###  The application should now be accessible at 

[http://localhost:8080](http://localhost:8080).

### pgAdmin should now be accessible at 

http://localhost:5050/browser/
   






