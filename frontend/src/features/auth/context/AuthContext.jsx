import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser, login as loginRequest, logout as logoutRequest, refreshToken as refreshTokenRequest, register as registerRequest } from '../api/authApi';
import { clearTokens, getAccessToken, getRefreshToken, setAccessToken, setRefreshToken } from '../../../utils/tokenStorage';
import { normalizeApiError } from '../../../utils/apiError';
import LoadingSpinner from '../../../components/common/LoadingSpinner';

const AuthContext = createContext(null);

let refreshPromise = null;

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    async function bootstrap() {
      const accessToken = getAccessToken();
      const refreshToken = getRefreshToken();

      if (!accessToken) {
        if (mounted) {
          setUser(null);
          setIsLoading(false);
        }
        return;
      }

      try {
        const currentUser = await getCurrentUser();
        if (mounted) {
          setUser(currentUser);
        }
      } catch (error) {
        const normalized = normalizeApiError(error);
        const shouldRefresh = normalized.status === 401 && refreshToken;
        if (shouldRefresh) {
          try {
            const refreshed = await refreshAuthenticationInternal(refreshToken);
            if (mounted) {
              setUser(refreshed.user ?? null);
            }
          } catch {
            clearTokens();
            if (mounted) {
              setUser(null);
            }
          }
        } else {
          clearTokens();
          if (mounted) {
            setUser(null);
          }
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    bootstrap();

    return () => {
      mounted = false;
    };
  }, []);

  const refreshAuthenticationInternal = useCallback(async (refreshTokenValue) => {
    if (!refreshPromise) {
      refreshPromise = refreshTokenRequest(refreshTokenValue)
        .then((response) => {
          if (response?.accessToken) {
            setAccessToken(response.accessToken);
          }
          if (response?.refreshToken) {
            setRefreshToken(response.refreshToken);
          }
          return response;
        })
        .finally(() => {
          refreshPromise = null;
        });
    }
    const refreshed = await refreshPromise;
    const currentUser = await getCurrentUser();
    return { ...refreshed, user: currentUser };
  }, []);

  const refreshAuthentication = useCallback(async () => {
    const refreshTokenValue = getRefreshToken();
    if (!refreshTokenValue) {
      clearTokens();
      setUser(null);
      return null;
    }

    try {
      const refreshed = await refreshAuthenticationInternal(refreshTokenValue);
      setUser(refreshed.user ?? null);
      return refreshed;
    } catch (error) {
      clearTokens();
      setUser(null);
      throw error;
    }
  }, [refreshAuthenticationInternal]);

  const login = useCallback(async (credentials) => {
    const response = await loginRequest(credentials);
    setAccessToken(response.accessToken);
    setRefreshToken(response.refreshToken);
    setUser(response.user);
    return response;
  }, []);

  const register = useCallback(async (payload) => {
    const response = await registerRequest(payload);
    setAccessToken(response.accessToken);
    setRefreshToken(response.refreshToken);
    setUser(response.user);
    return response;
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } catch {
      // Stateless JWT logout is best-effort on the backend.
    } finally {
      clearTokens();
      setUser(null);
      navigate('/', { replace: true });
    }
  }, [navigate]);

  const hasRole = useCallback((role) => user?.role === role, [user]);

  const isAdmin = useCallback(() => hasRole('ADMIN'), [hasRole]);

  const syncUser = useCallback((nextUser) => {
    setUser(nextUser);
  }, []);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isLoading,
      login,
      register,
      logout,
      refreshAuthentication,
      hasRole,
      isAdmin,
      syncUser,
    }),
    [user, isLoading, login, register, logout, refreshAuthentication, hasRole, isAdmin, syncUser]
  );

  if (isLoading) {
    return <LoadingSpinner label="Checking your session..." />;
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
