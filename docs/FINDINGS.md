# RedLedger — Security Tool Findings

> **Purpose:** Unified summary of all automated scanner and manual testing findings.  
> **Scope:** Intentionally vulnerable endpoints only. All findings are by design.  
> **Tools:** Snyk (SCA), Semgrep (SAST), OWASP Dependency-Check (SCA), Burp Suite (Manual)  
> **Reports:** `docs/evidence/snyk-report.json`, `docs/evidence/semgrep-report.json`, `docs/evidence/dependency-check-report.json`

---

## Summary

| Tool                   | Critical | High | Medium | Low |
|------------------------|----------|------|--------|-----|
| Snyk SCA               | 3        | 6    | 2      | 0   |
| Semgrep SAST           | 0        | 3    | 2      | 0   |
| OWASP Dependency-Check | 3        | 4    | 4      | 1   |
| Burp Suite (Manual)    | 6        | 10   | 5      | 0   |

> **Note:** Snyk deduplicates by unique CVE/package. OWASP Dependency-Check counts unique CVEs across all affected JARs. Semgrep bcrypt-hash findings are false positives (intentional seed data).

---

## Snyk SCA Findings

> **Report:** `docs/evidence/snyk-report.json`  
> **Command:** `snyk test --severity-threshold=medium`

| Snyk ID | Package | Version | Severity | CVSS | CVE | CWE | Description | Fix |
|---------|---------|---------|----------|------|-----|-----|-------------|-----|
| SNYK-JAVA-COMMONSCOLLECTIONS-6056408 | `commons-collections` | 3.2.1 | **Critical** | 9.8 | CVE-2015-4852 | CWE-502 | Deserialization of Untrusted Data — RCE via `InvokerTransformer` gadget chain (CISA KEV) | Upgrade to 3.2.2 |
| SNYK-JAVA-COMMONSCOLLECTIONS-30078 | `commons-collections` | 3.2.1 | **Critical** | 9.8 | CVE-2015-7501 | CWE-502 | Deserialization of Untrusted Data — arbitrary code execution via `AnnotationInvocationHandler` | Upgrade to 3.2.2 |
| SNYK-JAVA-COMMONSCOLLECTIONS-472711 | `commons-collections` | 3.2.1 | **Critical** | 9.8 | CVE-2015-6420 | CWE-502 | Deserialization of Untrusted Data — `InvokerTransformer` not blocked pre-3.2.2 | Upgrade to 3.2.2 |
| SNYK-JAVA-ORGAPACHETOMCATEMBED-15989808 | `tomcat-embed-core` | 11.0.18 | **High** | 8.8 | CVE-2026-29145 | CWE-287 | Improper Authentication — OCSP soft-fail bypass in `CLIENT_CERT` auth | Upgrade to 11.0.20 |
| SNYK-JAVA-ORGAPACHETOMCATEMBED-15989820 | `tomcat-embed-core` | 11.0.18 | **High** | 8.3 | CVE-2026-34500 | CWE-287 | Improper Authentication — edge-case OCSP soft-fail bypass | Upgrade to 11.0.21 |
| SNYK-JAVA-ORGAPACHETOMCATEMBED-15990059 | `tomcat-embed-core` | 11.0.18 | **High** | 8.2 | CVE-2026-29129 | CWE-327 | Broken/Risky Cryptographic Algorithm — cipher preference order not preserved during TLS negotiation | Upgrade to 11.0.20 |
| SNYK-JAVA-ORGAPACHETOMCATEMBED-15990633 | `tomcat-embed-core` | 11.0.18 | **High** | 8.2 | CVE-2026-24880 | CWE-444 | HTTP Request Smuggling — invalid chunk extensions in `ChunkedInputFilter` | Upgrade to 11.0.20 |
| SNYK-JAVA-COMFASTERXMLJACKSONCORE-15907551 | `jackson-core` | 2.21.1 | **High** | 8.7 | GHSA-2m67-wjpj-xhg9 | CWE-770 | Allocation of Resources Without Limits — oversized JSON documents bypass size limits (DoS) | Upgrade to 2.21.2 |
| SNYK-JAVA-COMFASTERXMLJACKSONCORE-15907551 | `jackson-core` (via `jackson-databind`) | 2.21.1 | **High** | 8.7 | GHSA-2m67-wjpj-xhg9 | CWE-770 | Same as above — transitive via `jackson-databind` | Upgrade `jackson-databind` to 2.21.2 |
| SNYK-JAVA-ORGAPACHETOMCATEMBED-15989812 | `tomcat-embed-core` | 11.0.18 | **Medium** | 6.3 | CVE-2026-34483 | CWE-116 | Improper Encoding — JSON log injection via `JsonAccessLogValve` | Upgrade to 11.0.21 |
| SNYK-JAVA-ORGAPACHETOMCATEMBED-15990787 | `tomcat-embed-core` | 11.0.18 | **Medium** | 5.1 | CVE-2026-25854 | CWE-601 | Open Redirect — `LoadBalancerDrainingValve` redirects to attacker-controlled URL | Upgrade to 11.0.20 |

---

## Semgrep SAST Findings

> **Report:** `docs/evidence/semgrep-report.json`  
> **Ruleset:** `p/default`

| Rule ID | File | Line | Severity | OWASP Category | Description | Notes |
|---------|------|------|----------|----------------|-------------|-------|
| `tainted-system-command` | `AdminController.java` | 79 | **Error** | A03 — Injection | User input passed directly to OS command execution — Command Injection | Intentional vuln (TC-A3-03) |
| `object-deserialization` | `AdminController.java` | 101 | **Warning** | A08 — Software & Data Integrity Failures | Unsafe `ObjectInputStream` deserialization — potential RCE | Intentional vuln (TC-A8-02) |
| `spring-sqli` | `AccountService.java` | 92 | **Warning** | A03 — Injection | Raw SQL string concatenation — SQL Injection | Intentional vuln (TC-A3-01) |
| `use-of-md5` | `AuthService.java` | 104 | **Warning** | A02 — Cryptographic Failures | MD5 used for password hashing — insecure algorithm | Intentional vuln (TC-A2-02) |
| `spring-sqli` | `TransactionService.java` | 132 | **Warning** | A03 — Injection | Raw SQL string concatenation — SQL Injection | Intentional vuln (TC-A3-02) |
| `detected-bcrypt-hash` | `data.sql` | 13 | **Error** | A07 — Authentication Failures | bcrypt hash detected in source file | **False Positive** — intentional seed data |
| `detected-bcrypt-hash` | `data.sql` | 14 | **Error** | A07 — Authentication Failures | bcrypt hash detected in source file | **False Positive** — intentional seed data |
| `detected-bcrypt-hash` | `data.sql` | 15 | **Error** | A07 — Authentication Failures | bcrypt hash detected in source file | **False Positive** — intentional seed data |

> ⚠️ The 3 `detected-bcrypt-hash` findings are **false positives**. Semgrep flags bcrypt hashes as potential secret leaks, but these are intentional test password hashes in `data.sql` for seeding the in-memory H2 database.

---

## OWASP Dependency-Check Findings

> **Report:** `docs/evidence/dependency-check-report.json`  
> **Command:** `./gradlew dependencyCheckAnalyze`  
> **Note:** CVEs affecting multiple Spring Boot JARs are listed once by unique CVE.

| CVE | Package | Severity | CVSS | CWE | Description | Fix |
|-----|---------|----------|------|-----|-------------|-----|
| CVE-2015-6420 | `commons-collections-3.2.1.jar` | **Critical** | 9.8 | CWE-502 | Deserialization RCE via `InvokerTransformer` | Upgrade to 3.2.2 |
| CVE-2026-40976 | `spring-boot-*.jar` (multiple) | **Critical** | 9.1 | CWE-862 | Spring Boot default web security ineffective — missing authorization | Upgrade Spring Boot |
| CVE-2022-31691 | `spring-boot-devtools-4.0.4.jar` | **Critical** | 9.8 | CWE-94 | Spring Tools RCE — code injection via devtools remote restart | Remove devtools from prod |
| CVE-2026-22747 | `spring-security-*.jar` (config/core/crypto/web) | **High** | 8.1 | CWE-297 | Improper certificate validation in `SubjectX500PrincipalExtractor` | Upgrade Spring Security |
| CVE-2026-40972 | `spring-boot-*.jar` (multiple) | **High** | 7.5 | CWE-208 | Timing side-channel attack on same network | Upgrade Spring Boot |
| CVE-2026-40975 | `spring-boot-*.jar` (multiple) | **High** | 7.5 | CWE-330 | `${random.value}` values not suitable for use as secrets | Upgrade Spring Boot |
| CVE-2026-22753 | `spring-security-*.jar` (multiple) | **High** | 7.5 | CWE-693 | `securityMatcher` protection mechanism failure | Upgrade Spring Security |
| CVE-2026-22754 | `spring-security-*.jar` (multiple) | **High** | 7.5 | CWE-284 | `<sec:intercept-url>` improper access control | Upgrade Spring Security |
| CVE-2026-34478 | `log4j-api-2.25.3.jar` | **Medium** | 7.5 | CWE-117/684 | Log injection via `Rfc5424Layout` | Upgrade Log4j |
| CVE-2026-34480 | `log4j-api-2.25.3.jar` | **Medium** | 7.5 | CWE-116 | Improper output encoding in `XmlLayout` | Upgrade Log4j |
| CVE-2026-34481 | `log4j-api-2.25.3.jar` | **Medium** | 7.5 | CWE-116 | Improper output encoding in `JsonTemplateLayout` | Upgrade Log4j |
| CVE-2026-22748 | `spring-security-*.jar` (multiple) | **Medium** | 6.5 | CWE-20 | JWT decoder misconfiguration — improper input validation | Upgrade Spring Security |
| CVE-2026-40977 | `spring-boot-*.jar` (multiple) | **Medium** | 6.7 | CWE-59 | `ApplicationPidFileWriter` symlink attack | Upgrade Spring Boot |
| CVE-2026-22746 | `spring-security-*.jar` (multiple) | **Low** | 3.7 | CWE-208 | `UserDetailsService` timing side-channel | Upgrade Spring Security |

---

## Manual Testing Findings (Burp Suite)

> **Test Plan:** `docs/burp/test-plan.md`  
> **Screenshots:** `docs/evidence/screenshots/`

| Test Case | Endpoint | Severity | OWASP Category | Description | Evidence |
|-----------|----------|----------|----------------|-------------|----------|
| TC-A1-01 | `GET /api/accounts/{id}` | High | A01 — Broken Access Control | IDOR: any authenticated user can access any account by ID | `screenshots/a1-idor-account.png` |
| TC-A1-02 | `GET /api/transactions/{id}` | High | A01 — Broken Access Control | IDOR: any authenticated user can access any transaction by ID | `screenshots/a1-idor-transaction.png` |
| TC-A1-03 | `GET /api/admin/**` | Critical | A01 — Broken Access Control | BFLA: ROLE_USER can access and invoke admin endpoints | `screenshots/a1-bfla-admin.png` |
| TC-A1-04 | `POST /api/transactions` | High | A01 — Broken Access Control | Destructive write IDOR: transfer from another user's account | `screenshots/a1-unauth-transfer.png` |
| TC-A2-01 | `POST /api/auth/login` | Critical | A02 — Cryptographic Failures | Weak JWT secret (`secret`) allows token forgery | `screenshots/a2-jwt-forge.png` |
| TC-A2-02 | `POST /api/auth/login-v2` | High | A02 — Cryptographic Failures | Passwords stored as MD5 hashes — confirmed by Semgrep (`AuthService.java:104`) | `screenshots/a2-md5-password.png` |
| TC-A2-03 | `AuthService`, `TransactionService` | Medium | A02 — Cryptographic Failures | Sensitive data (credentials, account numbers) logged in plaintext | `screenshots/a2-sensitive-logs.png` |
| TC-A3-01 | `GET /api/accounts/search` | Critical | A03 — Injection | SQL Injection via `name` parameter — confirmed by Semgrep (`AccountService.java:92`) | `screenshots/a3-sqli-search.png` |
| TC-A3-02 | `GET /api/transactions/filter` | Critical | A03 — Injection | SQL Injection via `status` parameter — confirmed by Semgrep (`TransactionService.java:132`) | `screenshots/a3-sqli-filter.png` |
| TC-A3-03 | `GET /api/admin/export` | Critical | A03 — Injection | Command Injection via `filename` parameter — confirmed by Semgrep (`AdminController.java:79`) | `screenshots/a3-cmdi-export.png` |
| TC-A4-01 | `POST /api/auth/login` | High | A04 — Insecure Design | No rate limiting — brute force login unrestricted | `screenshots/a4-bruteforce.png` |
| TC-A4-02 | `POST /api/accounts` | Medium | A04 — Insecure Design | Predictable sequential account numbers enable enumeration | `screenshots/a4-predictable-accounts.png` |
| TC-A4-03 | JWT config | High | A04 — Insecure Design | Weak session management — excessive token lifetime, no revocation | `screenshots/a4-weak-session.png` |
| TC-A5-01 | All endpoints | Medium | A05 — Security Misconfiguration | Verbose stack traces exposed in error responses | `screenshots/a5-stacktrace.png` |
| TC-A5-02 | `/h2-console` | Critical | A05 — Security Misconfiguration | H2 console publicly accessible — full DB exposed | `screenshots/a5-h2-console.png` |
| TC-A5-03 | All endpoints | High | A05 — Security Misconfiguration | CORS misconfiguration: wildcard origin with credentials | `screenshots/a5-cors.png` |
| TC-A7-01 | `POST /api/auth/login` | High | A07 — Authentication Failures | No account lockout after repeated failed attempts | `screenshots/a7-no-lockout.png` |
| TC-A7-02 | `POST /api/auth/register` | Medium | A07 — Authentication Failures | No password complexity enforcement | `screenshots/a7-weak-password.png` |
| TC-A7-03 | All protected endpoints | High | A07 — Authentication Failures | Expired/tampered JWT tokens accepted | `screenshots/a7-jwt-validation.png` |
| TC-A8-01 | `POST /api/transactions` | High | A08 — Software & Data Integrity Failures | Negative transfer amounts accepted — balance manipulation | `screenshots/a8-negative-transfer.png` |
| TC-A8-02 | `POST /api/admin/import` | Critical | A08 — Software & Data Integrity Failures | Insecure deserialization — RCE via Commons Collections gadget chain — confirmed by Snyk (CVE-2015-4852) and Semgrep (`AdminController.java:101`) | `screenshots/a8-deserial.png` |
| TC-A10-01 | `POST /api/accounts/{id}/webhook` | Critical | A10 — SSRF | SSRF via webhook URL — internal services and cloud metadata accessible | `screenshots/a10-ssrf-metadata.png` |

---

## OWASP Coverage Summary

| OWASP Category | Vuln Count | Severity Range |
|----------------|-----------|----------------|
| A01 — Broken Access Control | 4 | High–Critical |
| A02 — Cryptographic Failures | 3 | Medium–Critical |
| A03 — Injection | 3 | Critical |
| A04 — Insecure Design | 3 | Medium–High |
| A05 — Security Misconfiguration | 3 | Medium–Critical |
| A07 — Authentication Failures | 3 | Medium–High |
| A08 — Software & Data Integrity Failures | 2 | High–Critical |
| A10 — SSRF | 1 | Critical |
| **Total** | **22** | |