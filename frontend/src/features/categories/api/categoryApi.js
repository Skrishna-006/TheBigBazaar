import axiosClient from '../../../api/axiosClient';

export async function getCategories() {
  const { data } = await axiosClient.get('/categories');
  return data;
}

export async function getCategoryById(id) {
  const { data } = await axiosClient.get(`/categories/${id}`);
  return data;
}
