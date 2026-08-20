#!/usr/bin/env bash
#
# Запуск MentorHub Backend локально одной командой.
#
#   ./run-local.sh
#
# Поднимает PostgreSQL в Docker, дожидается готовности и стартует приложение
# с профилем dev. Отправка почты выключена — коды подтверждения печатаются в лог.
#
# Переопределяется переменными окружения:
#   DB_PORT=5433   порт, на который прокидывается PostgreSQL (5432 часто занят)
#   APP_PORT=8080  порт приложения
#   MAIL_ENABLED=true  включить реальную отправку писем (нужны MAIL_USERNAME/MAIL_PASSWORD)

set -euo pipefail
cd "$(dirname "$0")"

DB_PORT="${DB_PORT:-5433}"
APP_PORT="${APP_PORT:-8080}"
MAIL_ENABLED="${MAIL_ENABLED:-false}"
CONTAINER_NAME="mentorhub-pg"

# ── JDK ────────────────────────────────────────────────────────────────────
# Нужен именно JDK: в системе часто стоит только JRE, и тогда javac отсутствует.
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/javac" ]; then
    for candidate in \
        "$HOME"/.jdks/* \
        /usr/lib/jvm/java-*-openjdk \
        "$HOME"/.local/share/JetBrains/Toolbox/apps/intellij-idea/jbr
    do
        if [ -x "$candidate/bin/javac" ]; then
            export JAVA_HOME="$candidate"
            break
        fi
    done
fi

if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/javac" ]; then
    echo "❌ Не найден JDK. Установите JDK 21+ или задайте JAVA_HOME." >&2
    exit 1
fi
echo "☕ JAVA_HOME=$JAVA_HOME"

# ── PostgreSQL ─────────────────────────────────────────────────────────────
# Существующий контейнер переиспользуем, только если он публикует нужный порт.
# Иначе приложение упрётся в «connection refused», а причина будет неочевидна.
if [ -n "$(docker ps -aq -f name="^${CONTAINER_NAME}$")" ]; then
    # || true обязателен: при отсутствии маппинга docker port возвращает ненулевой код,
    # а под set -e -o pipefail это молча убило бы скрипт.
    published="$(docker port "$CONTAINER_NAME" 5432/tcp 2>/dev/null | head -1 | sed 's/.*://' || true)"
    if [ "$published" != "$DB_PORT" ]; then
        echo "🧹 Контейнер ${CONTAINER_NAME} отдаёт порт '${published:-нет}', а нужен ${DB_PORT} — пересоздаю"
        docker rm -f "$CONTAINER_NAME" >/dev/null
    fi
fi

if [ -n "$(docker ps -aq -f name="^${CONTAINER_NAME}$")" ]; then
    if [ -z "$(docker ps -q -f name="^${CONTAINER_NAME}$")" ]; then
        echo "🐘 Запускаю существующий контейнер ${CONTAINER_NAME}"
        docker start "$CONTAINER_NAME" >/dev/null
    fi
else
    echo "🐘 Создаю контейнер ${CONTAINER_NAME} на порту ${DB_PORT}"
    # Именованный том: данные переживают пересоздание контейнера.
    # Сбросить базу начисто: docker rm -f mentorhub-pg && docker volume rm mentorhub-pgdata
    docker run -d --name "$CONTAINER_NAME" \
        -e POSTGRES_DB=mentorhub \
        -e POSTGRES_USER=mentorhub \
        -e POSTGRES_PASSWORD=mentorhub \
        -p "${DB_PORT}:5432" \
        -v mentorhub-pgdata:/var/lib/postgresql/data \
        postgres:16-alpine >/dev/null
fi

# Проверяем именно с хоста и через TCP — так же, как будет ходить приложение.
# pg_isready внутри контейнера ходит в unix-сокет и не заметит проблем с публикацией порта.
echo -n "⏳ Жду готовности базы"
db_ready=false
for _ in $(seq 1 40); do
    if (exec 3<>"/dev/tcp/localhost/${DB_PORT}") 2>/dev/null \
        && docker exec "$CONTAINER_NAME" pg_isready -U mentorhub -q 2>/dev/null; then
        db_ready=true
        echo " — готова"
        break
    fi
    echo -n "."
    sleep 1
done

if [ "$db_ready" != true ]; then
    echo ""
    echo "❌ База так и не ответила на localhost:${DB_PORT}." >&2
    echo "   Логи: docker logs ${CONTAINER_NAME}" >&2
    exit 1
fi

# ── Приложение ─────────────────────────────────────────────────────────────
echo "🚀 Стартую приложение на http://localhost:${APP_PORT}"
echo "   Swagger UI:  http://localhost:${APP_PORT}/swagger-ui"
echo "   Админ (dev): admin@mentorhub.local / Admin123!"
echo ""

exec ./mvnw -B spring-boot:run \
    -Dspring-boot.run.profiles=dev \
    -Dspring-boot.run.jvmArguments="-Dspring.datasource.url=jdbc:postgresql://localhost:${DB_PORT}/mentorhub -Dserver.port=${APP_PORT} -Dapp.mail.enabled=${MAIL_ENABLED}"
