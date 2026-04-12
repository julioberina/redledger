# RedLedger STRIDE Threat Model

> 🚧 **This document is a placeholder and will be populated with detailed threat analysis.**

## Application Overview

RedLedger is a fintech REST API that handles user authentication, bank account management, and financial transactions. This threat model identifies potential security threats using the STRIDE methodology.

## System Architecture

```
[Client] --> [REST API (Spring Boot)] --> [H2 Database]
                    |
              [JWT Auth Layer]
```

## STRIDE Analysis

### Spoofing
| Threat | Component | Risk | Mitigation Status |
|--------|-----------|------|-------------------|
| JWT token forgery | Auth System | High | TBD |
| Credential stuffing | Login endpoint | Medium | TBD |
| Session hijacking | JWT tokens | High | TBD |

### Tampering
| Threat | Component | Risk | Mitigation Status |
|--------|-----------|------|-------------------|
| Transaction amount manipulation | Transfer API | Critical | TBD |
| Role escalation via API | Admin endpoints | High | TBD |
| Request parameter tampering | All endpoints | Medium | TBD |

### Repudiation
| Threat | Component | Risk | Mitigation Status |
|--------|-----------|------|-------------------|
| Unlogged financial transactions | Transaction Service | High | TBD |
| Missing audit trail | All services | Medium | TBD |

### Information Disclosure
| Threat | Component | Risk | Mitigation Status |
|--------|-----------|------|-------------------|
| IDOR on account details | Account API | High | TBD |
| Verbose error messages | Error handling | Medium | TBD |
| SQL injection data leak | Database queries | Critical | TBD |

### Denial of Service
| Threat | Component | Risk | Mitigation Status |
|--------|-----------|------|-------------------|
| Rate limiting absence | All endpoints | Medium | TBD |
| Resource exhaustion | Database | Low | TBD |

### Elevation of Privilege
| Threat | Component | Risk | Mitigation Status |
|--------|-----------|------|-------------------|
| BFLA - accessing admin endpoints | Admin API | Critical | TBD |
| Horizontal privilege escalation | Account API | High | TBD |
| Role manipulation | User management | High | TBD |

## Data Flow Diagrams

> TODO: Add detailed DFD for each major flow (authentication, transfers, admin operations)

## Evidence

Screenshots and proof-of-concept demonstrations will be stored in `docs/evidence/`.
