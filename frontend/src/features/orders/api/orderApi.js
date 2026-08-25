import axiosClient from '../../../api/axiosClient';

export async function createOrder(addressId) {
  const { data } = await axiosClient.post('/orders', { addressId });
  return data;
}

export async function getOrders() {
  const { data } = await axiosClient.get('/orders');
  return data;
}

export async function getOrderById(id) {
  const { data } = await axiosClient.get(`/orders/${id}`);
  return data;
}

export async function cancelOrder(id) {
  const { data } = await axiosClient.post(`/orders/${id}/cancel`);
  return data;
}
