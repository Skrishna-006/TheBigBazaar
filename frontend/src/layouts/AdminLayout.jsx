import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth/context/AuthContext';
import AdminSidebar from '../components/admin/AdminSidebar';

export default function AdminLayout() {
  const { user } = useAuth();

  return (
    <div className="admin-shell">
      <AdminSidebar />
      <div className="admin-shell__content">
        <header className="admin-topbar">
          <div>
            <p className="admin-topbar__eyebrow">TheBigBazaar Administration</p>
            <h1 className="admin-topbar__title">
              {user?.firstName ? `${user.firstName} ${user.lastName ?? ''}`.trim() : 'Admin'}
            </h1>
          </div>
          <Link to="/products" className="secondary-btn">
            Back to Storefront
          </Link>
        </header>
        <main className="admin-page">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
