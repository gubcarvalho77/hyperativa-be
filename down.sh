#!/bin/bash
set -e

echo "Stoping project containers and volumes..."
docker-compose down -v --remove-orphans
