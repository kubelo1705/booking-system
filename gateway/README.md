# Gateway Service

This is the API Gateway for the Booking System microservices architecture. It routes requests to the appropriate backend services (room, user, booking) using Spring Cloud Gateway.

## Routes
- `/rooms/**` → Room Service
- `/users/**` → User Service
- `/bookings/**` → Booking Service

## How to Run

### Standalone
1. Build the project:
   ```sh
   mvn clean package
   ```
2. Run the application:
   ```sh
   java -jar target/gateway-0.0.1-SNAPSHOT.jar
   ```

### With Docker Compose
The service is included in the root `docker-compose.yml` and will start automatically with the rest of the system:
```sh
docker-compose up --build
```

## Configuration
Routes are configured in `src/main/resources/application.yml`. 