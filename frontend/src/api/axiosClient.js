import axios from 'axios';
import { clearTokens, getAccessToken, getRefreshToken, setAccessToken, setRefreshToken } from '../utils/tokenStorage';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const axiosClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const refreshClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

let refreshPromise = null;

axiosClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

function isAuthPath(url) {
  return ['/auth/login', '/auth/register', '/auth/refresh', '/auth/logout'].some((path) => url?.includes(path));
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('Missing refresh token');
  }

  if (!refreshPromise) {
    refreshPromise = refreshClient
      .post('/auth/refresh', { refreshToken })
      .then(({ data }) => {
        if (data?.accessToken) {
          setAccessToken(data.accessToken);
        }
        if (data?.refreshToken) {
          setRefreshToken(data.refreshToken);
        }
        return data;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    if (status !== 401 || !originalRequest || originalRequest._retry || isAuthPath(originalRequest.url)) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      await refreshAccessToken();
      const token = getAccessToken();
      if (token) {
        originalRequest.headers.Authorization = `Bearer ${token}`;
      }
      return axiosClient(originalRequest);
    } catch (refreshError) {
      clearTokens();
      return Promise.reject(refreshError);
    }
  }
);

export default axiosClient;
