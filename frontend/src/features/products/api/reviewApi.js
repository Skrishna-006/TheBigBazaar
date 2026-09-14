import axiosClient from '../../../api/axiosClient';

export const createReview = async (productId, payload) => {
  const response = await axiosClient.post(`/products/${productId}/reviews`, payload);
  return response.data;
};

export const updateReview = async (reviewId, payload) => {
  const response = await axiosClient.put(`/reviews/${reviewId}`, payload);
  return response.data;
};

export const deleteReview = async (reviewId) => {
  const response = await axiosClient.delete(`/reviews/${reviewId}`);
  return response.data;
};
