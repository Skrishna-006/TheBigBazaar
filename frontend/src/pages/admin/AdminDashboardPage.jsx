import { useEffect, useState } from 'react';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import EmptyState from '../../components/common/EmptyState';
import { getCategories } from '../../features/admin/api/categoryAdminApi';
import { getBrands } from '../../features/admin/api/brandAdminApi';
import { getAdminProducts } from '../../features/admin/api/productAdminApi';
import { getLowStockInventory } from '../../features/admin/api/inventoryAdminApi';
import { getAdminOrders } from '../../features/admin/api/orderAdminApi';
import { normalizeApiError } from '../../utils/apiError';

export default function AdminDashboardPage() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const [categories, brands, products, lowStock, orders] = await Promise.all([
          getCategories(),
          getBrands(),
          getAdminProducts(),
          getLowStockInventory(),
          getAdminOrders(),
        ]);
        if (!active) return;
        setData({ categories, brands, products, lowStock, orders });
      } catch (exception) {
        if (active) setError(normalizeApiError(exception, 'Unable to load admin dashboard.'));
      } finally {
        if (active) setIsLoading(false);
      }
    }

    load();
    return () => {
      active = false;
    };
  }, []);

  if (isLoading) return <LoadingSpinner label="Loading admin dashboard..." />;
  if (error) return <ErrorMessage title="Dashboard error" message={error} />;
  if (!data) return <EmptyState title="No data" message="No dashboard data is available right now." />;

  return (
    <section className="admin-section">
      <h1>Dashboard</h1>
      <div className="admin-stats">
        <article className="admin-stat-card"><span>Categories</span><strong>{data.categories.length}</strong></article>
        <article className="admin-stat-card"><span>Brands</span><strong>{data.brands.length}</strong></article>
        <article className="admin-stat-card"><span>Products</span><strong>{data.products.length}</strong></article>
        <article className="admin-stat-card"><span>Low stock</span><strong>{data.lowStock.length}</strong></article>
        <article className="admin-stat-card"><span>Orders</span><strong>{data.orders.length}</strong></article>
      </div>
    </section>
  );
}
