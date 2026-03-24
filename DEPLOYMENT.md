# MentorHub Backend - Production Deployment Guide

## Overview

MentorHub Backend is a Spring Boot 3.5 application designed for production deployment on Google Cloud Run with Cloud SQL and Google Cloud Storage.

### Key Features
- JWT-based authentication
- PostgreSQL with Flyway migrations
- Google Cloud Storage for file uploads (configurable to local storage)
- Email notifications via SMTP
- Multi-profile support (local, dev, prod)
- Production-ready security configuration
- Container-native deployment with Cloud Run

## Prerequisites

### Local Development
- Java 21 JDK
- Maven 3.8+
- Docker Desktop
- PostgreSQL 14+

### Production (Google Cloud)
- Google Cloud Project with billing enabled
- `gcloud` CLI configured
- Cloud SQL instance
- Cloud Storage bucket
- Secret Manager for secrets

## Local Development Setup

### 1. Database Setup
```bash
# Create PostgreSQL database
createdb mentorhub
psql mentorhub < schema.sql  # if available

# Or let Flyway handle migration at startup
```

### 2. Environment Variables (Local)
Create `.env` file:
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mentorhub
DB_USER=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_secret_base64
MAIL_USERNAME=your_mail@example.com
MAIL_PASSWORD=your_mail_password
MAIL_FROM=noreply@mentorhub.app
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
STORAGE_TYPE=local
```

### 3. Run Application
```bash
# Development profile (local storage)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Or build and run
mvn clean package
java -Dspring.profiles.active=dev -jar target/mentorhub-backend-*.jar
```

### 4. Access Application
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health: http://localhost:8080/actuator/health

## Production Deployment on Google Cloud Run

### 1. Initial GCP Infrastructure Setup

```bash
# Make setup script executable
chmod +x setup-gcp-infrastructure.sh

# Run infrastructure setup (one-time)
./setup-gcp-infrastructure.sh
```

This script will:
- Create service account
- Set up IAM roles
- Create GCS bucket
- Create Secret Manager secrets
- Configure permissions

### 2. Update Secrets in Secret Manager

After running setup script, update these secrets with actual values:

```bash
# Database secrets
gcloud secrets versions add prod-db-host --data-file=- <<< "YOUR_CLOUD_SQL_IP"
gcloud secrets versions add prod-db-password --data-file=- <<< "YOUR_DB_PASSWORD"

# JWT secret (generate: openssl rand -base64 32)
gcloud secrets versions add prod-jwt-secret --data-file=- <<< "YOUR_JWT_SECRET"

# Mail secrets
gcloud secrets versions add prod-mail-username --data-file=- <<< "YOUR_SMTP_USER"
gcloud secrets versions add prod-mail-password --data-file=- <<< "YOUR_SMTP_PASSWORD"

# CORS
gcloud secrets versions add prod-cors-allowed-origins --data-file=- <<< "https://mentorhub.app,https://www.mentorhub.app"
```

### 3. Deploy to Cloud Run

```bash
# Make deploy script executable
chmod +x deploy.sh

# Deploy
./deploy.sh prod mentorhub-backend

# Verify deployment
gcloud run services describe mentorhub-backend --region us-central1
```

### 4. Verify Deployment

```bash
# Get service URL
SERVICE_URL=$(gcloud run services describe mentorhub-backend --region us-central1 --format 'value(status.url)')

# Test health endpoint
curl $SERVICE_URL/actuator/health

# View logs
gcloud run logs read mentorhub-backend --region us-central1 --limit 50
```

## Storage Configuration

### Local Storage (Development)
Configured in `application.yml`:
```yaml
app:
  storage:
    type: local
    local:
      upload-dir: uploads
      public-base-url: http://localhost:8080
      avatar-max-size-bytes: 10485760
```

Files stored in: `./uploads/avatars/users/{userId}/{filename}`

### Google Cloud Storage (Production)
Configured in `application-prod.yml` via environment variables:
```yaml
app:
  storage:
    type: gcs  # Set via STORAGE_TYPE env var
    gcs:
      bucket-name: ${GCS_BUCKET_NAME}
      public-base-url: ${GCS_PUBLIC_BASE_URL}
      avatar-max-size-bytes: 10485760
```

Files stored in: `gs://{bucket-name}/avatars/users/{userId}/{filename}`

## Docker Build and Push

```bash
# Build Docker image locally
docker build -t gcr.io/YOUR_PROJECT_ID/mentorhub-backend:latest .

# Test locally
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=host.docker.internal \
  gcr.io/YOUR_PROJECT_ID/mentorhub-backend:latest

# Push to Google Container Registry
docker push gcr.io/YOUR_PROJECT_ID/mentorhub-backend:latest
```

## Environment Variables Reference

### Database
- `DB_HOST`: PostgreSQL host (Cloud SQL proxy address)
- `DB_PORT`: PostgreSQL port (default: 5432)
- `DB_NAME`: Database name (default: mentorhub)
- `DB_USER`: Database user
- `DB_PASSWORD`: Database password (from Secret Manager)

### JWT Authentication
- `JWT_SECRET`: Base64-encoded JWT signing key (from Secret Manager)

### Email / SMTP
- `MAIL_HOST`: SMTP server host
- `MAIL_PORT`: SMTP server port (default: 587)
- `MAIL_USERNAME`: SMTP username (from Secret Manager)
- `MAIL_PASSWORD`: SMTP password (from Secret Manager)
- `MAIL_FROM`: From email address
- `MAIL_ENABLED`: Enable/disable mail (default: true)

### CORS
- `CORS_ALLOWED_ORIGINS`: Comma-separated allowed origins (from Secret Manager)

### Storage
- `STORAGE_TYPE`: "local" or "gcs" (default: local)
- `GCS_BUCKET_NAME`: Google Cloud Storage bucket name (from Secret Manager)
- `GCS_PUBLIC_BASE_URL`: Public URL for accessing GCS objects (from Secret Manager)

### Other
- `SWAGGER_ENABLED`: Enable/disable Swagger UI (default: false in prod)

## Cloud Run Configuration

### Instance Settings
- **Memory**: 512 MB (configurable)
- **CPU**: 1 vCPU (configurable)
- **Timeout**: 60 seconds
- **Max Instances**: 100 (configurable)
- **Concurrency**: Default per Cloud Run

### Autoscaling
Cloud Run automatically scales based on traffic. Adjust `--max-instances` in deploy script as needed.

## Health Checks and Monitoring

### Health Endpoint
```bash
curl https://your-service.run.app/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### Metrics
Available at `/actuator/metrics` (protected endpoint)

### Logs
```bash
# View Cloud Run logs
gcloud run logs read mentorhub-backend --region us-central1

# Stream logs
gcloud run logs read mentorhub-backend --region us-central1 --follow
```

## Database Migrations

Flyway migrations are run automatically on application startup.

Migration files location: `src/main/resources/db/migration/`

To add new migration:
```sql
-- V{number}__{description}.sql
-- Example: V9__add_notifications_table.sql

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    -- columns here
);
```

## Security Best Practices

1. **Secrets Management**: Use Google Cloud Secret Manager, never hardcode
2. **HTTPS Only**: Cloud Run provides HTTPS by default
3. **CORS**: Configure allowed origins via environment variable
4. **JWT Secret**: Generate strong secret (min 32 bytes)
5. **Database**: Use Cloud SQL with private IP connection
6. **Storage**: Configure GCS bucket with appropriate access levels
7. **Service Account**: Use minimal IAM roles

## Troubleshooting

### Service fails to start
```bash
# Check logs
gcloud run logs read mentorhub-backend --region us-central1 --limit 100

# Verify environment variables are set
gcloud run services describe mentorhub-backend --region us-central1
```

### Database connection issues
```bash
# Verify Cloud SQL proxy connection
# Check that service account has cloudsql.client role
gcloud projects get-iam-policy PROJECT_ID
```

### File upload issues
```bash
# Verify GCS bucket permissions
gsutil iam ch serviceAccount:SERVICE_ACCOUNT_EMAIL:objectAdmin gs://BUCKET_NAME

# Check GCS bucket exists
gsutil ls -b gs://BUCKET_NAME
```

## Performance Tuning

### Database Connection Pool
Adjust in `application-prod.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10  # Adjust based on load
      minimum-idle: 2
```

### Cloud Run Memory/CPU
Increase via deploy script:
```bash
gcloud run deploy mentorhub-backend \
  --memory 1Gi \  # Increase from 512Mi
  --cpu 2         # Increase from 1
```

## Rollback Procedure

```bash
# View recent revisions
gcloud run revisions list --service=mentorhub-backend --region=us-central1

# Route traffic to specific revision
gcloud run services update-traffic mentorhub-backend \
  --region us-central1 \
  --to-revisions REVISION_NAME=100

# Or update deploy to previous working image version
```

## Support and Documentation

- Spring Boot Docs: https://spring.io/projects/spring-boot
- Cloud Run Docs: https://cloud.google.com/run/docs
- Cloud SQL Docs: https://cloud.google.com/sql/docs
- Google Cloud Storage Docs: https://cloud.google.com/storage/docs

