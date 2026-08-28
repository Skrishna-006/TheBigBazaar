import axiosClient from '../../../api/axiosClient';

export async function getCategories() {
  const { data } = await axiosClient.get('/categories');
  return data;
}

export async function createCategory(payload) {
  const { data } = await axiosClient.post('/categories', payload);
  return data;
}

export async function updateCategory(id, payload) {
  const { data } = await axiosClient.put(`/categories/${id}`, payload);
  return data;
}

export async function deleteCategory(id) {
  await axiosClient.delete(`/categories/${id}`);
}
