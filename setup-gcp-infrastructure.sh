#!/bin/bash

# MentorHub Backend - Google Cloud Infrastructure Setup
# Run this once to set up all necessary GCP resources

set -e

PROJECT_ID=$(gcloud config get-value project)
REGION="us-central1"
ENVIRONMENT="prod"
SERVICE_ACCOUNT_NAME="mentorhub-backend-sa"

echo "🔧 Setting up MentorHub Backend infrastructure on Google Cloud"
echo "Project: $PROJECT_ID"
echo "Region: $REGION"
echo ""

# 1. Create Service Account
echo "👤 Creating service account..."
gcloud iam service-accounts create "$SERVICE_ACCOUNT_NAME" \
  --display-name="MentorHub Backend Service Account" \
  --project="$PROJECT_ID" || echo "Service account already exists"

SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

# 2. Grant Cloud SQL Client role
echo "🔐 Granting Cloud SQL Client role..."
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
  --role="roles/cloudsql.client" \
  --quiet

# 3. Grant Cloud Storage access
echo "📦 Granting Cloud Storage access..."
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
  --role="roles/storage.objectAdmin" \
  --quiet

# 4. Grant Secret Manager access
echo "🔑 Granting Secret Manager access..."
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
  --role="roles/secretmanager.secretAccessor" \
  --quiet

# 5. Create GCS bucket for avatars if it doesn't exist
echo "🪣 Creating GCS bucket for avatars..."
BUCKET_NAME="${PROJECT_ID}-mentorhub-avatars"
gsutil mb -l "$REGION" -b on "gs://${BUCKET_NAME}" 2>/dev/null || echo "Bucket already exists"

# 6. Make bucket private but allow signed URLs
echo "🔒 Configuring bucket security..."
gsutil uniformbucketlevelaccess set on "gs://${BUCKET_NAME}"

# 7. Create secrets in Secret Manager
echo "🔐 Creating secrets in Secret Manager..."

# Helper function to create/update secrets
create_secret() {
  local secret_name=$1
  local secret_value=$2

  if gcloud secrets describe "$secret_name" --project="$PROJECT_ID" &>/dev/null; then
    echo "  ✓ Secret '$secret_name' already exists"
    # Optionally update with: echo -n "$secret_value" | gcloud secrets versions add "$secret_name" --data-file=-
  else
    echo -n "$secret_value" | gcloud secrets create "$secret_name" \
      --replication-policy="automatic" \
      --data-file=- \
      --project="$PROJECT_ID"
    echo "  ✓ Created secret '$secret_name'"
  fi
}

# Database secrets (update these with actual values)
create_secret "${ENVIRONMENT}-db-host" "YOUR_CLOUD_SQL_IP"
create_secret "${ENVIRONMENT}-db-port" "5432"
create_secret "${ENVIRONMENT}-db-name" "mentorhub"
create_secret "${ENVIRONMENT}-db-user" "postgres"
create_secret "${ENVIRONMENT}-db-password" "YOUR_DB_PASSWORD"

# JWT secret
create_secret "${ENVIRONMENT}-jwt-secret" "YOUR_JWT_SECRET_BASE64"

# Mail secrets
create_secret "${ENVIRONMENT}-mail-host" "smtp-relay.brevo.com"
create_secret "${ENVIRONMENT}-mail-port" "587"
create_secret "${ENVIRONMENT}-mail-username" "YOUR_MAIL_USERNAME"
create_secret "${ENVIRONMENT}-mail-password" "YOUR_MAIL_PASSWORD"
create_secret "${ENVIRONMENT}-mail-from" "noreply@mentorhub.app"

# GCS secrets
create_secret "${ENVIRONMENT}-gcs-bucket-name" "$BUCKET_NAME"
create_secret "${ENVIRONMENT}-gcs-public-base-url" "https://storage.googleapis.com/${BUCKET_NAME}"

# CORS
create_secret "${ENVIRONMENT}-cors-allowed-origins" "https://mentorhub.app,https://www.mentorhub.app"

# Cloud SQL instance
create_secret "${ENVIRONMENT}-cloud-sql-instance" "mentorhub-db-instance"

# 8. Grant service account access to secrets
echo "🔓 Granting service account access to secrets..."
for secret in $(gcloud secrets list --filter="labels.app=mentorhub" --format="value(name)"); do
  gcloud secrets add-iam-policy-binding "$secret" \
    --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
    --role="roles/secretmanager.secretAccessor" \
    --quiet 2>/dev/null || true
done

# Grant access to all prod secrets
gcloud secrets add-iam-policy-binding "${ENVIRONMENT}-db-host" \
  --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
  --role="roles/secretmanager.secretAccessor" \
  --quiet
gcloud secrets add-iam-policy-binding "${ENVIRONMENT}-db-password" \
  --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
  --role="roles/secretmanager.secretAccessor" \
  --quiet
gcloud secrets add-iam-policy-binding "${ENVIRONMENT}-jwt-secret" \
  --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
  --role="roles/secretmanager.secretAccessor" \
  --quiet

echo ""
echo "✅ Infrastructure setup complete!"
echo ""
echo "📋 Important next steps:"
echo "1. Update all secrets in Secret Manager with actual values:"
echo "   gcloud secrets versions add prod-db-host --data-file=- <<< 'YOUR_IP'"
echo ""
echo "2. Create Cloud SQL instance with name 'mentorhub-db-instance'"
echo ""
echo "3. Create database 'mentorhub' in Cloud SQL"
echo ""
echo "4. Configure Cloud SQL connection for Cloud Run"
echo ""
echo "5. Run deployment: ./deploy.sh prod mentorhub-backend"
echo ""
echo "Service Account: $SERVICE_ACCOUNT_EMAIL"
echo "GCS Bucket: gs://${BUCKET_NAME}"

