#!/bin/bash

# MentorHub Backend - Cloud Run Deployment Script
# Usage: ./deploy.sh [environment] [service-name]

set -e

ENVIRONMENT=${1:-prod}
SERVICE_NAME=${2:-mentorhub-backend}
PROJECT_ID=$(gcloud config get-value project)
REGION="us-central1"

echo "🚀 Deploying MentorHub Backend to Cloud Run"
echo "Environment: $ENVIRONMENT"
echo "Service: $SERVICE_NAME"
echo "Project: $PROJECT_ID"
echo "Region: $REGION"

# Load environment variables from Secret Manager
echo "📝 Loading secrets from Secret Manager..."

DB_HOST=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-db-host")
DB_PORT=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-db-port")
DB_NAME=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-db-name")
DB_USER=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-db-user")
DB_PASSWORD=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-db-password")
JWT_SECRET=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-jwt-secret")
MAIL_HOST=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-mail-host")
MAIL_PORT=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-mail-port")
MAIL_USERNAME=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-mail-username")
MAIL_PASSWORD=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-mail-password")
MAIL_FROM=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-mail-from")
GCS_BUCKET_NAME=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-gcs-bucket-name")
GCS_PUBLIC_BASE_URL=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-gcs-public-base-url")
CORS_ALLOWED_ORIGINS=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-cors-allowed-origins")
CLOUD_SQL_INSTANCE=$(gcloud secrets versions access latest --secret="${ENVIRONMENT}-cloud-sql-instance")

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t "gcr.io/${PROJECT_ID}/${SERVICE_NAME}:latest" .

# Push to Google Container Registry
echo "📤 Pushing image to GCR..."
docker push "gcr.io/${PROJECT_ID}/${SERVICE_NAME}:latest"

# Deploy to Cloud Run
echo "🌐 Deploying to Cloud Run..."
gcloud run deploy "$SERVICE_NAME" \
  --image "gcr.io/${PROJECT_ID}/${SERVICE_NAME}:latest" \
  --region "$REGION" \
  --platform managed \
  --memory 512Mi \
  --cpu 1 \
  --timeout 60 \
  --max-instances 100 \
  --allow-unauthenticated \
  --set-env-vars "DB_HOST=${DB_HOST},DB_PORT=${DB_PORT},DB_NAME=${DB_NAME},DB_USER=${DB_USER},DB_PASSWORD=${DB_PASSWORD},JWT_SECRET=${JWT_SECRET},MAIL_HOST=${MAIL_HOST},MAIL_PORT=${MAIL_PORT},MAIL_USERNAME=${MAIL_USERNAME},MAIL_PASSWORD=${MAIL_PASSWORD},MAIL_FROM=${MAIL_FROM},STORAGE_TYPE=gcs,GCS_BUCKET_NAME=${GCS_BUCKET_NAME},GCS_PUBLIC_BASE_URL=${GCS_PUBLIC_BASE_URL},CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS}" \
  --add-cloudsql-instances "${PROJECT_ID}:${REGION}:${CLOUD_SQL_INSTANCE}" \
  --service-account="mentorhub-backend-sa@${PROJECT_ID}.iam.gserviceaccount.com"

SERVICE_URL=$(gcloud run services describe "$SERVICE_NAME" --region "$REGION" --format 'value(status.url)')

echo "✅ Deployment successful!"
echo "Service URL: $SERVICE_URL"
echo ""
echo "📚 API Documentation: $SERVICE_URL/swagger-ui/index.html (if enabled)"
echo "💚 Health Check: $SERVICE_URL/actuator/health"

