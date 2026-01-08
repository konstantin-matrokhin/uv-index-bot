
![Logo](assets/logo.jpg)

![Build](https://img.shields.io/github/actions/workflow/status/konstantin-matrokhin/uv-index-bot/build.yml?branch=main&label=build&style=flat)

# UV Index Bot for Telegram

**Bot link: [@uv_advisor_bot](https://t.me/uv_advisor_bot)**

This is a Telegram bot that sends UV index data and sun-safety recommendations. It showcases Spring Boot, Feign clients for weather/geocoding APIs, ChatGPT-powered advice, and Sentry.

## Key Features

- **I18n**: English and Russian texts live in dedicated profiles  (`application-ru.yml`, `application-en.yml`), served through the **I18nProperties** component.
- **Telegram bot framework**: built on top of [TelegramBots](https://github.com/rubenlagus/TelegramBots) with abilities, reply flows, and custom keyboards for an interactive UX.

## Developer Notes

- **Weather data** is fetched through an Feign client that calls the [Open-Meteo API](https://open-meteo.com/); geocoding relies on [Nominatim](https://nominatim.org/) to resolve city names.
- `ScheduledNotificationsService` polls locations once an hour, compares the latest UV index with the stored value, and pushes proactive updates when the delta crosses a threshold.
- [Outdated] ~~**ChatGPTService** wraps OpenAI's API, so you can experiment with prompt templates and tone directly in `application.yml`.~~
