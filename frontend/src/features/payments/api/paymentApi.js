import axiosClient from '../../../api/axiosClient';

export async function initiatePayment(orderId, paymentMethod, idempotencyKey) {
  const { data } = await axiosClient.post(
    `/payments/orders/${orderId}`,
    { paymentMethod },
    {
      headers: idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : undefined,
    }
  );
  return data;
}

export async function getPayment(paymentId) {
  const { data } = await axiosClient.get(`/payments/${paymentId}`);
  return data;
}

export async function getOrderPayment(orderId) {
  const { data } = await axiosClient.get(`/orders/${orderId}/payment`);
  return data;
}
