import axiosClient from '../../../api/axiosClient';

export async function getWishlist() {
  const { data } = await axiosClient.get('/wishlist');
  return data;
}

export async function addToWishlist(productId) {
  const { data } = await axiosClient.post('/wishlist/items', { productId });
  return data;
}

export async function removeFromWishlist(productId) {
  await axiosClient.delete(`/wishlist/items/${productId}`);
}

export async function isWishlisted(productId) {
  const { data } = await axiosClient.get(`/wishlist/items/${productId}`);
  return data;
}
