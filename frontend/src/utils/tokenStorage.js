const ACCESS_TOKEN_KEY = 'shopsphere_access_token';
const REFRESH_TOKEN_KEY = 'shopsphere_refresh_token';

export function getAccessToken() {
  if (typeof window === 'undefined') {
    return null;
  }
  return window.localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token) {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function clearAccessToken() {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken() {
  if (typeof window === 'undefined') {
    return null;
  }
  return window.localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setRefreshToken(token) {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

export function clearTokens() {
  clearAccessToken();
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
}
