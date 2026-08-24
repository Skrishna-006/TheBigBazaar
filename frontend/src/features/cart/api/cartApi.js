import axiosClient from '../../../api/axiosClient';

export async function getCart() {
  const { data } = await axiosClient.get('/cart');
  return data;
}

export async function addCartItem(productId, quantity) {
  const { data } = await axiosClient.post('/cart/items', { productId, quantity });
  return data;
}

export async function updateCartItemQuantity(productId, quantity) {
  const { data } = await axiosClient.put(`/cart/items/${productId}`, { quantity });
  return data;
}

export async function removeCartItem(productId) {
  await axiosClient.delete(`/cart/items/${productId}`);
}

export async function clearCart() {
  await axiosClient.delete('/cart');
}
