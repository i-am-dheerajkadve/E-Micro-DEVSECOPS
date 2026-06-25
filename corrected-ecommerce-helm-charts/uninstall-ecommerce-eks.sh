#!/usr/bin/env bash
set -euo pipefail
NS="ecommerce-devops"

for r in frontend api-gateway order-service notification-service payment-service inventory-service cart-service product-service user-service ecommerce-common; do
  helm uninstall "$r" -n "$NS" 2>/dev/null || helm uninstall "$r" 2>/dev/null || true
done

echo "If you also want to delete PostgreSQL data, run:"
echo "kubectl delete pvc postgres-pvc -n $NS --ignore-not-found"
