# Weather Agent Demo

![Build](https://github.com/your-username/embabel01/actions/workflows/maven.yml/badge.svg)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) 
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) 
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)

A weather query agent built with the [Embabel framework](https://github.com/embabel/embabel-agent).

## Features

- Extracts city name from user input using LLM
- Retrieves geographic coordinates using OpenWeather Geocoding API
- Fetches weather data using OpenWeather Weather API
- Generates friendly weather responses using LLM

## Requirements

- Java 21+
- Maven 3.8+
- OpenWeather API Key (free tier available)
- DeepSeek API Key (for LLM)

## Quick Start

### 1. Set Environment Variables

```bash
export OPENWEATHER_API_KEY=your-openweather-api-key
export DEEPSEEK_API_KEY=your-deepseek-api-key
```

Or create a `.env` file:

```env
OPENWEATHER_API_KEY=your-openweather-api-key
DEEPSEEK_API_KEY=your-deepseek-api-key
```

### 2. Run the Application

```bash
./mvnw spring-boot:run
```

### 3. Use the Agent

When the shell starts, you can query weather:

```
x "What's the weather like in London?"
```

## Project Structure

```
embabel01/
├── src/main/java/com/example/embabel01/
│   ├── agent/
│   │   └── WeatherAgent.java      # Main weather agent
│   ├── config/
│   │   └── RestTemplateConfig.java # RestTemplate configuration
│   └── Embabel01Application.java   # Spring Boot entry point
├── src/main/resources/
│   └── application.yml             # Application configuration
├── src/test/                       # Unit and integration tests
└── pom.xml                         # Maven configuration
```

## Configuration

### application.yml

```yaml
spring:
  application:
    name: embabel-agent-app

embabel:
  models:
    default-llm: deepseek-chat
  agent:
    platform:
      models:
        deepseek:
          api-key: ${DEEPSEEK_API_KEY}

openweather:
  api:
    key: ${OPENWEATHER_API_KEY}
```

## Testing

### Run All Tests

```bash
./mvnw test
```

### Unit Tests

Unit tests use Embabel's `FakeOperationContext` to test agent actions without calling actual LLMs or APIs.

### Integration Tests

Integration tests test the complete agent workflow with mocked LLM responses.

## Technology Stack

- **Framework**: Spring Boot 3.5.13
- **Agent Framework**: Embabel 0.3.5
- **LLM Provider**: DeepSeek
- **Weather API**: OpenWeatherMap
- **HTTP Client**: Spring RestTemplate

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.
