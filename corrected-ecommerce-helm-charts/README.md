# Corrected E-Commerce Helm Charts for EKS

This version fixes the broken single-line YAML issue and creates working Helm charts for:

- `ecommerce-common`: Namespace, ConfigMap, Secret, PostgreSQL init ConfigMap, PVC, Deployment, Service
- `frontend`: Nginx frontend with a proxy on port `8080` to internal `api-gateway`
- `api-gateway` and all backend services as internal ClusterIP services

## Install order

```bash
cd ecommerce-helm-charts

helm upgrade --install ecommerce-common ./ecommerce-common
kubectl wait --for=condition=available deployment/postgres -n ecommerce-devops --timeout=180s

helm upgrade --install user-service ./user-service -n ecommerce-devops
helm upgrade --install product-service ./product-service -n ecommerce-devops
helm upgrade --install cart-service ./cart-service -n ecommerce-devops
helm upgrade --install inventory-service ./inventory-service -n ecommerce-devops
helm upgrade --install payment-service ./payment-service -n ecommerce-devops
helm upgrade --install notification-service ./notification-service -n ecommerce-devops
helm upgrade --install order-service ./order-service -n ecommerce-devops
helm upgrade --install api-gateway ./api-gateway -n ecommerce-devops
helm upgrade --install frontend ./frontend -n ecommerce-devops
```

## Verify

```bash
kubectl get pods -n ecommerce-devops
kubectl get svc -n ecommerce-devops
kubectl logs -n ecommerce-devops deployment/postgres
```

Open the frontend LoadBalancer DNS. The frontend Service exposes both `80` and `8080`. Port `8080` is handled by Nginx inside the frontend pod and proxied to the internal `api-gateway` service.

## Important PostgreSQL note

The database init script runs only when the PostgreSQL data directory is empty. If you already installed a broken Postgres once, delete the old PVC before reinstalling:

```bash
helm uninstall ecommerce-common -n ecommerce-devops || true
kubectl delete pvc postgres-pvc -n ecommerce-devops --ignore-not-found
```

Deleting the PVC deletes the database data.
