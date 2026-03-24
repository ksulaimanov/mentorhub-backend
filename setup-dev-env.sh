#!/bin/bash

# MentorHub Backend - Local Development Setup
# Run this once to set up local development environment

set -e

echo "🛠️ Setting up MentorHub Backend for local development"
echo ""

# 1. Check prerequisites
echo "✓ Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "❌ Java 21 is required. Please install it."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[^"]*')
echo "  Java version: $JAVA_VERSION"

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is required. Please install it."
    exit 1
fi

echo "  Maven installed"

if ! command -v psql &> /dev/null; then
    echo "⚠️  PostgreSQL client not found. Make sure PostgreSQL server is running."
else
    echo "  PostgreSQL client installed"
fi

# 2. Create database
echo ""
echo "🗄️ Setting up PostgreSQL database..."

if psql -U postgres -c "SELECT 1 FROM pg_database WHERE datname = 'mentorhub'" | grep -q 1; then
    echo "  Database 'mentorhub' already exists"
else
    psql -U postgres -c "CREATE DATABASE mentorhub;"
    echo "  Created database 'mentorhub'"
fi

# 3. Build project
echo ""
echo "🔨 Building project..."
mvn clean package -DskipTests -q
echo "  Build complete"

# 4. Create .env file if not exists
echo ""
echo "📝 Setting up environment variables..."

if [ ! -f .env ]; then
    cat > .env << 'EOF'
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mentorhub
DB_USER=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=c3VwZXItc2VjdXJlLXRlc3Qta2V5LXN1cGVyLXNlY3VyZS10ZXN0LWtleQ==

# Mail (optional for development)
MAIL_USERNAME=test@example.com
MAIL_PASSWORD=test_password
MAIL_FROM=noreply@mentorhub.local

# Storage
STORAGE_TYPE=local

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# Swagger
SWAGGER_ENABLED=true
EOF
    echo "  Created .env file (update with your credentials)"
else
    echo "  .env file already exists"
fi

# 5. Show next steps
echo ""
echo "✅ Setup complete! Next steps:"
echo ""
echo "1. Start PostgreSQL (if not running):"
echo "   # On macOS:"
echo "   brew services start postgresql"
echo ""
echo "   # On Linux:"
echo "   sudo systemctl start postgresql"
echo ""
echo "2. Run the application:"
echo "   mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=dev'"
echo ""
echo "3. Access the application:"
echo "   API: http://localhost:8080"
echo "   Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "   Health: http://localhost:8080/actuator/health"
echo ""

