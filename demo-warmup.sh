#!/bin/sh
# Warm up the Render free-tier backends before a demo (ADR-022).
#
# Render free web services sleep after ~15 min of inactivity; the first request then
# triggers a ~50s cold start. Run this a minute before demoing, then demonstrate within
# ~15 minutes (the SPA is a static site and doesn't sleep).
#
# Usage: sh demo-warmup.sh

set -u

JVM="https://api-identity-jvm.onrender.com"
PYTHON="https://api-identity-python.onrender.com"
BFF="https://api-auth-ndva.onrender.com"

echo "Waking backends (cold start ~50s if asleep)..."
curl -s -o /dev/null --max-time 120 "$JVM/health" &
curl -s -o /dev/null --max-time 120 "$PYTHON/health" &
curl -s -o /dev/null --max-time 120 "$BFF/actuator/health" &
wait

echo
echo "Status:"
printf '  JVM    /health           -> '; curl -s --max-time 15 "$JVM/health"; echo
printf '  Python /health           -> '; curl -s --max-time 15 "$PYTHON/health"; echo
printf '  BFF    /actuator/health  -> '; curl -s --max-time 15 "$BFF/actuator/health"; echo
echo
echo "Demo URLs:"
echo "  SPA: https://ui-identity.onrender.com"
echo "  BFF: $BFF/oauth2/authorization/ciam"