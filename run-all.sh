#!/usr/bin/env bash

set -e

# Build shared DTO library first

mvn clean install -pl common-dto -am

# Run all microservices in parallel background processes

mvn spring-boot:run -pl transaction-gateway-service &

PID1=$!

mvn spring-boot:run -pl fraud-detection-engine &

PID2=$!

mvn spring-boot:run -pl fraud-audit-service &

PID3=$!

# Trap SIGINT (Ctrl+C) to gracefully shut down all background JVMs

trap "kill $PID1 $PID2 $PID3" SIGINT SIGTERM EXIT

wait
