import { Navigate, useLocation } from 'react-router-dom';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { useAuth } from '../features/auth/context/AuthContext';

export default function AdminRoute({ children }) {
  const { isAuthenticated, isLoading, isAdmin } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingSpinner label="Loading session..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (!isAdmin()) {
    return <EmptyState title="Forbidden" message="You do not have permission to access this page." />;
  }

  return children;
}
