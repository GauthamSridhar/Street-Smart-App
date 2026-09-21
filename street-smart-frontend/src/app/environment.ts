// Public deployment configuration. Never put backend secrets in this object.
const runtime = (
  window as Window & { streetSmartConfig?: { apiBaseUrl?: string; googleMapsApiKey?: string } }
).streetSmartConfig;
export const environment = {
  production: false,
  apiBaseUrl: runtime?.apiBaseUrl || '/api',
  googleMapsApiKey: runtime?.googleMapsApiKey || '',
  googleMapsId: '',
};
