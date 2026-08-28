import axiosClient from '../../../api/axiosClient';

export async function getAdminOrders(status) {
  const { data } = await axiosClient.get('/admin/orders', { params: status ? { status } : undefined });
  return data;
}

export async function getAdminOrderById(id) {
  const { data } = await axiosClient.get(`/admin/orders/${id}`);
  return data;
}

export async function updateAdminOrderStatus(id, status) {
  const { data } = await axiosClient.patch(`/admin/orders/${id}/status`, { status });
  return data;
}
