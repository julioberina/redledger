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

| Component | Technology                |
|-----------|---------------------------|
| Language | Java 25                   |
| Framework | Spring Boot 4.0.4         |
| Security | Spring Security 7 + JJWT  |
| Database | H2 (in-memory)            |
| Build Tool | Gradle 9.4.1 (Kotlin DSL) |
| Container | Docker                    |

### Quick Start

#### Prerequisites
- Java 25+
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
| mduser | password123 | ROLE_USER (MD5 path) |

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

> ⚠️ All vulnerabilities are **intentional** and exist for educational demonstration purposes.

| # | Vulnerability | OWASP Category | Endpoint(s) | Status |
|---|--------------|----------------|-------------|--------|
| 1 | IDOR on account endpoints | A1 — Broken Access Control | `GET /api/accounts/{id}`, `GET /api/accounts/{id}/balance` | ✅ Implemented |
| 2 | IDOR on transaction endpoints | A1 — Broken Access Control | `GET /api/transactions/{id}`, `GET /api/transactions/filter` | ✅ Implemented |
| 3 | BFLA on admin endpoints | A1 — Broken Access Control | `GET /api/admin/**`, `PUT /api/admin/users/{id}/role` | ✅ Implemented |
| 4 | Destructive write IDOR on transfer | A1 — Broken Access Control | `POST /api/transactions` | ✅ Implemented |
| 5 | Weak JWT secret | A2 — Cryptographic Failures | `POST /api/auth/login` | ✅ Implemented |
| 6 | Insecure password storage (MD5) | A2 — Cryptographic Failures | `POST /api/auth/login-v2` | ✅ Implemented |
| 7 | Sensitive data exposure in logs | A2 — Cryptographic Failures | `AuthService`, `TransactionService` | ✅ Implemented |
| 8 | SQL injection on account search | A3 — Injection | `GET /api/accounts/search` | ✅ Implemented |
| 9 | SQL injection on transaction filter | A3 — Injection | `GET /api/transactions/filter` | ✅ Implemented |
| 10 | Command injection on admin export | A3 — Injection | `GET /api/admin/export` | ✅ Implemented |
| 11 | Missing rate limiting on login | A4 — Insecure Design | `POST /api/auth/login` | ✅ Implemented |
| 12 | Predictable account numbers | A4 — Insecure Design | `POST /api/accounts` | ✅ Implemented |
| 13 | Weak session management | A4 — Insecure Design | JWT config | ✅ Implemented |
| 14 | Verbose error messages | A5 — Security Misconfiguration | All endpoints | ✅ Implemented |
| 15 | H2 console exposed | A5 — Security Misconfiguration | `/h2-console` | ✅ Implemented |
| 16 | CORS misconfiguration | A5 — Security Misconfiguration | All endpoints | ✅ Implemented |
| 17 | No account lockout | A7 — Authentication Failures | `POST /api/auth/login` | ✅ Implemented |
| 18 | Weak password policy | A7 — Authentication Failures | `POST /api/auth/register` | ✅ Implemented |
| 19 | JWT not properly validated | A7 — Authentication Failures | All protected endpoints | ✅ Implemented |
| 20 | Missing input validation on transactions | A8 — Integrity Failures | `POST /api/transactions` | ✅ Implemented |
| 21 | Insecure deserialization | A8 — Integrity Failures | `POST /api/admin/import` | ✅ Implemented |
| 22 | SSRF via webhook | A10 — SSRF | `POST /api/accounts/{id}/webhook` | ✅ Implemented |

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
