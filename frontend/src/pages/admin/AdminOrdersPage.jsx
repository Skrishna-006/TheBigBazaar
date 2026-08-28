import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import EmptyState from '../../components/common/EmptyState';
import { normalizeApiError } from '../../utils/apiError';
import { getAdminOrders, updateAdminOrderStatus } from '../../features/admin/api/orderAdminApi';
import OrderStatusBadge from '../../features/orders/components/OrderStatusBadge';

const statuses = ['', 'PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [activeOrderId, setActiveOrderId] = useState('');
  const [nextStatus, setNextStatus] = useState('PROCESSING');

  const load = async (nextFilter = status) => {
    setOrders(await getAdminOrders(nextFilter || undefined));
  };

  useEffect(() => {
    (async () => {
      try {
        await load();
      } catch (exception) {
        setError(normalizeApiError(exception, 'Unable to load orders.'));
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  const applyFilter = async (event) => {
    const nextValue = event.target.value;
    setStatus(nextValue);
    try {
      await load(nextValue);
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to load orders.'));
    }
  };

  const changeStatus = async (orderId) => {
    try {
      setActiveOrderId(orderId);
      await updateAdminOrderStatus(orderId, nextStatus);
      await load();
      setSuccess('Order status updated.');
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to update order status.'));
    } finally {
      setActiveOrderId('');
    }
  };

  if (isLoading) return <LoadingSpinner label="Loading orders..." />;

  return (
    <section className="admin-section">
      <h1>Orders</h1>
      <label className="inline-field">
        Status
        <select value={status} onChange={applyFilter}>
          {statuses.map((entry) => <option key={entry || 'ALL'} value={entry}>{entry || 'All statuses'}</option>)}
        </select>
      </label>
      {error ? <ErrorMessage title="Order error" message={error} /> : null}
      {success ? <p className="form-success">{success}</p> : null}
      {orders.length === 0 ? <EmptyState title="No orders" message="Orders will appear here." /> : (
        <div className="admin-table">
          {orders.map((order) => (
            <article key={order.id} className="admin-row admin-row--stack">
              <div>
                <strong>{order.id}</strong>
                <p>{order.createdAt}</p>
                <p>Total: {order.totalAmount}</p>
                <OrderStatusBadge status={order.status} />
              </div>
              <div className="admin-row__actions">
                <Link className="secondary-btn" to={`/admin/orders/${order.id}`}>View</Link>
                <select value={nextStatus} onChange={(e) => setNextStatus(e.target.value)} aria-label="Next status">
                  {['PENDING','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED'].map((entry) => <option key={entry} value={entry}>{entry}</option>)}
                </select>
                <button type="button" className="primary-btn" disabled={activeOrderId === order.id} onClick={() => changeStatus(order.id)}>
                  {activeOrderId === order.id ? 'Saving...' : 'Update'}
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
