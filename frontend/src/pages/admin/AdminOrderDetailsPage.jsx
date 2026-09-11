import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import { normalizeApiError } from '../../utils/apiError';
import { getAdminOrderById, updateAdminOrderStatus } from '../../features/admin/api/orderAdminApi';
import OrderItem from '../../features/orders/components/OrderItem';
import OrderStatusBadge from '../../features/orders/components/OrderStatusBadge';
import OrderSummary from '../../features/orders/components/OrderSummary';
import { getAvailableTransitions } from '../../features/orders/utils/orderTransitions';

export default function AdminOrderDetailsPage() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [loadError, setLoadError] = useState('');
  const [saveError, setSaveError] = useState('');
  const [status, setStatus] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  const load = async () => {
    const data = await getAdminOrderById(id);
    setOrder(data);
    setStatus(data.status);
  };

  useEffect(() => {
    (async () => {
      try {
        await load();
      } catch (exception) {
        setLoadError(normalizeApiError(exception).message);
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
      setSaveError('');
    } catch (exception) {
      setSaveError(normalizeApiError(exception).message);
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) return <LoadingSpinner label="Loading order..." />;
  if (loadError) return <ErrorMessage title="Order error" message={loadError} />;
  if (!order) return null;

  const availableTransitions = getAvailableTransitions(order.status);
  const isTerminal = availableTransitions.length === 0;

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
          {isTerminal ? (
            <span className="status-note" style={{ fontStyle: 'italic', color: 'var(--text-muted)' }}>No further transitions available</span>
          ) : (
            <>
              <select value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value={order.status} disabled>{order.status} (Current)</option>
                {availableTransitions.map((entry) => <option key={entry} value={entry}>{entry}</option>)}
              </select>
              <button type="button" className="primary-btn" disabled={isSaving || status === order.status} onClick={saveStatus}>{isSaving ? 'Saving...' : 'Update status'}</button>
            </>
          )}
        </div>
        {saveError ? <p className="form-error" style={{ marginTop: '1rem' }}>{saveError}</p> : null}
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
