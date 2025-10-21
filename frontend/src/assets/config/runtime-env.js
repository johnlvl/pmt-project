(function(){
  window.RUNTIME_CONFIG = window.RUNTIME_CONFIG || {};
  // Default to /api; overridden via Nginx env-injected file at runtime
  window.RUNTIME_CONFIG.API_BASE_URL = window.RUNTIME_CONFIG.API_BASE_URL || (window.__API_BASE_URL__ || '/api');
})();
