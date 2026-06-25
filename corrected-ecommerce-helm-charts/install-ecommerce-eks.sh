#!/usr/bin/env bash
set -euo pipefail

NS="ecommerce-devops"

helm upgrade --install ecommerce-common ./ecommerce-common
kubectl wait --for=condition=available deployment/postgres -n "$NS" --timeout=180s

helm upgrade --install user-service ./user-service -n "$NS"
helm upgrade --install product-service ./product-service -n "$NS"
helm upgrade --install cart-service ./cart-service -n "$NS"
helm upgrade --install inventory-service ./inventory-service -n "$NS"
helm upgrade --install payment-service ./payment-service -n "$NS"
helm upgrade --install notification-service ./notification-service -n "$NS"
helm upgrade --install order-service ./order-service -n "$NS"
helm upgrade --install api-gateway ./api-gateway -n "$NS"
helm upgrade --install frontend ./frontend -n "$NS"

kubectl get pods -n "$NS"
kubectl get svc -n "$NS"
