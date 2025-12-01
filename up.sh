#!/bin/bash
set -e

echo "Starting project containers..."
docker-compose --env-file .env up -d --build