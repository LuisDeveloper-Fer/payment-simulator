#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE:-http://localhost:8080}"
curl -i -X POST "$BASE/api/payments" -H 'Content-Type: application/json' -H 'Idempotency-Key: payment-demo-001' --data '{"amount":125.5,"currency":"PEN","scenario":"LOST_RESPONSE"}'
curl -i "$BASE/api/payments"
curl -i "$BASE/api/payments/payment-demo-001"
curl -i -X POST "$BASE/api/payments/payment-demo-001/reversals"
