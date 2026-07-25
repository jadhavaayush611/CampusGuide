#!/usr/bin/env bash
set -e

echo "=== CampusGuide Verification Script ==="

echo "--> Building Backend..."
cd backend
mvn clean verify
cd ..

if [ -d "frontend/node_modules" ]; then
  echo "--> Building Frontend..."
  cd frontend
  npm run build
  cd ..
fi

echo "=== Verification Succeeded ==="
