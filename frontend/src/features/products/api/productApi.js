import axiosClient from '../../../api/axiosClient';

export async function getProducts(params = {}) {
  const { data } = await axiosClient.get('/products', { params });
  return data;
}

export async function getProductById(id) {
  const { data } = await axiosClient.get(`/products/${id}`);
  return data;
}

export async function getProductBySlug(slug) {
  const { data } = await axiosClient.get(`/products/slug/${slug}`);
  return data;
}
