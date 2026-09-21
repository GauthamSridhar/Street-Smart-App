#!/bin/sh
set -eu

# This is a browser-restricted key, not a backend secret. Keep it out of source
# control and supply it through the ignored root .env file at container startup.
sed "s|__GOOGLE_MAPS_API_KEY__|${GOOGLE_MAPS_API_KEY:-}|g" \
  /srv/config.template.js > /srv/config.js

exec "$@"
