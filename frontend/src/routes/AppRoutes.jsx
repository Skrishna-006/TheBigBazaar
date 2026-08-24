import { Navigate, Route, Routes } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import HomePage from '../pages/HomePage';
import ProductsPage from '../pages/ProductsPage';
import ProductDetailsPage from '../pages/ProductDetailsPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import CartPage from '../pages/CartPage';
import ProtectedRoute from './ProtectedRoute';
import AdminRoute from './AdminRoute';
import { useAuth } from '../features/auth/context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';

function Placeholder({ title, message }) {
  return (
    <section className="page-card">
      <h1>{title}</h1>
      <p>{message}</p>
    </section>
  );
}

export default function AppRoutes() {
  const { isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner label="Preparing ShopSphere..." />;
  }

  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/products/:id" element={<ProductDetailsPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <Placeholder title="Profile" message="Profile management will be implemented in a later phase." />
            </ProtectedRoute>
          }
        />
        <Route
          path="/addresses"
          element={
            <ProtectedRoute>
              <Placeholder title="Addresses" message="Address management will be implemented in a later phase." />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <AdminRoute>
              <EmptyState title="Admin area" message="Admin features will be added in a later phase." />
            </AdminRoute>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
