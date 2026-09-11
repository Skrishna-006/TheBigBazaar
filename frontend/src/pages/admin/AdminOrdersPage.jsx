import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import EmptyState from '../../components/common/EmptyState';
import { normalizeApiError } from '../../utils/apiError';
import { getAdminOrders, updateAdminOrderStatus } from '../../features/admin/api/orderAdminApi';
import OrderStatusBadge from '../../features/orders/components/OrderStatusBadge';
import { getAvailableTransitions } from '../../features/orders/utils/orderTransitions';

const statuses = ['', 'PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [activeOrderId, setActiveOrderId] = useState('');
  const [nextStatuses, setNextStatuses] = useState({});
  const [rowErrors, setRowErrors] = useState({});

  const load = async (nextFilter = status) => {
    setOrders(await getAdminOrders(nextFilter || undefined));
  };

  useEffect(() => {
    (async () => {
      try {
        await load();
      } catch (exception) {
        setError(normalizeApiError(exception).message);
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
      setError(normalizeApiError(exception).message);
    }
  };

  const changeStatus = async (orderId, fallbackStatus) => {
    try {
      setActiveOrderId(orderId);
      const targetStatus = nextStatuses[orderId] || fallbackStatus;
      await updateAdminOrderStatus(orderId, targetStatus);
      await load();
      setSuccess('Order status updated successfully.');
      setNextStatuses(prev => {
        const updated = { ...prev };
        delete updated[orderId];
        return updated;
      });
      setRowErrors(prev => {
        const updated = { ...prev };
        delete updated[orderId];
        return updated;
      });
      setError('');
    } catch (exception) {
      setRowErrors(prev => ({ ...prev, [orderId]: normalizeApiError(exception).message }));
      setSuccess('');
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
          {orders.map((order) => {
            const availableTransitions = getAvailableTransitions(order.status);
            const isTerminal = availableTransitions.length === 0;

            return (
              <article key={order.id} className="admin-row admin-row--stack" style={{ flexWrap: 'wrap' }}>
                <div>
                  <strong>{order.id}</strong>
                  <p>{order.createdAt}</p>
                  <p>Total: {order.totalAmount}</p>
                  <OrderStatusBadge status={order.status} />
                </div>
                <div className="admin-row__actions">
                  <Link className="secondary-btn" to={`/admin/orders/${order.id}`}>View</Link>
                  {isTerminal ? (
                    <span className="status-note" style={{ fontStyle: 'italic', color: 'var(--text-muted)' }}>No further transitions</span>
                  ) : (
                    <>
                      <select 
                        value={nextStatuses[order.id] || order.status} 
                        onChange={(e) => setNextStatuses(prev => ({...prev, [order.id]: e.target.value}))} 
                        aria-label="Next status"
                      >
                        <option value={order.status} disabled>{order.status} (Current)</option>
                        {availableTransitions.map((entry) => <option key={entry} value={entry}>{entry}</option>)}
                      </select>
                      <button 
                        type="button" 
                        className="primary-btn" 
                        disabled={activeOrderId === order.id || !nextStatuses[order.id] || nextStatuses[order.id] === order.status} 
                        onClick={() => changeStatus(order.id, order.status)}
                      >
                        {activeOrderId === order.id ? 'Saving...' : 'Update'}
                      </button>
                    </>
                  )}
                </div>
                {rowErrors[order.id] ? <div className="form-error" style={{ width: '100%', marginTop: '1rem' }}>{rowErrors[order.id]}</div> : null}
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
