# Million Dollar Picks - User Service

## Overview
This service is part of the Million Dollar Picks microservice architecture, responsible for user management functionality. It provides GraphQL API endpoints for creating, retrieving, and updating user information. The service uses JWT-based authentication, connecting to an Auth Service for token validation.

## Architecture

### Technology Stack
- **Java 17** - Core programming language
- **Spring Boot 3.2.3** - Application framework
- **PostgreSQL** - Database for user data storage
- **GraphQL (Netflix DGS)** - API query language
- **Flyway** - Database migration management
- **gRPC** - Communication with Auth Service
- **Docker & Docker Compose** - Containerization and deployment

### Service Responsibilities
- Store and manage user profiles
- Process user registrations
- Track user login activity
- Validate authentication tokens (via Auth Service)

### Entity Model
The service manages User entities with the following attributes:
- `id` (Long): Unique identifier
- `username` (String): Unique username
- `registrationDate` (OffsetDateTime): When the user registered
- `lastLoginDate` (OffsetDateTime): Time of last successful login

## Setup & Installation

### Prerequisites
- JDK 17 or higher
- Docker and Docker Compose
- PostgreSQL (if running locally without Docker)

### Running with Docker
The easiest way to run the service is using Docker Compose:

```bash
# Clone the repository
git clone <repository-url>
cd user-service

# Start with Docker Compose
docker-compose up -d
```

This will start:
- The User Service on port 8080
- PostgreSQL database on port 5432
- PgAdmin interface on port 5050 (email: admin@example.com, password: admin)

### Running Locally
To run the service directly on your machine:

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

Make sure to configure your local PostgreSQL instance and update the application.yml file accordingly.

## API Documentation

### GraphQL API
The service exposes a GraphQL API at `/graphql`. A GraphiQL interface is available at `/graphiql` for interactive exploration.

#### Queries
- `userById(id: ID!)`: Get user by ID
- `userByUsername(username: String!)`: Get user by username

#### Mutations
- `updateLastLogin(username: String!)`: Update user's last login timestamp
- `createOrUpdateUser(username: String!)`: Create a new user or update an existing one

### Example Queries

```graphql
# Get a user by username
query {
  userByUsername(username: "testuser") {
    id
    username
    registrationDate
    lastLoginDate
  }
}

# Create or update a user
mutation {
  createOrUpdateUser(username: "newuser") {
    id
    username
    registrationDate
  }
}
```

## Authentication

The service supports JWT authentication through a filter that validates tokens against the Auth Service.

To make authenticated requests:
1. Obtain a valid JWT token from the Auth Service
2. Include it as a Bearer token in the Authorization header

```
Authorization: Bearer <your-jwt-token>
```

For development purposes, authentication can be disabled by setting the environment variable:
```
AUTHENTICATION_ENABLED=false
```

## Development

### Database Migrations
The service uses Flyway for database schema management. Migration scripts are located in `src/main/resources/db/migration`.

To create a new migration:
1. Create a new SQL file in the migration directory with the format `V{number}__{description}.sql`
2. Add your SQL statements
3. The migration will be automatically applied on service startup

### Configuration
The main configuration files are:
- `application.yml` - Default configuration
- `application-docker.yml` - Docker-specific configuration

## Class Diagram

```
  ┌───────────────────┐        ┌───────────────────┐
  │     UserService   │◄───────┤  UserServiceImpl  │
  └───────────────────┘        └───────┬───────────┘
                                       │
                                       │
  ┌───────────────────┐                │
  │      UserDto      │                │
  └───────────────────┘                │
                                       │
  ┌───────────────────┐        ┌───────▼───────────┐
  │   UserResolver    │───────►│  UserRepository   │
  └───────────────────┘        └───────┬───────────┘
                                       │
                                       │
  ┌───────────────────┐        ┌───────▼───────────┐
  │    UserMapper     │◄───────┤        User       │
  └───────────────────┘        └───────────────────┘
```

## Security Considerations

- Token validation is performed via gRPC calls to the Auth Service
- GraphQL mutations require valid authentication
- JPA Repository methods use parameterized queries to prevent SQL injection
- Error handling is implemented to avoid leaking sensitive information

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| SPRING_DATASOURCE_USERNAME | Database username | postgres |
| SPRING_DATASOURCE_PASSWORD | Database password | postgres |
| SPRING_FLYWAY_URL | Flyway migration DB URL | jdbc:postgresql://postgres:5432/userdb |
| AUTHENTICATION_ENABLED | Enable/disable auth | true |
| DB_HOST | Database hostname | localhost |
| DB_PORT | Database port | 5432 |
| DB_NAME | Database name | userdb |

## License
[Your license information here]