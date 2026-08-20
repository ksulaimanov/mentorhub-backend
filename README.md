# MentorHub Backend

REST API платформы менторства MentorHub / JaiMentorship: аккаунты и роли, профили менторов
и студентов, заявки на менторство, слоты доступности, бронирования, отзывы и уведомления.

Фронтенд живёт в отдельном репозитории — [metorhub-frontend](https://github.com/ksulaimanov/metorhub-frontend).

## Стек

| | |
|---|---|
| Язык / рантайм | Java 21 |
| Фреймворк | Spring Boot 3.5 (Web, Security, Data JPA, Validation, Mail, Thymeleaf, WebSocket, Actuator) |
| БД | PostgreSQL + Flyway (миграции в `src/main/resources/db/migration`) |
| Аутентификация | JWT (jjwt) в httpOnly cookie + refresh-токены с ротацией |
| Хранилище файлов | Google Cloud Storage либо локальный диск (`app.storage.type`) |
| Почта | SMTP, шаблоны Thymeleaf в `src/main/resources/templates/mail` |
| Документация API | springdoc-openapi → `/swagger-ui` |
| Сборка | Maven (через `./mvnw`) |

## Модули

Код разложен по доменным пакетам в `kg.kut.os.mentorhub`:

| Пакет | Ответственность |
|---|---|
| `auth` | регистрация, вход, подтверждение email, сброс пароля, refresh-токены, роли |
| `student` / `mentor` | профили и аватары |
| `application` | заявки студентов на роль ментора и их модерация |
| `availability` | слоты доступности ментора |
| `booking` | бронирование занятий |
| `review` | отзывы о менторах |
| `notification` | in-app уведомления и отправка писем |
| `dashboard` | сводки для студента, ментора и админа |
| `admin` | администрирование пользователей |
| `common` | security, конфигурация, обработка ошибок, общие DTO |

## Локальный запуск

Нужны **JDK 21+** и **Docker**. Maven ставить не нужно — в репозитории есть wrapper.

```bash
./run-local.sh
```

Скрипт находит JDK, поднимает PostgreSQL в Docker, дожидается готовности базы
и стартует приложение с профилем `dev`. Отправка почты выключена — коды
подтверждения печатаются в лог.

Приложение слушает `http://localhost:8080`, Swagger UI — `http://localhost:8080/swagger-ui`.
Профиль `dev` создаёт админа `admin@mentorhub.local` / `Admin123!`.

Настраивается переменными окружения:

| Переменная | По умолчанию | Зачем |
|---|---|---|
| `DB_PORT` | `5433` | порт PostgreSQL на хосте (5432 часто занят другим проектом) |
| `APP_PORT` | `8080` | порт приложения |
| `MAIL_ENABLED` | `false` | `true` включает реальную отправку (нужны `MAIL_USERNAME` / `MAIL_PASSWORD`) |

Сбросить базу начисто:

```bash
docker rm -f mentorhub-pg && docker volume rm mentorhub-pgdata
```

### Запуск вручную

```bash
docker compose -f docker-compose.dev.yml up -d postgres   # база на 5432
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Тесты

```bash
./mvnw test
```

Интеграционные тесты используют Testcontainers, поэтому нужен запущенный Docker.

## Профили и конфигурация

| Профиль | Файл | Назначение |
|---|---|---|
| (по умолчанию) | `application.yml` | локальная разработка, значения захардкожены |
| `dev` | `application-dev.yml` | + сид админского аккаунта |
| `prod` | `application-prod.yml` | всё через переменные окружения, Cloud SQL, GCS |

Ключевые переменные окружения для `prod` перечислены в [DEPLOYMENT.md](DEPLOYMENT.md).

> **Важно:** значение `app.jwt.secret` по умолчанию — общеизвестный тестовый ключ,
> лежащий в открытом репозитории. Он годится только для локальной работы. Любой стенд
> обязан задавать `JWT_SECRET` (в `prod` — из Secret Manager), иначе кто угодно сможет
> подписать валидный токен.

## Сборка образа

```bash
docker build -t mentorhub-backend .
```

Multi-stage сборка: `eclipse-temurin:21-jdk-alpine` собирает jar через `./mvnw`,
рантайм-слой — `21-jre-alpine`, запуск от непривилегированного пользователя,
профиль `prod` зашит в `ENTRYPOINT`.

## Документация

- [API_REFERENCE.md](API_REFERENCE.md) — полный список эндпоинтов с правами доступа
- [DEPLOYMENT.md](DEPLOYMENT.md) — деплой на Google Cloud Run + Cloud SQL + GCS
