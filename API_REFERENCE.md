# API Reference - Все эндпоинты и методы

## Аутентификация и пользователи

### AuthController (`/api/auth`)

#### Публичные методы (permitAll):

| Метод | Путь | Описание |
|-------|------|---------|
| **POST** | `/api/auth/register/student` | Регистрация студента |
| **POST** | `/api/auth/register/mentor` | Регистрация менторов (закрыто - 410) |
| **POST** | `/api/auth/verify-email` | Подтверждение email по коду |
| **POST** | `/api/auth/resend-verification` | Переотправить код подтверждения |
| **POST** | `/api/auth/login` | Логин (ограничено rate limit) |
| **POST** | `/api/auth/refresh` | Обновить access token |
| **POST** | `/api/auth/forgot-password` | Запросить сброс пароля (ограничено rate limit) |
| **POST** | `/api/auth/reset-password` | Сбросить пароль (ограничено rate limit) |

#### Приватные методы (требуют token):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/auth/me` | Получить данные текущего пользователя |
| **POST** | `/api/auth/logout` | Выход из аккаунта |

---

### UserController (`/api/users`)

#### Приватные методы (требуют token):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/users/me` | Получить профиль пользователя |

---

## Студенты

### StudentProfileController (`/api/student/profile`)

#### Приватные методы (требуют роль STUDENT):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/student/profile` | Получить профиль студента |
| **PUT** | `/api/student/profile` | Обновить профиль студента |

---

### StudentAvatarController (`/api/student/profile/avatar`)

#### Приватные методы (требуют роль STUDENT):

| Метод | Путь | Описание |
|-------|------|---------|
| **POST** | `/api/student/profile/avatar` | Загрузить аватар |
| **DELETE** | `/api/student/profile/avatar` | Удалить аватар |

---

### StudentBookingController (`/api/student/bookings`)

#### Приватные методы (требуют роль STUDENT):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/student/bookings` | Получить список занятий студента |
| **POST** | `/api/student/bookings` | Создать новое занятие (запись) |
| **PATCH** | `/api/student/bookings/{bookingId}/cancel` | Отменить занятие |

---

### StudentReviewController (`/api/student/reviews`)

#### Приватные методы (требуют роль STUDENT):

| Метод | Путь | Описание |
|-------|------|---------|
| **POST** | `/api/student/reviews` | Создать отзыв на менторра |

---

### StudentDashboardController (`/api/student/dashboard`)

#### Приватные методы (требуют роль STUDENT):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/student/dashboard` | Получить дашборд студента |

---

### StudentApplicationController (`/api/student/mentor-application`)

#### Приватные методы (требуют роль STUDENT):

| Метод | Путь | Описание |
|-------|------|---------|
| **POST** | `/api/student/mentor-application` | Подать заявку на менторство |
| **GET** | `/api/student/mentor-application` или `/api/student/mentor-application/me` | Получить статус своей заявки |

---

## Менторы

### MentorProfileController (`/api/mentor`)

#### Приватные методы (требуют роль MENTOR):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/mentor/profile` | Получить профиль менторра |
| **PUT** | `/api/mentor/profile` | Обновить профиль менторра |

---

### MentorAvatarController (`/api/mentor/profile/avatar`)

#### Приватные методы (требуют роль MENTOR):

| Метод | Путь | Описание |
|-------|------|---------|
| **POST** | `/api/mentor/profile/avatar` | Загрузить аватар менторра |
| **DELETE** | `/api/mentor/profile/avatar` | Удалить аватар менторра |

---

### MentorAvailabilitySlotController (`/api/mentor/availability-slots`)

#### Приватные методы (требуют роль MENTOR):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/mentor/availability-slots` | Получить слоты доступности |
| **POST** | `/api/mentor/availability-slots` | Создать новый слот |
| **PUT** | `/api/mentor/availability-slots/{slotId}` | Обновить слот |
| **PATCH** | `/api/mentor/availability-slots/{slotId}/deactivate` | Деактивировать слот |

---

### MentorBookingController (`/api/mentor/bookings`)

#### Приватные методы (требуют роль MENTOR):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/mentor/bookings` | Получить список занятий менторра |
| **PATCH** | `/api/mentor/bookings/{bookingId}/confirm` | Подтвердить занятие |
| **PATCH** | `/api/mentor/bookings/{bookingId}/decline` | Отклонить занятие |
| **PATCH** | `/api/mentor/bookings/{bookingId}/complete` | Завершить занятие |
| **PATCH** | `/api/mentor/bookings/{bookingId}/status` | Обновить статус занятия |

---

### MentorDashboardController (`/api/mentor/dashboard`)

#### Приватные методы (требуют роль MENTOR):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/mentor/dashboard` | Получить дашборд менторра |

---

### MentorStudentController (`/api/mentors`)

#### Приватные методы (требуют роль MENTOR):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/mentors/student-preview/{studentId}` | Получить превью студента |

---

### PublicMentorDirectoryController (`/api/public/mentors`)

#### Публичные методы (permitAll):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/public/mentors` | Получить справочник менторов (с фильтрацией) |
| **GET** | `/api/public/mentors/{mentorId}` | Получить профиль менторра |
| **GET** | `/api/public/mentors/{mentorId}/slots` | Получить доступные слоты менторра |

---

## Администраторы

### AdminUserController (`/api/admin/users`)

#### Приватные методы (требуют роль ADMIN):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/admin/users` | Получить список пользователей |
| **PATCH** | `/api/admin/users/{id}/status` | Изменить статус пользователя |
| **DELETE** | `/api/admin/users/{id}/avatar` | Удалить аватар пользователя |
| **GET** | `/api/admin/users/reviews` | Получить отзывы (для модерации) |
| **DELETE** | `/api/admin/users/reviews/{id}` | Удалить отзыв |

---

### AdminApplicationController (`/api/admin/mentor-applications`)

#### Приватные методы (требуют роль ADMIN):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/admin/mentor-applications` | Получить список заявок |
| **GET** | `/api/admin/mentor-applications/{id}` | Получить детали заявки |
| **POST** | `/api/admin/mentor-applications/{id}/approve` | Одобрить заявку |
| **POST** | `/api/admin/mentor-applications/{id}/reject` | Отклонить заявку |

---

### AdminDashboardController (`/api/admin/dashboard`)

#### Приватные методы (требуют роль ADMIN):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/admin/dashboard` | Получить админ дашборд |

---

### AdminController (`/api/admin`)

#### Приватные методы (требуют роль ADMIN):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/admin/stats` | Получить общую статистику |

---

## Отзывы

### PublicReviewController (`/api/public/mentors/{mentorId}/reviews`)

#### Публичные методы (permitAll):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/public/mentors/{mentorId}/reviews` | Получить отзывы на менторра |

---

## Уведомления

### NotificationController (`/api/notifications`)

#### Приватные методы (требуют token):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/api/notifications` | Получить уведомления |
| **GET** | `/api/notifications/unread-count` | Получить количество непрочитанных |
| **PATCH** | `/api/notifications/{id}/read` | Отметить уведомление как прочитанное |
| **PATCH** | `/api/notifications/read-all` | Отметить все как прочитанные |

---

## WebSocket

### WebSocketConfig (`/ws-stomp`)

#### Публичные методы (permitAll):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/ws-stomp` | WebSocket endpoint (STOMP) |

**Примечание**: WebSocket можно отключить в конфигурации:
```yaml
app:
  websocket:
    enabled: false
```

---

## Статическые файлы

### Local Storage (`/uploads`)

#### Публичные методы (permitAll):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/uploads/**` | Получить файл (аватары, документы) |

---

## Документация и утилиты

### Swagger/OpenAPI

#### Публичные методы (permitAll):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/swagger-ui/**` | Swagger UI документация |
| **GET** | `/v3/api-docs/**` | OpenAPI 3.0 документация |
| **GET** | `/api-docs/**` | Альтернативный путь документации |

### Health Check

#### Публичные методы (permitAll):

| Метод | Путь | Описание |
|-------|------|---------|
| **GET** | `/actuator/health` | Health check приложения |
| **GET** | `/actuator/health/**` | Детальный health check |

---

## Особенности безопасности

### Rate Limiting (ограничение частоты запросов)

Следующие эндпоинты ограничены 10 запросами в минуту (по умолчанию):
- `POST /api/auth/login`
- `POST /api/auth/register/student`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

**Ошибка**: `429 Too Many Requests`
**Заголовок**: `Retry-After: X` (сколько секунд ждать)

### CORS Политика

Разрешенные источники (по умолчанию):
- `http://localhost:3000`
- `https://jaimentorship.kutman.me`
- `https://app.kutman.me`
- `https://jaimentorship.kg`

Разрешенные методы: `GET, POST, PUT, PATCH, DELETE, OPTIONS`

### CSRF Protection

CSRF токены автоматически обрабатываются для:
- Исключены: `/api/auth/**`, `/api/public/**`
- Включены: Все остальные защищенные пути

---

## Коды ответов

| Код | Описание |
|-----|---------|
| **200** | OK - успешно |
| **201** | Created - ресурс создан |
| **204** | No Content - успешно, без содержимого |
| **400** | Bad Request - неправильные данные |
| **401** | Unauthorized - нужна аутентификация |
| **403** | Forbidden - недостаточно прав |
| **404** | Not Found - ресурс не найден |
| **405** | Method Not Allowed - неправильный метод |
| **409** | Conflict - конфликт данных |
| **410** | Gone - ресурс недоступен |
| **429** | Too Many Requests - лимит запросов |
| **500** | Internal Server Error - ошибка сервера |

---

## Примеры использования

### Авторизация через Cookies
```bash
# 1. Логин
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"email":"test@test.com","password":"pass"}'

# 2. Использование cookies в запросах
curl -X GET http://localhost:8080/api/student/profile \
  -b cookies.txt
```

### Авторизация через Bearer Token
```bash
# Используй token из ответа login
curl -X GET http://localhost:8080/api/student/profile \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### CORS Preflight Request
```bash
# Браузер автоматически отправляет OPTIONS перед запросом
curl -X OPTIONS http://localhost:8080/api/student/profile \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET"
```

---

## Смотри также:
- `DIAGNOSTICS.md` - диагностика ошибок 405 и 404
- `QUICK_FIX.md` - быстрое решение проблем
- `README.md` - документация проекта

