# Security Policy

## Supported Versions

We actively support and provide security updates for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 0.0.x   | :white_check_mark: |

## Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security vulnerability in this project, please report it responsibly.

### How to Report

**Please do NOT create a public GitHub issue for security vulnerabilities.**

Instead, please report security vulnerabilities by:

1. **Email**: Send details to [your-email@example.com] (replace with your actual email)
2. **GitHub Security Advisory**: Use GitHub's [private vulnerability reporting](https://github.com/your-username/uv-index-tg-bot/security/advisories/new)

### What to Include

Please include the following information in your report:

- Description of the vulnerability
- Steps to reproduce the issue
- Potential impact and severity
- Any suggested fixes or mitigations
- Your contact information

### Response Timeline

- **Initial Response**: Within 48 hours
- **Assessment**: Within 1 week
- **Fix Development**: Depends on severity and complexity
- **Public Disclosure**: After fix is released (coordinated disclosure)

## Security Best Practices for Users

### Environment Variables
- **Never commit** `.env` files or configuration with secrets
- Use strong, unique passwords for database and services
- Rotate API keys and tokens regularly
- Use different credentials for development and production

### Database Security
- Use strong passwords for PostgreSQL
- Restrict database access to necessary IP addresses only
- Enable SSL/TLS for database connections in production
- Regular security updates for PostgreSQL

### Telegram Bot Security
- Keep your bot token secret and secure
- Use HTTPS webhooks in production (not polling)
- Validate all user inputs
- Implement rate limiting to prevent abuse
- Monitor bot usage for suspicious activity

### Production Deployment
- Use HTTPS for all external communications
- Keep Java runtime and dependencies updated
- Use Docker security best practices:
  - Non-root user in containers
  - Minimal base images
  - Regular image updates
- Implement proper logging and monitoring
- Use secrets management systems (not environment variables in production)

### OpenAI Integration (if enabled)
- Secure storage of OpenAI API keys
- Monitor API usage and costs
- Implement proper error handling
- Be aware of data privacy implications

## Dependency Security

### Automated Dependency Scanning
We recommend using tools like:
- GitHub Dependabot
- OWASP Dependency Check
- Snyk
- Gradle dependency vulnerability scanning

### Manual Review
- Regularly review and update dependencies
- Monitor security advisories for used libraries
- Remove unused dependencies

## Known Security Considerations

### Data Privacy
- User location data is stored and processed
- Consider GDPR/privacy law compliance
- Implement data retention policies
- Provide user data deletion mechanisms

### Rate Limiting
- Implement rate limiting for API calls
- Protect against Telegram API rate limits
- Monitor for suspicious patterns

### Input Validation
- All user inputs are validated
- Location coordinates are bounded
- Text messages are sanitized

## Security Configuration Checklist

### Development Environment
- [ ] Use `.env` file for local secrets (never commit)
- [ ] Use example configurations for sharing
- [ ] Enable debug logging for security events
- [ ] Use local database with non-production data

### Production Environment
- [ ] All secrets in secure environment variables
- [ ] Database connections use SSL
- [ ] Application runs with minimal privileges
- [ ] Logging configured for security monitoring
- [ ] Regular security updates applied
- [ ] Network access properly restricted
- [ ] HTTPS enabled for all external communications

## Incident Response

In case of a security incident:

1. **Immediate Actions**:
   - Isolate affected systems
   - Preserve logs and evidence
   - Assess the scope of the breach

2. **Communication**:
   - Notify maintainers immediately
   - Prepare user communication if needed
   - Coordinate with security researchers

3. **Recovery**:
   - Apply security patches
   - Update credentials if compromised
   - Monitor for further issues

4. **Post-Incident**:
   - Conduct security review
   - Update security practices
   - Document lessons learned

## Contact

For security-related questions or concerns:
- Security Email: [your-security-email@example.com]
- Maintainer: [your-github-username]

## Acknowledgments

We appreciate the security research community and will acknowledge researchers who responsibly disclose vulnerabilities (unless they prefer to remain anonymous).

---

**Last Updated**: January 2025

This security policy is subject to updates. Please check this document regularly for the latest security guidelines.