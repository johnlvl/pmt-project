#!/bin/sh
set -e
API_BASE_URL_ESC=$(printf %s "$API_BASE_URL" | sed 's/[&/]/\\&/g')
cat > /usr/share/nginx/html/assets/config/runtime-env.js <<EOF
(function(){
  window.RUNTIME_CONFIG = window.RUNTIME_CONFIG || {};
  window.RUNTIME_CONFIG.API_BASE_URL = "$API_BASE_URL_ESC";
})();
EOF
echo "Injected API_BASE_URL=$API_BASE_URL_ESC"
