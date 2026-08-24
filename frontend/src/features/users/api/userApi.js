import axiosClient from '../../../api/axiosClient';

export async function getCurrentUser() {
  const { data } = await axiosClient.get('/users/me');
  return data;
}

export async function updateProfile(data) {
  const { data: response } = await axiosClient.put('/users/me', data);
  return response;
}

export async function changePassword(data) {
  const { data: response } = await axiosClient.put('/users/me/password', data);
  return response;
}
