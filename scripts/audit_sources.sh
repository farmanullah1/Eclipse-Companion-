#!/usr/bin/env bash
# Run from the project root: bash scripts/audit_sources.sh
set -euo pipefail
PATTERN='android\.permission\.INTERNET|okhttp|retrofit|volley|crashlytics|admob|appsflyer|mixpanel|sentry|java\.net\.URL|HttpURLConnection|HttpsURLConnection|DexClassLoader|PathClassLoader|WebView'
if grep -RIEn "$PATTERN" app/src/main \
     --include='*.kt' --include='*.kts' --include='*.xml' --include='*.json'; then
  echo "FAIL: network, analytics, ads or dynamic-code reference found (see above)"; exit 1
fi
echo "OK: no network, analytics, ads or dynamic-code references found"
