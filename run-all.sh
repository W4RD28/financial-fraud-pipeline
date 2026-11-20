#!/usr/bin/env bash
set -euo pipefail

# 1. Auto-format the entire multi-module workspace with Spotless and install common-dto
echo "==> Applying Spotless formatting & installing common-dto..."
mvn spotless:apply
mvn install -pl common-dto -DskipTests -Dspotless.check.skip=true

# 2. Start all 4 microservices in parallel (skipping redundant spotless:check on boot)
echo "==> Starting microservices..."
mvn spring-boot:run -pl transaction-gateway-service -Dspotless.check.skip=true &
PID1=$!

mvn spring-boot:run -pl fraud-detection-engine -Dspotless.check.skip=true &
PID2=$!

mvn spring-boot:run -pl fraud-audit-service -Dspotless.check.skip=true &
PID3=$!

mvn spring-boot:run -pl api-gateway -Dspotless.check.skip=true &
PID4=$!

cleanup() {
  echo ""
  echo "==> Shutting down all microservices..."
  # Kill child JVM processes spawned by mvn spring-boot:run as well as the Maven wrappers
  for pid in $PID1 $PID2 $PID3 $PID4; do
    pkill -P "$pid" 2>/dev/null || true
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
}

trap cleanup SIGINT SIGTERM EXIT

wait
