import axiosClient from '../../../api/axiosClient';

export async function getAdminProducts() {
  const { data } = await axiosClient.get('/products');
  return data;
}

export async function createProduct(payload) {
  const { data } = await axiosClient.post('/products', payload);
  return data;
}

export async function updateProduct(id, payload) {
  const { data } = await axiosClient.put(`/products/${id}`, payload);
  return data;
}

export async function deleteProduct(id) {
  await axiosClient.delete(`/products/${id}`);
}
