import axiosClient from '../../../api/axiosClient';

export async function getAddresses() {
  const { data } = await axiosClient.get('/addresses');
  return data;
}

export async function getAddressById(id) {
  const { data } = await axiosClient.get(`/addresses/${id}`);
  return data;
}

export async function createAddress(data) {
  const { data: response } = await axiosClient.post('/addresses', data);
  return response;
}

export async function updateAddress(id, data) {
  const { data: response } = await axiosClient.put(`/addresses/${id}`, data);
  return response;
}

export async function deleteAddress(id) {
  await axiosClient.delete(`/addresses/${id}`);
}

export async function setDefaultAddress(id) {
  await axiosClient.post(`/addresses/${id}/default`);
}
