#!/bin/sh
# Live-demo build (ADR-022): write the Auth0/Render runtime config before the Angular
# build bundles src/assets/config.json into dist/. The committed config.json keeps the
# localhost/Keycloak defaults for local dev.
set -e
cat > src/assets/config.json <<'EOF'
{
  "authority": "https://jie-ciam.us.auth0.com/",
  "clientId": "rpMrST6njiP9pBYhV2sJ69yZnShClETG",
  "audience": "https://api.ciam-prototype",
  "apiBaseUrl": "https://api-identity-jvm.onrender.com"
}
EOF
npm ci
npm run build