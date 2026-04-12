# RedLedger 💰

**A deliberately vulnerable fintech REST API designed to demonstrate Application Security (AppSec) concepts.**

> ⚠️ **WARNING**: This application contains intentional security vulnerabilities for educational purposes. **DO NOT deploy to production or expose to the public internet.**

---

### Project Overview

RedLedger is a simulated banking/fintech API built with Spring Boot that demonstrates common web application vulnerabilities from the OWASP Top 10 and API Security Top 10. It serves as a portfolio project showcasing:

- **Vulnerability identification** using SAST/SCA tools
- **Threat modeling** with the STRIDE framework
- **Manual exploitation** with Burp Suite
- **DevSecOps pipeline** integration
- **Security remediation** patterns

### Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.4 |
| Security | Spring Security 6 + JJWT |
| Database | H2 (in-memory) |
| Build Tool | Gradle 9.4.1 (Kotlin DSL) |
| Container | Docker |

### Quick Start

#### Prerequisites
- Java 17+
- Gradle 9.4.1+ (or use included wrapper)
- Docker (optional)

#### Run Locally
```bash
# Clone the repository
git clone <repository-url>
cd redledger

# Build and run
./gradlew bootRun

# Application starts at http://localhost:8080
# H2 Console at http://localhost:8080/h2-console
```

#### Run with Docker
```bash
docker-compose up --build
```

### Test Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ROLE_ADMIN |
| jsmith | password123 | ROLE_USER |
| jdoe | password456 | ROLE_USER |

### API Endpoints

#### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and receive JWT |

#### Accounts
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/accounts` | List user's accounts |
| GET | `/api/accounts/{id}` | Get account details |
| GET | `/api/accounts/{id}/balance` | Get account balance |

#### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions/transfer` | Create transfer |
| GET | `/api/transactions/account/{id}` | List account transactions |
| GET | `/api/transactions/{id}` | Get transaction details |

#### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| GET | `/api/admin/users/{id}` | Get user details |
| DELETE | `/api/admin/users/{id}` | Delete user |
| PUT | `/api/admin/users/{id}/role` | Update user role |

### Known Vulnerabilities

> 🚧 **Placeholder** — Vulnerabilities will be documented here after implementation.

| # | Vulnerability | OWASP Category | Status |
|---|--------------|----------------|--------|
| 1 | TBD | TBD | Planned |
| 2 | TBD | TBD | Planned |
| 3 | TBD | TBD | Planned |

### STRIDE Threat Model

A comprehensive STRIDE threat model for RedLedger is documented in [THREAT_MODEL.md](THREAT_MODEL.md).

The threat model covers:
- **S**poofing — Authentication bypass scenarios
- **T**ampering — Data modification attacks
- **R**epudiation — Logging and audit trail gaps
- **I**nformation Disclosure — Sensitive data exposure
- **D**enial of Service — Resource exhaustion vectors
- **E**levation of Privilege — Authorization bypass scenarios

### Project Structure

```
redledger/
├── src/main/java/com/redledger/
│   ├── config/          # Security and web configuration
│   ├── controller/      # REST API controllers
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # JPA entities
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JWT and security filters
│   └── service/         # Business logic services
├── src/main/resources/
│   ├── application.properties
│   ├── data.sql         # Test data
│   └── logback.xml      # Logging configuration
├── docs/evidence/       # Burp Suite screenshots
├── .github/workflows/   # CI/CD pipeline
├── THREAT_MODEL.md
├── SECURITY.md
└── README.md
```

### License

This project is for educational purposes only.
