import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/common/EmptyState';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { getOrders } from '../features/orders/api/orderApi';
import OrderCard from '../features/orders/components/OrderCard';
import { normalizeApiError } from '../utils/apiError';

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let mounted = true;

    async function loadOrders() {
      setIsLoading(true);
      setErrorMessage('');
      try {
        const data = await getOrders();
        if (mounted) {
          setOrders(data || []);
        }
      } catch (error) {
        if (mounted) {
          setErrorMessage(normalizeApiError(error).message);
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    loadOrders();

    return () => {
      mounted = false;
    };
  }, []);

  if (isLoading) {
    return <LoadingSpinner label="Loading orders..." />;
  }

  if (errorMessage) {
    return <ErrorMessage title="Unable to load orders" message={errorMessage} />;
  }

  if (!orders.length) {
    return (
      <EmptyState
        title="You have no orders yet."
        message={
          <span>
            Browse products and place your first order. <Link to="/products">Continue shopping</Link>
          </span>
        }
      />
    );
  }

  const sortedOrders = [...orders];

  return (
    <section className="orders-page">
      <div className="orders-page__header">
        <div>
          <p className="eyebrow">Order history</p>
          <h1>Your Orders</h1>
        </div>
      </div>

      <div className="orders-grid">
        {sortedOrders.map((order) => (
          <OrderCard key={order.id} order={order} />
        ))}
      </div>
    </section>
  );
}
