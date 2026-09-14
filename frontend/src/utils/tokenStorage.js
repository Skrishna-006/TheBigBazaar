const OLD_ACCESS_TOKEN_KEY = 'shopsphere_access_token';
const OLD_REFRESH_TOKEN_KEY = 'shopsphere_refresh_token';
const ACCESS_TOKEN_KEY = 'thebigbazaar_access_token';
const REFRESH_TOKEN_KEY = 'thebigbazaar_refresh_token';

function migrateTokens() {
  if (typeof window === 'undefined') return;
  const oldAccess = window.localStorage.getItem(OLD_ACCESS_TOKEN_KEY);
  if (oldAccess && !window.localStorage.getItem(ACCESS_TOKEN_KEY)) {
    window.localStorage.setItem(ACCESS_TOKEN_KEY, oldAccess);
    window.localStorage.removeItem(OLD_ACCESS_TOKEN_KEY);
  }
  const oldRefresh = window.localStorage.getItem(OLD_REFRESH_TOKEN_KEY);
  if (oldRefresh && !window.localStorage.getItem(REFRESH_TOKEN_KEY)) {
    window.localStorage.setItem(REFRESH_TOKEN_KEY, oldRefresh);
    window.localStorage.removeItem(OLD_REFRESH_TOKEN_KEY);
  }
}

export function getAccessToken() {
  if (typeof window === 'undefined') return null;
  migrateTokens();
  return window.localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token) {
  if (typeof window === 'undefined') return;
  migrateTokens();
  window.localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function clearAccessToken() {
  if (typeof window === 'undefined') return;
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
  window.localStorage.removeItem(OLD_ACCESS_TOKEN_KEY);
}

export function getRefreshToken() {
  if (typeof window === 'undefined') return null;
  migrateTokens();
  return window.localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setRefreshToken(token) {
  if (typeof window === 'undefined') return;
  migrateTokens();
  window.localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

export function clearTokens() {
  clearAccessToken();
  if (typeof window === 'undefined') return;
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
  window.localStorage.removeItem(OLD_REFRESH_TOKEN_KEY);
}
