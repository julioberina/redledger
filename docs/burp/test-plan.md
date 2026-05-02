# RedLedger — Burp Suite Test Plan

> **Tool:** Burp Suite Community Edition  
> **Note:** No project files (.burp) — Community Edition does not support saving projects.  
> Evidence is captured via screenshots saved to `docs/evidence/screenshots/`.

---

## Setup

1. Start RedLedger locally: `./gradlew bootRun`
2. Launch Burp Suite → Proxy → set browser to `127.0.0.1:8080`
3. Install Burp CA certificate in browser
4. Use Repeater for all manual request manipulation

---

## Test Credentials

| Username | Password     | Role                    |
|----------|--------------|-------------------------|
| admin    | admin123     | ROLE_ADMIN              |
| jsmith   | password123  | ROLE_USER               |
| jdoe     | password456  | ROLE_USER               |
| mduser   | password123  | ROLE_USER (MD5 path)    |

---

## A1 — Broken Access Control

### TC-A1-01: IDOR on Account Endpoint
- **Endpoint:** `GET /api/accounts/{id}`
- **Steps:**
    1. Login as `jsmith`, capture JWT
    2. Call `GET /api/accounts/{jsmith_account_id}`
    3. Change `{id}` to an account owned by `jdoe`
    4. Observe full account details returned without ownership check
- **Expected (vulnerable):** `200 OK` with jdoe's account data
- **Evidence:** `screenshots/a1-idor-account.png`

### TC-A1-02: IDOR on Transaction Endpoint
- **Endpoint:** `GET /api/transactions/{id}`
- **Steps:**
    1. Login as `jsmith`
    2. Request a transaction ID belonging to `jdoe`
    3. Observe transaction details returned
- **Expected (vulnerable):** `200 OK` with jdoe's transaction data
- **Evidence:** `screenshots/a1-idor-transaction.png`

### TC-A1-03: BFLA on Admin Endpoints
- **Endpoint:** `GET /api/admin/users`, `PUT /api/admin/users/{id}/role`
- **Steps:**
    1. Login as `jsmith` (ROLE_USER), capture JWT
    2. Call `GET /api/admin/users` with jsmith's token
    3. Call `PUT /api/admin/users/{jsmith_id}/role` with body `{"role": "ROLE_ADMIN"}`
    4. Observe privilege escalation succeeds
- **Expected (vulnerable):** `200 OK` on both requests
- **Evidence:** `screenshots/a1-bfla-admin.png`, `screenshots/a1-bfla-escalation.png`

### TC-A1-04: Unauthorized Transfer
- **Endpoint:** `POST /api/transactions/transfer`
- **Steps:**
    1. Login as `jsmith`
    2. Submit transfer with `sourceAccountId` belonging to `jdoe`
    3. Observe transfer executes without ownership check
- **Expected (vulnerable):** `200 OK`, funds moved from jdoe's account
- **Evidence:** `screenshots/a1-unauth-transfer.png`

---

## A2 — Cryptographic Failures

### TC-A2-01: Weak JWT Secret
- **Steps:**
    1. Login as `jsmith` and capture JWT
    2. Decode JWT at [jwt.io](https://jwt.io)
    3. Forge a new token signed with `secret` (the known weak key)
    4. Use forged token to access a protected endpoint
- **Expected (vulnerable):** Forged token accepted
- **Evidence:** `screenshots/a2-jwt-forge.png`

### TC-A2-02: Insecure Password Storage (MD5)
- **Endpoint:** `POST /api/auth/login-v2`
- **Steps:**
    1. Login as `mduser` via `POST /api/auth/login-v2`
    2. Open H2 console at `http://localhost:8080/h2-console`
    3. Run `SELECT * FROM users WHERE username = 'mduser'`
    4. Confirm password is stored as an MD5 hash (not bcrypt)
- **Expected (vulnerable):** Login succeeds; password column shows MD5 hash
- **Evidence:** `screenshots/a2-md5-password.png`

### TC-A2-03: Sensitive Data in Logs
- **Steps:**
    1. Login as `jsmith`, observe application logs in terminal
    2. Initiate a transfer, observe logs again
    3. Confirm raw credentials and account numbers appear in log output
- **Expected (vulnerable):** Credentials and PII visible in logs
- **Evidence:** `screenshots/a2-sensitive-logs.png`

---

## A3 — Injection

### TC-A3-01: SQL Injection in Account Search
- **Endpoint:** `GET /api/accounts/search?name=`
- **Steps:**
    1. Login as `jsmith`
    2. Send `name=test' OR '1'='1` in Repeater
    3. Observe all accounts returned regardless of ownership
    4. Try `name=test'; DROP TABLE accounts;--` and observe error response
- **Expected (vulnerable):** All accounts returned or SQL error leaked
- **Evidence:** `screenshots/a3-sqli-search.png`

### TC-A3-02: SQL Injection in Transaction Filter
- **Endpoint:** `GET /api/transactions/filter?status=`
- **Steps:**
    1. Login as `jsmith`
    2. Send `status=COMPLETED' OR '1'='1`
    3. Observe all transactions returned regardless of status or ownership
- **Expected (vulnerable):** All transactions returned
- **Evidence:** `screenshots/a3-sqli-filter.png`

### TC-A3-03: Command Injection in Export Endpoint
- **Endpoint:** `GET /api/admin/export?filename=`
- **Steps:**
    1. Login as `admin`
    2. Send `filename=test.txt; id`
    3. Observe OS command output in response
- **Expected (vulnerable):** Command output (`uid=...`) returned in response
- **Evidence:** `screenshots/a3-cmdi-export.png`

---

## A4 — Insecure Design

### TC-A4-01: Brute Force Login (No Rate Limiting)
- **Endpoint:** `POST /api/auth/login`
- **Steps:**
    1. In Repeater, send 20+ failed login attempts for `jsmith` rapidly
    2. Observe no lockout, delay, or CAPTCHA triggered
- **Expected (vulnerable):** All requests return `401` with no throttling
- **Evidence:** `screenshots/a4-bruteforce.png`

### TC-A4-02: Predictable Account Numbers
- **Steps:**
    1. Register 3 new users and create accounts for each
    2. Observe account numbers are sequential (`100001`, `100002`, `100003`)
    3. Enumerate accounts via IDOR using sequential IDs
- **Expected (vulnerable):** Sequential, guessable account numbers confirmed
- **Evidence:** `screenshots/a4-predictable-accounts.png`

### TC-A4-03: Weak Session Management
- **Steps:**
    1. Login as `jsmith` and decode the JWT at [jwt.io](https://jwt.io)
    2. Observe an excessively long or non-expiring `exp` claim
    3. Confirm no token revocation mechanism exists (logout does not invalidate token)
- **Expected (vulnerable):** Token remains valid well beyond a reasonable session window
- **Evidence:** `screenshots/a4-weak-session.png`

---

## A5 — Security Misconfiguration

### TC-A5-01: Verbose Error Messages
- **Steps:**
    1. Send a malformed request to trigger a 500 error (e.g., invalid JSON body)
    2. Observe full stack trace in response body
- **Expected (vulnerable):** Stack trace with class names and SQL details visible
- **Evidence:** `screenshots/a5-stacktrace.png`

### TC-A5-02: H2 Console Exposed
- **Steps:**
    1. Navigate to `http://localhost:8080/h2-console` without authentication
    2. Connect to the database
    3. Run `SELECT * FROM users` to dump all credentials
- **Expected (vulnerable):** H2 console accessible, full DB exposed
- **Evidence:** `screenshots/a5-h2-console.png`

### TC-A5-03: CORS Misconfiguration
- **Steps:**
    1. Send a request with `Origin: https://evil.com` header in Repeater
    2. Observe `Access-Control-Allow-Origin: *` and `Access-Control-Allow-Credentials: true` in response
- **Expected (vulnerable):** Both headers present simultaneously
- **Evidence:** `screenshots/a5-cors.png`

---

## A7 — Authentication Failures

### TC-A7-01: No Account Lockout
- **Steps:**
    1. Send 50 failed login attempts for `jsmith`
    2. Confirm account remains unlocked and accessible
- **Expected (vulnerable):** No lockout after unlimited attempts
- **Evidence:** `screenshots/a7-no-lockout.png`

### TC-A7-02: Weak Password Accepted
- **Endpoint:** `POST /api/auth/register`
- **Steps:**
    1. Register a new user with password `a` (1 character)
    2. Confirm registration succeeds
- **Expected (vulnerable):** `201 Created` with 1-character password
- **Evidence:** `screenshots/a7-weak-password.png`

### TC-A7-03: JWT Not Properly Validated
- **Steps:**
    1. Obtain a JWT for `jsmith` and decode it at [jwt.io](https://jwt.io)
    2. Manually backdate the `exp` claim to a past timestamp and re-sign with the weak secret
    3. Use the expired/tampered token on a protected endpoint
    4. Observe request succeeds
- **Expected (vulnerable):** `200 OK` with expired or tampered token
- **Evidence:** `screenshots/a7-jwt-validation.png`

---

## A8 — Software & Data Integrity Failures

### TC-A8-01: Missing Input Validation on Transactions
- **Endpoint:** `POST /api/transactions/transfer`
- **Steps:**
    1. Login as `jsmith`
    2. Submit transfer with `amount: -500`
    3. Observe funds move in reverse (from destination to source)
- **Expected (vulnerable):** `200 OK`, balance manipulation confirmed
- **Evidence:** `screenshots/a8-negative-transfer.png`

### TC-A8-02: Insecure Deserialization
- **Endpoint:** `POST /api/admin/import`
- **Steps:**
    1. Login as `admin`
    2. Generate a malicious serialized Java object using `ysoserial` with `CommonsCollections` gadget chain
    3. POST the payload as `application/octet-stream`
    4. Observe RCE or error confirming deserialization occurred
- **Expected (vulnerable):** Command executed or deserialization error confirming the vector
- **Evidence:** `screenshots/a8-deserial.png`

---

## A10 — Server-Side Request Forgery (SSRF)

### TC-A10-01: SSRF via Webhook URL
- **Endpoint:** `POST /api/accounts/{id}/webhook`
- **Steps:**
    1. Login as `jsmith`
    2. Submit `{"url": "http://169.254.169.254/latest/meta-data/"}` (AWS metadata endpoint)
    3. Submit `{"url": "http://localhost:8080/h2-console"}` (internal service)
    4. Observe server fetches the URL and returns content
- **Expected (vulnerable):** Internal content returned in response
- **Evidence:** `screenshots/a10-ssrf-metadata.png`, `screenshots/a10-ssrf-internal.png`

---

## Evidence Checklist

| Test Case | Screenshot                      | Status |
|-----------|---------------------------------|--------|
| TC-A1-01  | a1-idor-account.png             | 🔲     |
| TC-A1-02  | a1-idor-transaction.png         | 🔲     |
| TC-A1-03  | a1-bfla-admin.png               | 🔲     |
| TC-A1-04  | a1-unauth-transfer.png          | 🔲     |
| TC-A2-01  | a2-jwt-forge.png                | 🔲     |
| TC-A2-02  | a2-md5-password.png             | 🔲     |
| TC-A2-03  | a2-sensitive-logs.png           | 🔲     |
| TC-A3-01  | a3-sqli-search.png              | 🔲     |
| TC-A3-02  | a3-sqli-filter.png              | 🔲     |
| TC-A3-03  | a3-cmdi-export.png              | 🔲     |
| TC-A4-01  | a4-bruteforce.png               | 🔲     |
| TC-A4-02  | a4-predictable-accounts.png     | 🔲     |
| TC-A4-03  | a4-weak-session.png             | 🔲     |
| TC-A5-01  | a5-stacktrace.png               | 🔲     |
| TC-A5-02  | a5-h2-console.png               | 🔲     |
| TC-A5-03  | a5-cors.png                     | 🔲     |
| TC-A7-01  | a7-no-lockout.png               | 🔲     |
| TC-A7-02  | a7-weak-password.png            | 🔲     |
| TC-A7-03  | a7-jwt-validation.png           | 🔲     |
| TC-A8-01  | a8-negative-transfer.png        | 🔲     |
| TC-A8-02  | a8-deserial.png                 | 🔲     |
| TC-A10-01 | a10-ssrf-metadata.png           | 🔲     |