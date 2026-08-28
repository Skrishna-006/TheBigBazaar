import axiosClient from '../../../api/axiosClient';

export async function getBrands() {
  const { data } = await axiosClient.get('/brands');
  return data;
}

export async function createBrand(payload) {
  const { data } = await axiosClient.post('/brands', payload);
  return data;
}

export async function updateBrand(id, payload) {
  const { data } = await axiosClient.put(`/brands/${id}`, payload);
  return data;
}

export async function deleteBrand(id) {
  await axiosClient.delete(`/brands/${id}`);
}
