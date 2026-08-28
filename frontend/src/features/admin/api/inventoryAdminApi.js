import axiosClient from '../../../api/axiosClient';

export async function createInventory(payload) {
  const { data } = await axiosClient.post('/inventory', payload);
  return data;
}

export async function getInventoryByProductId(productId) {
  const { data } = await axiosClient.get(`/inventory/products/${productId}`);
  return data;
}

export async function adjustInventory(productId, payload) {
  const { data } = await axiosClient.post(`/inventory/products/${productId}/adjust`, payload);
  return data;
}

export async function getLowStockInventory() {
  const { data } = await axiosClient.get('/inventory/low-stock');
  return data;
}

export async function getInventoryMovements(productId) {
  const { data } = await axiosClient.get(`/inventory/products/${productId}/movements`);
  return data;
}
