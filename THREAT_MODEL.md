# RedLedger — STRIDE Threat Model

> **Version:** 1.0  
> **Status:** Complete  
> **Last Updated:** 2026-04-22  
> **Author:** RedLedger AppSec Portfolio Project

---

## 1. Application Overview

RedLedger is a deliberately vulnerable fintech REST API that simulates a simplified banking and payment platform. It handles:

- **User authentication** — registration, login, JWT issuance
- **Account management** — create accounts, query balances, search accounts
- **Financial transactions** — fund transfers between accounts, transaction history
- **Administrative operations** — user management, role assignment, data export

The application is built with Java / Spring Boot, secured (intentionally weakly) with JWT Bearer tokens, and backed by an H2 in-memory relational database. It is designed to demonstrate OWASP Top 10 and OWASP API Security Top 10 vulnerabilities.

---

## 2. Architecture Overview

### Components

| Component | Technology | Trust Level |
|---|---|---|
| REST API | Spring Boot / Spring MVC | Semi-trusted (validates JWT) |
| Auth Layer | Spring Security + JJWT (HS256) | Trusted |
| Business Services | `AccountService`, `TransactionService`, `UserService` | Trusted |
| Database | H2 in-memory (JPA / JDBC) | Trusted |
| Admin Subsystem | `AdminController` + `UserService` | Privileged |
| Client | Browser / Postman / Burp Suite | Untrusted |

### Trust Boundaries

```
┌─────────────────────────────────────────────────────────────┐
│  UNTRUSTED ZONE                                             │
│                                                             │
│   [Attacker / Client]  ──HTTP──►  [Load Balancer / TLS]     │
└──────────────────────────────────────┬──────────────────────┘
                                       │  Trust Boundary #1
┌──────────────────────────────────────▼──────────────────────┐
│  APPLICATION ZONE                                           │
│                                                             │
│   [JwtAuthenticationFilter]                                 │
│          │                                                  │
│          ▼                                                  │
│   [Spring Security / SecurityConfig]                        │
│          │                                                  │
│          ▼                                                  │
│   [Controllers: Auth / Account / Transaction / Admin]       │
│          │                                                  │
│          ▼                                                  │
│   [Services: AuthService / AccountService /                 │
│              TransactionService / UserService]              │
└──────────────────────────────────────┬──────────────────────┘
                                       │  Trust Boundary #2
┌──────────────────────────────────────▼──────────────────────┐
│  DATA ZONE                                                  │
│                                                             │
│   [H2 Database — Users / Accounts / Transactions]           │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Data Flow Diagrams

### 3.1 Authentication Flow

```
Client                  AuthController           AuthService                H2 DB
  │                           │                       │                       │
  │── POST /api/auth/login ──►│                       │                       │
  │   {username, password}    │── login(req) ────────►│                       │
  │                           │                       │── findByUsername() ──►│
  │                           │                       │◄─ User entity ────────│
  │                           │                       │                       │
  │                           │                       │ [BCrypt.matches()]    │
  │                           │                       │ [JwtUtils.generate()] │
  │                           │◄─ LoginResponse ──────│                       │
  │◄─ 200 OK {token, role} ───│                       │                       │
```

**Data elements in flight:** plaintext credentials (request), JWT (response), hashed password (DB).
**Threat surface:** credential interception, weak secret enabling token forgery, brute-force on login.

---

### 3.2 Account / Transaction Flow

```
Client                   Filter               Controller            Service               H2 DB
  │                        │                      │                    │                    │
  │── GET /api/accounts ──►│                      │                    │                    │
  │   Authorization: Bearer│                      │                    │                    │
  │                        │ [validateToken()]    │                    │                    │
  │                        │── set SecurityCtx ──►│                    │                    │
  │                        │                      │── getAccounts() ──►│                    │
  │                        │                      │                    │── SELECT * ───────►│
  │                        │                      │                    │◄─ Account rows ────│
  │                        │                      │◄─ AccountResponse  │                    │
  │◄─ 200 OK [accounts] ───│                      │                    │                    │
```

**Data elements in flight:** JWT (header), account numbers, balances, transaction amounts.
**Threat surface:** IDOR (no ownership check), missing auth on transfer source, SQL injection in search/filter.

---

### 3.3 Admin Operations Flow

```
Client (ROLE_USER)       Filter             AdminController         UserService             H2 DB
  │                        │                      │                      │                    │
  │── GET /api/admin/users►│                      │                      │                    │
  │   Bearer <user_token>  │ [validateToken()]    │                      │                    │
  │                        │── set SecurityCtx ──►│                      │                    │
  │                        │                      │ [VULN: no role       │                    │
  │                        │                      │  check → BFLA]       │                    │
  │                        │                      │── getAllUsers() ────►│                    │
  │                        │                      │                      │── SELECT * ───────►│
  │◄─ 200 OK [all users] ──│                      │                      │                    │
```

**Data elements in flight:** all user PII, roles, account associations.
**Threat surface:** BFLA, privilege escalation via role update endpoint, command injection in export.

---

## 4. STRIDE Analysis

### Legend

| Column | Meaning |
|---|---|
| Threat ID | Unique identifier for cross-referencing |
| Component | Affected system component |
| Risk | Critical / High / Medium / Low |
| OWASP | Mapped OWASP Top 10 / API Sec category |
| Phase 3 Task | Checklist task that implements this vuln |
| Status | Planned / Implemented / Mitigated |

---

### 4.1 Spoofing — *Can an attacker impersonate a legitimate user or service?*

| Threat ID | Threat | Component | Risk | OWASP | Phase 3 Task | Status |
|---|---|---|---|---|---|---|
| S-01 | JWT token forgery via weak HS256 secret (`secret`) | `JwtUtils`, `application.properties` | **High** | A2 Crypto Failures | 3.A2.1 | Planned |
| S-02 | `alg: none` attack — strip signature, bypass validation | `JwtAuthenticationFilter`, `JwtUtils` | **High** | A7 Auth Failures | 3.A7.3 | Planned |
| S-03 | Credential stuffing / brute-force on `/api/auth/login` | `AuthController`, `AuthService` | **High** | A7 Auth Failures | 3.A4.1, 3.A7.1 | Planned |
| S-04 | Accepting expired JWT tokens (skip expiry check) | `JwtAuthenticationFilter` | **Medium** | A7 Auth Failures | 3.A7.3 | Planned |
| S-05 | Plaintext/MD5 password storage on `/api/auth/login-v2` | `AuthService` | **Critical** | A2 Crypto Failures | 3.A2.2 | Planned |

**Attack scenario (S-01):** An attacker discovers the secret `secret` via Burp Suite interception or source code review. Using a tool like `jwt_tool` or CyberChef, they forge a token with `"role": "ROLE_ADMIN"` and gain full administrative access without valid credentials.

**Attack scenario (S-02):** An attacker decodes a valid JWT, modifies the payload to elevate their role, sets `"alg": "none"`, removes the signature, and replays the token. If the library or filter does not explicitly reject unsigned tokens, the server accepts it.

---

### 4.2 Tampering — *Can an attacker modify data in transit or at rest?*

| Threat ID | Threat | Component | Risk | OWASP | Phase 3 Task | Status |
|---|---|---|---|---|---|---|
| T-TX-01 | Negative transfer amount reverses fund flow (steal money) | `TransactionService`, `TransferRequest` | **Critical** | A8 Integrity Failures | 3.A8.1 | Planned |
| T-TX-02 | Zero-amount transfer pollutes audit trail | `TransactionService` | **Medium** | A8 Integrity Failures | 3.A8.1 | Planned |
| T-TX-03 | Transfer from any source account (no ownership check) | `TransactionController`, `TransactionService` | **Critical** | A1 Broken Access Control | 3.A1.4 | Planned |
| T-AC-01 | Role escalation via `PUT /api/admin/users/{id}/role` | `AdminController` | **High** | A1 Broken Access Control | 3.A1.3 | Planned |
| T-DB-01 | SQL injection in account search modifies query logic | `AccountService` (JDBC) | **Critical** | A3 Injection | 3.A3.1 | Planned |
| T-DB-02 | SQL injection in transaction filter | `TransactionService` | **High** | A3 Injection | 3.A3.2 | Planned |
| T-SER-01 | Insecure deserialization via `/api/admin/import` | `AdminController` | **Critical** | A8 Integrity Failures | 3.A8.2 | Planned |

**Attack scenario (T-TX-01):** An authenticated user sends `POST /api/transactions/transfer` with `"amount": -500.00`. Without validation, the service subtracts a negative value from the destination account and adds it to the source — effectively stealing funds. No `@Valid` constraint or service-layer check prevents this.

**Attack scenario (T-DB-01):** The search endpoint `GET /api/accounts/search?name=` concatenates the parameter directly into a JDBC query string. An attacker submits `name=' OR '1'='1` to dump all accounts, or `name='; DROP TABLE accounts; --` to destroy data.

---

### 4.3 Repudiation — *Can an attacker deny performing an action?*

| Threat ID | Threat | Component | Risk | OWASP | Phase 3 Task | Status |
|---|---|---|---|---|---|---|
| R-01 | No immutable audit log for financial transactions | `TransactionService` | **High** | A4 Insecure Design | — | Planned (doc only) |
| R-02 | Sensitive data (passwords, account numbers) logged in plaintext | `AuthService`, `TransactionService`, `logback.xml` | **High** | A2 Crypto Failures | 3.A2.3 | Planned |
| R-03 | No correlation ID / request tracing across services | All controllers | **Medium** | A4 Insecure Design | — | Planned (doc only) |
| R-04 | Admin actions (role change, delete user) not logged | `AdminController` | **High** | A1 Broken Access Control | 3.A1.3 | Planned |

**Attack scenario (R-02):** During registration, `AuthService` logs `"User registered: jsmith / password123"`. An attacker who gains read access to log files (via SSRF to `localhost:8080/actuator/logfile`, or a misconfigured log aggregator) harvests plaintext credentials for all registered users.

---

### 4.4 Information Disclosure — *Can an attacker access data they shouldn't?*

| Threat ID | Threat | Component | Risk | OWASP | Phase 3 Task | Status |
|---|---|---|---|---|---|---|
| I-01 | IDOR — `GET /api/accounts/{id}` exposes any account | `AccountController`, `AccountService` | **High** | A1 Broken Access Control | 3.A1.1 | Planned |
| I-02 | IDOR — `GET /api/transactions/{id}` exposes any transaction | `TransactionController`, `TransactionService` | **High** | A1 Broken Access Control | 3.A1.2 | Planned |
| I-03 | IDOR — `GET /api/transactions?accountId=` leaks other users' history | `TransactionController` | **High** | A1 Broken Access Control | 3.A1.2 | Planned |
| I-04 | Stack traces returned in 500 responses | `application.properties`, error handling | **Medium** | A5 Security Misconfig | 3.A5.1 | Planned |
| I-05 | H2 console exposed with no auth at `/h2-console` | `SecurityConfig`, `application.properties` | **High** | A5 Security Misconfig | 3.A5.2 | Planned |
| I-06 | SQL error details leaked in API responses | Error handler | **Medium** | A5 Security Misconfig | 3.A5.1 | Planned |
| I-07 | CORS wildcard + credentials allows cross-origin data theft | `WebConfig` | **High** | A5 Security Misconfig | 3.A5.3 | Planned |
| I-08 | Predictable sequential account numbers enable enumeration | `AccountService` | **Medium** | A4 Insecure Design | 3.A4.2 | Planned |
| I-09 | SSRF — server fetches attacker-supplied URL, probes internal network | `AccountController` (webhook) | **High** | A10 SSRF | 3.A10.1 | Planned |
| I-10 | Command injection in export leaks server filesystem | `AdminController` (export) | **Critical** | A3 Injection | 3.A3.3 | Planned |

**Attack scenario (I-01 — IDOR):** User `jsmith` (userId=2) authenticates and receives a JWT. They call `GET /api/accounts/1` — an account owned by `admin`. Because `AccountService.getAccount()` performs no ownership check, the full account object (number, balance, owner) is returned. The attacker iterates IDs 1–N to enumerate all accounts in the system.

**Attack scenario (I-09 — SSRF):** An attacker calls `POST /api/accounts/3/webhook` with body `{"url": "http://169.254.169.254/latest/meta-data/"}`. The server's `RestTemplate` fetches the URL and returns cloud instance metadata (IAM credentials, instance ID) in the response — enabling lateral movement in a cloud-hosted deployment.

---

### 4.5 Denial of Service — *Can an attacker degrade or crash the service?*

| Threat ID | Threat | Component | Risk | OWASP | Phase 3 Task | Status |
|---|---|---|---|---|---|---|
| D-01 | No rate limiting on `/api/auth/login` — enables brute-force | `AuthController`, `SecurityConfig` | **High** | A4 Insecure Design | 3.A4.1 | Planned |
| D-02 | No account lockout after failed login attempts | `AuthService` | **High** | A7 Auth Failures | 3.A7.1 | Planned |
| D-03 | Unbounded transaction queries — no pagination | `TransactionController` | **Medium** | A4 Insecure Design | — | Planned (doc only) |
| D-04 | Insecure deserialization gadget chain can crash JVM | `AdminController` (import) | **High** | A8 Integrity Failures | 3.A8.2 | Planned |
| D-05 | SSRF to internal services can exhaust connection pool | `AccountController` (webhook) | **Medium** | A10 SSRF | 3.A10.1 | Planned |

**Attack scenario (D-01):** With no rate limiting or lockout, an attacker scripts `POST /api/auth/login` at thousands of requests per minute, attempting a credential dictionary attack against known usernames (`admin`, `jsmith`, `jdoe`). The H2 in-memory database is also hammered with authentication queries, degrading service for legitimate users.

---

### 4.6 Elevation of Privilege — *Can an attacker gain permissions beyond what they were granted?*

| Threat ID | Threat | Component | Risk | OWASP | Phase 3 Task | Status |
|---|---|---|---|---|---|---|
| E-01 | BFLA — `GET /api/admin/users` accessible to `ROLE_USER` | `AdminController`, `SecurityConfig` | **Critical** | A1 Broken Access Control | 3.A1.3 | Planned |
| E-02 | BFLA — `PUT /api/admin/users/{id}/role` allows self-promotion | `AdminController` | **Critical** | A1 Broken Access Control | 3.A1.3 | Planned |
| E-03 | BFLA — `DELETE /api/admin/users/{id}` accessible to `ROLE_USER` | `AdminController` | **High** | A1 Broken Access Control | 3.A1.3 | Planned |
| E-04 | JWT forgery with forged `ROLE_ADMIN` claim | `JwtUtils`, `JwtAuthenticationFilter` | **Critical** | A2 / A7 | 3.A2.1, 3.A7.3 | Planned |
| E-05 | Weak password policy allows trivially guessable passwords | `RegisterRequest`, `AuthService` | **Medium** | A7 Auth Failures | 3.A7.2 | Planned |
| E-06 | Long-lived JWT (24h) with no revocation — stolen token = persistent access | `application.properties`, `JwtUtils` | **High** | A4 Insecure Design | 3.A4.3 | Planned |

**Attack scenario (E-01 — BFLA):** A regular user authenticates as `jsmith` and calls `GET /api/admin/users`. The `@PreAuthorize("hasRole('ADMIN')")` annotation has been intentionally removed from `AdminController`. Spring Security's filter chain passes the request through, and the full user list (including admin credentials hash and roles) is returned to an unprivileged user.

**Attack scenario (E-02):** The same user calls `PUT /api/admin/users/2/role` with body `{"role": "ROLE_ADMIN"}`, promoting their own account to admin. They then have full control of the platform.

---

## 5. Threat-to-Vulnerability Master Map

| Threat ID | STRIDE Category | OWASP Top 10 | Affected Endpoint(s) | Phase 3 Task | CWE |
|---|---|---|---|---|---|
| S-01 | Spoofing | A2 Crypto Failures | All (JWT) | 3.A2.1 | CWE-321 |
| S-02 | Spoofing | A7 Auth Failures | All (JWT) | 3.A7.3 | CWE-347 |
| S-03 | Spoofing | A7 Auth Failures | `POST /api/auth/login` | 3.A4.1, 3.A7.1 | CWE-307 |
| S-04 | Spoofing | A7 Auth Failures | All (JWT) | 3.A7.3 | CWE-613 |
| S-05 | Spoofing | A2 Crypto Failures | `POST /api/auth/login-v2` | 3.A2.2 | CWE-256 |
| T-TX-01 | Tampering | A8 Integrity Failures | `POST /api/transactions/transfer` | 3.A8.1 | CWE-20 |
| T-TX-03 | Tampering | A1 Broken Access Control | `POST /api/transactions/transfer` | 3.A1.4 | CWE-639 |
| T-AC-01 | Tampering | A1 Broken Access Control | `PUT /api/admin/users/{id}/role` | 3.A1.3 | CWE-269 |
| T-DB-01 | Tampering | A3 Injection | `GET /api/accounts/search` | 3.A3.1 | CWE-89 |
| T-DB-02 | Tampering | A3 Injection | `GET /api/transactions/filter` | 3.A3.2 | CWE-89 |
| T-SER-01 | Tampering | A8 Integrity Failures | `POST /api/admin/import` | 3.A8.2 | CWE-502 |
| R-02 | Repudiation | A2 Crypto Failures | `AuthService`, `TransactionService` | 3.A2.3 | CWE-532 |
| I-01 | Info Disclosure | A1 Broken Access Control | `GET /api/accounts/{id}` | 3.A1.1 | CWE-639 |
| I-02 | Info Disclosure | A1 Broken Access Control | `GET /api/transactions/{id}` | 3.A1.2 | CWE-639 |
| I-04 | Info Disclosure | A5 Security Misconfig | All (error handler) | 3.A5.1 | CWE-209 |
| I-05 | Info Disclosure | A5 Security Misconfig | `/h2-console` | 3.A5.2 | CWE-16 |
| I-07 | Info Disclosure | A5 Security Misconfig | All (CORS) | 3.A5.3 | CWE-942 |
| I-08 | Info Disclosure | A4 Insecure Design | `POST /api/accounts` | 3.A4.2 | CWE-340 |
| I-09 | Info Disclosure | A10 SSRF | `POST /api/accounts/{id}/webhook` | 3.A10.1 | CWE-918 |
| I-10 | Info Disclosure | A3 Injection | `GET /api/admin/export` | 3.A3.3 | CWE-78 |
| D-01 | Denial of Service | A4 Insecure Design | `POST /api/auth/login` | 3.A4.1 | CWE-307 |
| D-02 | Denial of Service | A7 Auth Failures | `POST /api/auth/login` | 3.A7.1 | CWE-307 |
| E-01 | Elevation of Privilege | A1 Broken Access Control | `GET /api/admin/users` | 3.A1.3 | CWE-285 |
| E-02 | Elevation of Privilege | A1 Broken Access Control | `PUT /api/admin/users/{id}/role` | 3.A1.3 | CWE-269 |
| E-04 | Elevation of Privilege | A2 / A7 | All (JWT) | 3.A2.1, 3.A7.3 | CWE-321 |
| E-06 | Elevation of Privilege | A4 Insecure Design | All (JWT) | 3.A4.3 | CWE-613 |

---

## 6. Assets & Impact Summary

| Asset | Confidentiality | Integrity | Availability | Notes |
|---|---|---|---|---|
| User credentials (passwords) | Critical | High | Medium | Plaintext logging, weak hashing path |
| JWT signing secret | Critical | Critical | High | Weak secret enables full impersonation |
| Account balances | High | Critical | Medium | IDOR + negative transfer attacks |
| Transaction records | High | High | Medium | IDOR, no audit immutability |
| User PII (username, role) | High | High | Low | BFLA exposes full user list |
| H2 database | Critical | Critical | High | Exposed console, SQLi, command injection |
| Server filesystem | Critical | Critical | High | Command injection via export endpoint |

---

## 7. Out of Scope

The following are acknowledged but not implemented in this portfolio project:

- TLS/HTTPS termination (assumed handled by reverse proxy in production)
- DDoS protection at the network layer
- Physical security controls
- Third-party dependency supply chain attacks (covered partially by Snyk SCA in Phase 4)

---

## 8. Evidence & References

- Exploitation evidence will be stored in `docs/evidence/` (Phase 4)
- Burp Suite test plan: `docs/burp/test-plan.md` (Phase 4)
- Scanner findings: `docs/FINDINGS.md` (Phase 4–5)
- Remediation guidance: `docs/REMEDIATION.md` (Phase 5–6)

### References

- [OWASP Top 10 (2021)](https://owasp.org/www-project-top-ten/)
- [OWASP API Security Top 10 (2023)](https://owasp.org/www-project-api-security/)
- [STRIDE Threat Modeling — Microsoft](https://learn.microsoft.com/en-us/azure/security/develop/threat-modeling-tool-threats)
- [CWE Top 25 Most Dangerous Software Weaknesses](https://cwe.mitre.org/top25/)