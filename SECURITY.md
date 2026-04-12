# Security Policy

## ⚠️ Important Disclaimer

RedLedger is a **deliberately vulnerable** application built for educational and portfolio purposes. It contains intentional security flaws to demonstrate AppSec concepts.

**DO NOT:**
- Deploy this application in a production environment
- Expose this application to the public internet
- Use this application to handle real financial data
- Use the code patterns in this project as templates for production applications

## Reporting Vulnerabilities

Since this application is intentionally vulnerable, we distinguish between:

### Intentional Vulnerabilities
These are documented in the [README.md](README.md) Known Vulnerabilities section and are part of the educational design.

### Unintentional Vulnerabilities
If you discover a vulnerability that is **not** part of the intended educational design (e.g., in the build pipeline, CI/CD configuration, or infrastructure), please report it by opening an issue.

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.x.x   | ✅ (Educational) |

## Security Tools Used

- **SAST**: Semgrep
- **SCA**: Snyk
- **DAST**: Burp Suite (manual)
- **Threat Modeling**: STRIDE framework
