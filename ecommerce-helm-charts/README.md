# E-Commerce Separate Helm Charts

This folder contains 9 separate Helm charts for Argo CD:

- frontend
- api-gateway
- user-service
- product-service
- cart-service
- order-service
- payment-service
- inventory-service
- notification-service

Before installing these charts, apply your common manifests first:

```bash
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/configmap-secret.yml
kubectl apply -f k8s/postgres-db.yml
```

Install one chart manually:

```bash
helm install frontend ./frontend -n ecommerce-devops
helm install api-gateway ./api-gateway -n ecommerce-devops
```

For Argo CD, create one Application per chart path, for example:

```yaml
spec:
  source:
    repoURL: https://github.com/i-am-dheerajkadve/E-Micro-DEVSECOPS.git
    targetRevision: main
    path: helm/frontend
```

Put this `ecommerce-helm-charts` folder inside your repo as `helm/`.
