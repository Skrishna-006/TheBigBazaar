import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { exchangeOAuth2Code } from '../features/auth/api/authApi';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { setAccessToken, setRefreshToken } from '../utils/tokenStorage';

export default function OAuth2CallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = useState(null);

  useEffect(() => {
    const code = searchParams.get('code');
    
    if (!code) {
      setError('Authorization code missing');
      return;
    }

    const exchangeCode = async () => {
      try {
        const data = await exchangeOAuth2Code(code);
        
        setAccessToken(data.accessToken);
        setRefreshToken(data.refreshToken);
        
        window.location.href = '/';
      } catch (err) {
        console.error('OAuth2 Exchange Error:', err);
        setError(err.response?.data?.message || 'Authentication failed. Please try again.');
        setTimeout(() => navigate('/login'), 3000);
      }
    };

    exchangeCode();
  }, [searchParams, navigate]);

  if (error) {
    return (
      <div className="auth-form-container">
        <h2>Authentication Failed</h2>
        <p className="auth-form__error">{error}</p>
        <p>Redirecting to login...</p>
      </div>
    );
  }

  return <LoadingSpinner label="Completing authentication..." />;
}
