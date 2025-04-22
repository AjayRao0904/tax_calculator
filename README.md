# Accounting Platform

A comprehensive accounting and tax management platform built with Spring Boot, designed to help businesses manage their financial operations, calculate taxes, and generate reports efficiently.

## Features

### Core Functionality
- **Tax Calculation**
  - Corporate tax computation
  - TDS (Tax Deducted at Source) management
  - Advance tax tracking
  - Depreciation calculations
  - Loss management

### Financial Management
- **Expense Tracking**
  - Multiple expense categories
  - Receipt management
  - Payment mode tracking
  - Expense reports

- **Revenue Management**
  - Income tracking
  - Revenue categorization
  - Financial year management
  - Revenue reports

### Asset Management
- **Asset Tracking**
  - Asset registration
  - Depreciation calculation
  - Asset categorization
  - Asset history

### Reporting
- **Tax Reports**
  - PDF report generation
  - Detailed tax calculations
  - Tax liability statements
  - Historical reports

### User Management
- **Multi-User Support**
  - Role-based access control
  - Company management
  - User profiles
  - Security features

## Technical Stack

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- Hibernate

### Frontend
- Thymeleaf
- Bootstrap
- JavaScript
- CSS

### Database
- PostgreSQL
- H2 (for development)

### Build Tools
- Maven
- Git

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Node.js and npm (for frontend development)

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/accounting-platform.git
   cd accounting-platform
   ```

2. Configure the database:
   - Create a PostgreSQL database
   - Update `application.properties` with your database credentials

3. Build the application:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Configuration

### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/accounting_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Security Configuration
```properties
spring.security.user.name=admin
spring.security.user.password=admin
```

## Project Structure
src/
├── main/
│ ├── java/
│ │ └── com/
│ │ └── accounting/
│ │ ├── controller/
│ │ ├── service/
│ │ ├── repository/
│ │ ├── entity/
│ │ └── config/
│ └── resources/
│ ├── static/
│ ├── templates/
│ └── application.properties
└── test/
└── java/
└── com/
└── accounting/


## API Documentation

### Tax Calculation API
```http
POST /api/tax/calculate
Content-Type: application/json

{
    "companyId": 1,
    "startDate": "2024-01-01",
    "endDate": "2024-03-31",
    "taxRate": 20
}
```

### Expense Management API
```http
POST /api/expenses
Content-Type: application/json

{
    "amount": 1000.00,
    "date": "2024-03-15",
    "category": "BUSINESS_EXPENSE",
    "description": "Office Supplies"
}
```

## Development

### Running Tests
```bash
mvn test
```

### Code Style
- Follow Google Java Style Guide
- Use 4 spaces for indentation
- Maximum line length: 100 characters

### Git Workflow
1. Create a feature branch
2. Make changes
3. Write tests
4. Submit pull request

## Deployment

### Production Deployment
1. Build the application:
   ```bash
   mvn clean package -Pprod
   ```

2. Deploy the JAR file:
   ```bash
   java -jar target/accounting-platform.jar
   ```

### Docker Deployment
```bash
docker build -t accounting-platform .
docker run -p 8080:8080 accounting-platform
```

## Security

- Spring Security for authentication and authorization
- Password encryption using BCrypt
- CSRF protection
- XSS prevention
- SQL injection prevention

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request


## Roadmap

- [ ] Mobile application
- [ ] Real-time notifications
- [ ] Advanced reporting features
- [ ] Integration with banking APIs
- [ ] Multi-currency support
