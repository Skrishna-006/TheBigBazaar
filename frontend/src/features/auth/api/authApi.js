import axiosClient from '../../../api/axiosClient';

export async function register(payload) {
  const { data } = await axiosClient.post('/auth/register', payload);
  return data;
}

export async function login(payload) {
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
