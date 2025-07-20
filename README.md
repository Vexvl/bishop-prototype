# Bishop Prototype

Bishop Prototype - это Spring Boot 3 приложение, демонстрирующее использование Synthetic Human Core Starter для обработки команд, аудита, очередей и метрик.

## Описание

Основные возможности:

- REST API для приёма команд;
- валидация входящих данных (description, priority, author, time);
- маппинг DTO → модель;
- исполнение команд через CommandService + CommandProcessor;
- аудит вызовов через `@WeylandWatchingYou` (лог + Kafka);
- централизованная обработка ошибок через `@RestControllerAdvice`;
- метрики через Spring Boot Actuator.

## Запуск

1. Убедиться, что Kafka работает (если audit.kafka-enabled=true).

2. Собрать и запустить приложение:

mvn clean package
mvn spring-boot:run

Приложение стартует на:

http://localhost:8081

## Конфигурация (application.properties)

server.port=8081
audit.kafka-enabled=true
audit.kafka-topic=audit-topic

spring.kafka.bootstrap-servers=localhost:9092

management.endpoints.web.exposure.include=*
management.endpoint.metrics.access=read_only
management.endpoint.prometheus.access=read_only
management.prometheus.metrics.export.enabled=true

## REST API

POST /commands

Пример запроса:

{
  "description": "Scan area",
  "priority": "COMMON",
  "author": "Bishop",
  "time": "2025-07-25T12:05:00"
}

curl пример:

curl -X POST http://localhost:8081/commands \
-H "Content-Type: application/json" \
-d '{"description":"Scan area","priority":"COMMON","author":"Bishop","time":"2025-07-25T12:05:00"}'

Возможные ответы:

- 200 OK - Command accepted
- 400 Bad Request - ошибка валидации или некорректный priority/time
- 429 Too Many Requests - очередь задач переполнена
- 500 Internal Server Error - внутренняя ошибка

## Ошибки

Общий формат ответа:

{
  "error": "ErrorType",
  "message": "Подробное сообщение"
}

Типы ошибок:

- ValidationError
- BadRequest
- QueueOverflow
- InternalError

## Метрики

Доступны через:

/actuator/metrics

Пример curl:

curl http://localhost:8081/actuator/metrics/command.queue.size

## Тесты

CommandControllerTest - тесты для:

- успешного запроса;
- ошибки валидации;
- ошибки IllegalArgumentException;
- переполнения очереди;
- внутренней ошибки.

## Основные классы

- CommandController - REST-контроллер;
- ExceptionsHandler - обработка ошибок;
- BishopPrototypeApplication - точка входа приложения.

## Сборка и тесты

Сборка:

mvn clean package

Запуск тестов:

mvn test

Java версия:
21

## Заключение

Bishop Prototype показывает пример интеграции Synthetic Human Core Starter в боевое приложение с полноценным REST API, аудитом, мониторингом и обработкой ошибок.
