# UV Index Telegram Bot

A Spring Boot application that provides UV index information and sun protection recommendations through a Telegram bot. The bot supports multiple languages and can send scheduled notifications to help users stay protected from harmful UV radiation.

## Features

- **UV Index Information**: Get current UV index data for any location
- **Sun Protection Recommendations**: Personalized advice on SPF, clothing, and sun safety
- **Multi-language Support**: Available in English and Russian
- **Location Services**: Geocoding support for location-based weather data
- **Scheduled Notifications**: Automatic reminders and updates
- **User Management**: Registration and subscription management
- **Admin Features**: Bot administration and statistics

## Tech Stack

- **Java 17**
- **Spring Boot 3.3.0**
- **PostgreSQL** - Database
- **Telegram Bot API** - Bot framework
- **Liquibase** - Database migrations
- **OpenFeign** - HTTP client for external APIs
- **Lombok** - Code generation
- **Docker** - Containerization

## Prerequisites

- Java 17 or higher
- PostgreSQL database
- Telegram Bot Token (from [@BotFather](https://t.me/botfather))
- Optional: OpenAI API key for enhanced recommendations

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd uv-index-tg-bot
```

### 2. Database Setup

Start PostgreSQL using Docker Compose:

```bash
cd devops
docker-compose up -d postgres
```

This will create a PostgreSQL instance. Configure your database settings in the `.env` file as described in the next step.

### 3. Environment Variables

Copy the example environment file and configure your settings:

```bash
cp .env.example .env
```

Edit `.env` file with your actual values:

```bash
# Required
TELEGRAM_TOKEN=your_telegram_bot_token_from_botfather
POSTGRES_PASSWORD=your_secure_database_password

# Optional
OPENAI_APIKEY=your_openai_api_key
```

**⚠️ Security Note**: Never commit the `.env` file to version control. It contains sensitive credentials.

### 4. Build and Run

#### Using Gradle

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

#### Using Docker

```bash
# Build the JAR file
./gradlew build

# Build Docker image
docker build -t uv-index-bot .

# Run the container
docker run -d \
  --name uv-index-bot \
  --env-file .env \
  --network host \
  uv-index-bot
```

### 5. Database Migration

Database migrations will run automatically on startup using Liquibase. The following tables will be created:
- `users` - User information and preferences
- `locations` - Location data and coordinates
- `channels` - Notification channels

## Configuration

### Application Properties

The bot can be configured through `application.yml`:

```yaml
telegram:
  token: ${TELEGRAM_TOKEN}

openai:
  enabled: false  # Set to true to enable AI recommendations
  model: gpt-3.5-turbo
  key: ${OPENAI_APIKEY}

spring:
  datasource:
    password: ${POSTGRES_PASSWORD}
  profiles:
    active: en,ru  # Supported languages
```

### Language Support

- English (`en`)
- Russian (`ru`)

Language-specific configurations are in:
- `application-en.yml`
- `application-ru.yml`

## Bot Commands

The bot supports various commands for interacting with users:

- Location sharing for UV index information
- Settings management
- Language preferences
- Subscription management

## Development

### Project Structure

```
src/main/java/com/kmatrokhin/uvbot/
├── config/          # Spring configuration
├── dto/             # Data transfer objects
├── entities/        # JPA entities
├── events/          # Application events
├── repositories/    # Data repositories
├── services/        # Business logic
└── telegram/        # Telegram bot abilities
```

### Running Tests

```bash
./gradlew test
```

### Code Style

This project uses Lombok for reducing boilerplate code. Make sure your IDE has Lombok plugin installed.

## Deployment

### Production Environment

1. Set profile to production:
   ```yaml
   spring:
     profiles:
       active: prod,en,ru
   ```

2. Configure production database in `application-prod.yml`

3. Use the provided Docker setup for containerized deployment

### Using Portainer

A `portainer.yml` file is provided for easy deployment with Portainer.

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines on how to contribute to this project.

Quick start:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Security

Security is important to us. Please see [SECURITY.md](SECURITY.md) for information on:
- Reporting security vulnerabilities
- Security best practices
- Configuration guidelines

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues and questions, please create an issue in the repository.