import axiosClient from '../../../api/axiosClient';

// === Registration Flow ===
export async function sendRegistrationOtp(email) {
  await axiosClient.post('/auth/register/send-otp', { email });
}

export async function verifyRegistrationOtp(email, otp) {
  const { data } = await axiosClient.post('/auth/register/verify-otp', { email, otp });
  return data; // { token }
}

export async function completeRegistration(payload) {
  // payload: { email, token, firstName, lastName, password }
  const { data } = await axiosClient.post('/auth/register/complete', payload);
  return data;
}

// === Password Reset Flow ===
export async function sendPasswordResetOtp(email) {
  await axiosClient.post('/auth/password-reset/send-otp', { email });
}

export async function verifyPasswordResetOtp(email, otp) {
  const { data } = await axiosClient.post('/auth/password-reset/verify-otp', { email, otp });
  return data; // { token }
}

export async function completePasswordReset(payload) {
  // payload: { email, token, newPassword }
  await axiosClient.post('/auth/password-reset/complete', payload);
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
