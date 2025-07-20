# Contributing to UV Index Telegram Bot

Thank you for your interest in contributing to the UV Index Telegram Bot! We welcome contributions from the community.

## How to Contribute

### 1. Fork the Repository

Fork the project on GitHub and clone your fork locally:

```bash
git clone https://github.com/your-username/uv-index-tg-bot.git
cd uv-index-tg-bot
```

### 2. Set Up Development Environment

1. **Prerequisites**: Ensure you have Java 17+ and PostgreSQL installed
2. **Environment Setup**: Copy `.env.example` to `.env` and fill in your credentials
3. **Database Setup**: Run PostgreSQL using Docker Compose:
   ```bash
   cd devops
   cp compose.example.yaml compose.yaml
   # Edit compose.yaml with your settings
   docker-compose up -d postgres
   ```
4. **Build**: Run `./gradlew build` to ensure everything compiles

### 3. Create a Branch

Create a branch for your feature or bugfix:

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/issue-description
```

### 4. Make Changes

- Follow existing code style and conventions
- Use Lombok annotations where appropriate
- Add tests for new functionality
- Update documentation if needed

### 5. Test Your Changes

Run tests before submitting:

```bash
./gradlew test
```

### 6. Commit Your Changes

Write clear, descriptive commit messages:

```bash
git add .
git commit -m "Add feature: brief description of what you added"
```

### 7. Submit a Pull Request

1. Push your branch to your fork
2. Create a Pull Request on GitHub
3. Provide a clear description of your changes
4. Reference any related issues

## Code Style Guidelines

### Java Code Style
- Use 4 spaces for indentation
- Follow standard Java naming conventions
- Use Lombok to reduce boilerplate code
- Add JavaDoc comments for public methods
- Keep methods focused and single-purpose

### Configuration
- Use environment variables for all secrets and configuration
- Never commit hardcoded passwords or API keys
- Provide example configuration files

### Database Changes
- Create Liquibase migrations for schema changes
- Test migrations both up and down
- Document any data migration requirements

## What to Contribute

### 🐛 Bug Reports
- Use GitHub Issues with bug report template
- Include steps to reproduce
- Provide environment details

### ✨ Feature Requests
- Open an issue first to discuss the feature
- Explain the use case and benefits
- Consider backward compatibility

### 📝 Documentation
- Improve README or code documentation
- Add code comments for complex logic
- Update configuration examples

### 🧪 Tests
- Add unit tests for business logic
- Integration tests for external APIs
- Improve test coverage

## Development Guidelines

### Environment Variables
Always use environment variables for:
- Database credentials
- API keys and tokens
- External service URLs
- Feature flags

### Error Handling
- Use appropriate exception types
- Log errors with context
- Provide user-friendly error messages
- Handle rate limiting gracefully

### Internationalization
- Add new strings to both `application-en.yml` and `application-ru.yml`
- Use descriptive keys for i18n properties
- Test with both languages

### Security
- Never log sensitive information
- Validate all user inputs
- Use parameterized queries
- Keep dependencies updated

## Pull Request Process

1. **Review Checklist**:
   - [ ] Code compiles without warnings
   - [ ] Tests pass
   - [ ] No hardcoded secrets
   - [ ] Documentation updated
   - [ ] Follows code style guidelines

2. **Review Process**:
   - At least one maintainer review required
   - Address all review comments
   - Keep PR scope focused

3. **Merge Requirements**:
   - All tests must pass
   - No merge conflicts
   - Approved by maintainer

## Getting Help

- 💬 **Questions**: Open a GitHub Discussion
- 🐛 **Bugs**: Create a GitHub Issue
- 💡 **Ideas**: Start with a GitHub Discussion

## Code of Conduct

Be respectful, inclusive, and constructive in all interactions. We follow the [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/version/2/1/code_of_conduct/).

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

Thank you for contributing! 🎉