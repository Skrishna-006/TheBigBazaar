import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import { normalizeApiError } from '../../utils/apiError';
import { getAdminOrderById, updateAdminOrderStatus } from '../../features/admin/api/orderAdminApi';
import OrderItem from '../../features/orders/components/OrderItem';
import OrderStatusBadge from '../../features/orders/components/OrderStatusBadge';
import OrderSummary from '../../features/orders/components/OrderSummary';

export default function AdminOrderDetailsPage() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [status, setStatus] = useState('PROCESSING');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  const load = async () => {
    const data = await getAdminOrderById(id);
    setOrder(data);
    setStatus(data.status ?? 'PROCESSING');
  };

  useEffect(() => {
    (async () => {
      try {
        await load();
      } catch (exception) {
        setError(normalizeApiError(exception, 'Unable to load order.'));
      } finally {
        setIsLoading(false);
      }
    })();
  }, [id]);

  const saveStatus = async () => {
    try {
      setIsSaving(true);
      await updateAdminOrderStatus(id, status);
      await load();
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to update order status.'));
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) return <LoadingSpinner label="Loading order..." />;
  if (error) return <ErrorMessage title="Order error" message={error} />;
  if (!order) return null;

  return (
    <section className="admin-section">
      <div className="page-header">
        <div>
          <h1>Order {order.id}</h1>
          <OrderStatusBadge status={order.status} />
        </div>
        <Link className="secondary-btn" to="/admin/orders">Back to orders</Link>
      </div>
      <div className="admin-card">
        <h2>Status</h2>
        <div className="admin-inline-actions">
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            {['PENDING','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED'].map((entry) => <option key={entry} value={entry}>{entry}</option>)}
          </select>
          <button type="button" className="primary-btn" disabled={isSaving} onClick={saveStatus}>{isSaving ? 'Saving...' : 'Update status'}</button>
        </div>
      </div>
      <div className="admin-card">
        <h2>Shipping</h2>
        <p>{order.shippingAddress?.fullName}</p>
        <p>{order.shippingAddress?.phoneNumber}</p>
        <p>{order.shippingAddress?.addressLine1}</p>
        <p>{order.shippingAddress?.addressLine2}</p>
        <p>{order.shippingAddress?.city}, {order.shippingAddress?.state} {order.shippingAddress?.postalCode}</p>
        <p>{order.shippingAddress?.country}</p>
      </div>
      <div className="order-items">
        {order.items?.map((item) => <OrderItem key={item.productId} item={item} />)}
      </div>
      <OrderSummary order={order} />
    </section>
  );
}
