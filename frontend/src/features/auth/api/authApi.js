import axiosClient from '../../../api/axiosClient';

// === Registration Flow ===
export async function register(payload) {
  // payload: { email, firstName, lastName, password }
  const { data } = await axiosClient.post('/auth/register', payload);
  return data;
}

// === Password Reset Flow ===
export async function requestPasswordReset(email) {
  await axiosClient.post('/auth/password-reset/request', { email });
}

export async function resetPassword(payload) {
  // payload: { email, token, newPassword }
  await axiosClient.post('/auth/password-reset/reset', payload);
}

export async function exchangeOAuth2Code(code) {
  const { data } = await axiosClient.post('/auth/oauth2/exchange', { code });
  return data;
}

// === Login & Session ===
export async function login(payload) {
  // payload: { email, password }
  const { data } = await axiosClient.post('/auth/login', payload);
  return data;
}

export async function refreshToken(refreshTokenValue) {
  const { data } = await axiosClient.post('/auth/refresh', { refreshToken: refreshTokenValue });
  return data;
}

export async function logout() {
  await axiosClient.post('/auth/logout');
}

export async function getCurrentUser() {
  const { data } = await axiosClient.get('/auth/me');
  return data;
}
