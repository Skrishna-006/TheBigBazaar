import axiosClient from '../../../api/axiosClient';

export async function getBrands() {
  const { data } = await axiosClient.get('/brands');
  return data;
}

export async function getBrandById(id) {
  const { data } = await axiosClient.get(`/brands/${id}`);
  return data;
}
