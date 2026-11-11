# StockPilot — Inventory Management System

**Smarter Inventory. Better Control.**

StockPilot is a modern web-based inventory management system built with Java 21 LTS and Spring Boot. It provides centralized inventory control, stock monitoring, item lending and return management, user administration, and transaction reporting.

Designed with a modular architecture, PostgreSQL database, and Docker deployment support, StockPilot is suitable for local development and self-hosted environments.

## Tech Stack

| Component | Technology |
|---|---|
| Programming Language | Java 21 LTS |
| Backend Framework | Spring Boot 3.5.6 |
| Security | Spring Security |
| Data Persistence | Spring Data JPA & Hibernate |
| Database | PostgreSQL 16+ |
| Database Migration | Flyway |
| Frontend | Thymeleaf, HTML5, CSS3, Vanilla JavaScript |
| Build Tool | Apache Maven 3.9+ |
| Password Hashing | BCrypt |
| Containerization | Docker & Docker Compose |
| Web Server | Embedded Apache Tomcat |
| Operating System | Ubuntu Server 26.04 LTS / Windows |

## Features

### Authentication & Authorization
- Session-based authentication.
- Secure password hashing using BCrypt.
- Role-based access control (ADMIN and STAFF).
- CSRF protection.
- Login, logout, and session management.
- Protected application routes.

### Dashboard
- Total inventory statistics.
- Total categories and users.
- Available stock monitoring.
- Borrowed inventory statistics.
- Recent transaction activity.
- Low-stock notifications.
- Inventory activity overview.

### Inventory Management
- Create, read, update, and delete inventory items.
- Manage item categories.
- Search and filter inventory.
- Stock quantity management.
- Item descriptions and storage locations.
- Input validation and data integrity.

### Borrowing & Returns
- Create borrowing transactions.
- Approve or reject borrowing requests.
- Manage borrowing and return status.
- Automatic stock adjustments.
- Insufficient stock validation.
- Transaction history.
- Overdue transaction monitoring.

### User Management
- Administrator and staff accounts.
- User creation and editing.
- Role management.
- Account status management.
- User administration restricted to administrators.

### Reports & Activity
- Transaction history.
- Stock movement records.
- Date-based filtering.
- User-based filtering.
- CSV report export.

## Project Architecture

StockPilot uses a layered application architecture to separate business logic, database operations, security, and presentation.

```text
stockpilot-java/
│
├── .mvn/
│   └── wrapper/
│
├── src/
│   ├── main/
│   │   ├── java/com/inventorymanagement/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── scheduler/
│   │   │   ├── security/
│   │   │   └── service/
│   │   │
│   │   └── resources/
│   │       ├── db/migration/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       ├── templates/
│   │       └── application.properties
│   │
│   └── test/
│       ├── java/com/inventorymanagement/
│       └── resources/
│
├── deploy/
│
├── loadtest/
│
├── .dockerignore
├── .env.example
├── .gitignore
├── Dockerfile
├── compose.yaml
├── pom.xml
└── README.md
```

## Requirements

Before running StockPilot locally, make sure the following software is installed:

- Java Development Kit (JDK) 21 LTS.
- Apache Maven 3.9 or newer.
- PostgreSQL 16 or newer.
- Git (optional, for source control).
- Docker Engine and Docker Compose (for container deployment).

## Environment Configuration

StockPilot uses environment variables to manage database credentials, application settings, and administrator bootstrap configuration.

**Important security notice:**

Never commit your actual `.env` file, database passwords, administrator passwords, or other private credentials to GitHub.

The repository includes `.env.example` as a configuration template.

### Setup

Create your local environment file from the template.

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Linux / Ubuntu:

```bash
cp .env.example .env
chmod 600 .env
```

Edit the `.env` file and replace all placeholder values with your own configuration.

Example template:

```properties
# Database Configuration
DB_NAME=inventory
DB_USERNAME=inventory_app
DB_PASSWORD=CHANGE_ME_TO_A_STRONG_PASSWORD

# Application Configuration
APP_PORT=8080

# Administrator Bootstrap
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@inventory.local
ADMIN_PASSWORD=CHANGE_ME_TO_A_UNIQUE_PASSWORD
```

These values are examples, not production credentials. Make sure the variable names match the application's `compose.yaml` and configuration files.

Administrator bootstrap credentials should only be used during initial account creation. Never use a hardcoded or publicly documented administrator password.

## Running Locally

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/stockpilot-java.git

cd stockpilot-java
```

### 2. Prepare PostgreSQL

Create a database and a dedicated application user.

Example SQL:

```sql
CREATE ROLE inventory_app
WITH LOGIN PASSWORD 'YOUR_LOCAL_DATABASE_PASSWORD';

CREATE DATABASE inventory
WITH OWNER inventory_app;
```

### 3. Configure the database connection

For Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/inventory"

$env:SPRING_DATASOURCE_USERNAME = "inventory_app"

$env:SPRING_DATASOURCE_PASSWORD = "YOUR_LOCAL_DATABASE_PASSWORD"
```

Make sure these credentials match the PostgreSQL role and database you created.

### 4. Run automated tests

```bash
mvn clean test
```

### 5. Build the application

```bash
mvn clean package
```

The executable JAR will be generated inside:

```text
target/inventory-management-1.0.0.jar
```

### 6. Start the application

```bash
java -jar target/inventory-management-1.0.0.jar
```

Alternatively, use the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

### 7. Access the application

Open your browser:

```text
http://localhost:8080
```

The login page is expected at:

```text
http://localhost:8080/login
```

Database migrations are managed through Flyway during application startup.

## Docker Deployment

StockPilot supports containerized deployment using Docker Compose.

### 1. Prepare the server

Install Docker Engine and the Docker Compose plugin using the official documentation:

https://docs.docker.com/engine/install/ubuntu/

Verify the installation:

```bash
docker --version

docker compose version
```

### 2. Clone the project

```bash
sudo mkdir -p /opt/stockpilot

sudo chown "$USER":"$USER" /opt/stockpilot

git clone https://github.com/YOUR_USERNAME/stockpilot-java.git /opt/stockpilot

cd /opt/stockpilot
```

### 3. Configure environment variables

```bash
cp .env.example .env

chmod 600 .env

nano .env
```

Set strong and unique credentials before starting the application.

### 4. Validate Docker Compose configuration

```bash
docker compose config --quiet
```

### 5. Build and start the containers

```bash
docker compose up -d --build
```

### 6. Monitor container status

```bash
docker compose ps
```

### 7. View application logs

```bash
docker compose logs -f app
```

### 8. Access StockPilot

```text
http://SERVER_IP:8080
```

Replace `SERVER_IP` with your Ubuntu Home Server's local IP address.

## Docker Management

Common commands for application maintenance:

```bash
# Restart all services
docker compose restart

# Restart the Java application
docker compose restart app

# Stop containers without deleting persistent data
docker compose down

# Rebuild and update the application
docker compose up -d --build

# Check container resource usage
docker stats

# View database logs
docker compose logs -f db

# View application logs
docker compose logs -f app
```

**Warning:** Avoid using `docker compose down -v` unless you intentionally want to delete persistent volumes, including database data.

## Database Backup & Restore

### Backup PostgreSQL

```bash
docker compose exec -T db sh -c \
'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' \
> "backup_$(date +%F).sql"
```

### Restore PostgreSQL

```bash
cat backup_2026-11-12.sql | docker compose exec -T db sh -c \
'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Replace the backup filename with the actual file you want to restore.

Store backups in a secure location and periodically verify that they can be restored.

## Testing & Quality Assurance

StockPilot includes automated testing for important application components.

Test coverage includes:

- Authentication and authorization.
- Protected routes.
- Inventory business logic.
- Stock validation.
- Borrowing transactions.
- Return processing.
- Application security.

Run the test suite:

```bash
mvn clean test
```

Maven test reports are available under:

```text
target/surefire-reports/
```

Record the actual number of passed, failed, and skipped tests from the latest test execution.

## Security Guidelines

Security is an important part of StockPilot.

- Never publish real `.env` files.
- Never hardcode database credentials.
- Never commit production passwords or API secrets.
- Use BCrypt for password hashing.
- Keep CSRF protection enabled.
- Apply role-based access control.
- Use unique and strong administrator credentials.
- Restrict public access to PostgreSQL.
- Keep Docker images and application dependencies updated.
- Back up database data regularly.

## Roadmap

Potential future improvements:

- [ ] Advanced inventory analytics.
- [ ] Barcode and QR code integration.
- [ ] PDF report generation.
- [ ] Email notifications.
- [ ] Advanced audit log viewer.
- [ ] REST API integration.
- [ ] Automated backup scheduling.
- [ ] Performance monitoring with Prometheus and Grafana.
- [ ] Load testing with k6.
- [ ] Security testing with OWASP ZAP.

## License

This project is currently maintained as a personal development and learning project.

A license will be added when the distribution terms are finalized.

---

**StockPilot**

*Smarter Inventory. Better Control.*
