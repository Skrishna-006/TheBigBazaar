import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { cancelOrder, getOrderById } from '../features/orders/api/orderApi';
import { getOrderPayment } from '../features/payments/api/paymentApi';
import OrderItem from '../features/orders/components/OrderItem';
import OrderStatusBadge from '../features/orders/components/OrderStatusBadge';
import OrderSummary from '../features/orders/components/OrderSummary';
import PaymentStatus from '../features/payments/components/PaymentStatus';
import ProductPrice from '../features/products/components/ProductPrice';
import { normalizeApiError } from '../utils/apiError';

export default function OrderDetailsPage() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isCancelling, setIsCancelling] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let mounted = true;

    async function loadOrder() {
      setIsLoading(true);
      setErrorMessage('');
      setNotFound(false);
      try {
        const data = await getOrderById(id);
        if (mounted) {
          setOrder(data);
        }
        try {
          const paymentData = await getOrderPayment(id);
          if (mounted) {
            setPayment(paymentData);
          }
        } catch (paymentError) {
          const normalizedPayment = normalizeApiError(paymentError);
          if (mounted && normalizedPayment.status !== 404) {
            setErrorMessage(normalizedPayment.message);
          }
          if (mounted) {
            setPayment(null);
          }
        }
      } catch (error) {
        const normalized = normalizeApiError(error);
        if (mounted) {
          if (normalized.status === 404) {
            setNotFound(true);
          } else {
            setErrorMessage(normalized.message);
          }
          setOrder(null);
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    loadOrder();

    return () => {
      mounted = false;
    };
  }, [id]);

  const canCancel = useMemo(() => {
    if (!order) {
      return false;
    }
    return ['PENDING', 'CONFIRMED'].includes(order.status);
  }, [order]);

  async function handleCancel() {
    const confirmed = window.confirm('Are you sure you want to cancel this order?');
    if (!confirmed) {
      return;
    }

    setIsCancelling(true);
    setErrorMessage('');
    try {
      const updated = await cancelOrder(id);
      setOrder(updated);
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setIsCancelling(false);
    }
  }

  if (isLoading) {
    return <LoadingSpinner label="Loading order details..." />;
  }

  if (notFound) {
    return <ErrorMessage title="Order not found" message="This order does not exist or you do not have access to it." />;
  }

  if (errorMessage) {
    return <ErrorMessage title="Unable to load order" message={errorMessage} />;
  }

  if (!order) {
    return null;
  }

  const shipping = order.shippingAddress || {};

  return (
    <section className="order-details">
      <div className="order-details__header">
        <div>
          <p className="eyebrow">Order details</p>
          <h1>Order {order.id}</h1>
          <p>{new Date(order.createdAt).toLocaleString()}</p>
        </div>
        <Link className="button button--secondary" to="/orders">
          Back to orders
        </Link>
      </div>

      {errorMessage ? <ErrorMessage title="Action failed" message={errorMessage} /> : null}

      <div className="order-details__layout">
        <div className="order-details__main">
          <section className="page-card">
            <div className="order-details__status">
              <h2>Order status</h2>
              <OrderStatusBadge status={order.status} />
            </div>
          </section>

          {payment ? (
            <section className="page-card">
              <div className="order-details__status">
                <h2>Payment status</h2>
                <PaymentStatus status={payment.status} />
              </div>
              <dl className="payment-summary">
                <div>
                  <dt>Method</dt>
                  <dd>{payment.paymentMethod}</dd>
                </div>
                <div>
                  <dt>Amount</dt>
                  <dd><ProductPrice value={payment.amount} /></dd>
                </div>
                {payment.providerReference ? (
                  <div>
                    <dt>Reference</dt>
                    <dd>{payment.providerReference}</dd>
                  </div>
                ) : null}
              </dl>
              {['FAILED', 'CANCELLED'].includes(payment.status) ? (
                <div className="payment-result__actions">
                  <Link className="button button--primary" to={`/payment/${order.id}`}>
                    Retry payment
                  </Link>
                </div>
              ) : null}
            </section>
          ) : null}

          <section className="page-card">
            <h2>Shipping information</h2>
            <address className="order-address">
              <strong>{shipping.fullName}</strong>
              <span>{shipping.phoneNumber}</span>
              <span>{shipping.addressLine1}</span>
              {shipping.addressLine2 ? <span>{shipping.addressLine2}</span> : null}
              <span>
                {shipping.city}, {shipping.state} {shipping.postalCode}
              </span>
              <span>{shipping.country}</span>
            </address>
          </section>

          <section className="page-card">
            <h2>Items</h2>
            <div className="order-items">
              {order.items.map((item) => (
                <OrderItem key={`${item.productId}-${item.productSku}`} item={item} />
              ))}
            </div>
          </section>
        </div>

        <OrderSummary order={order} onCancel={handleCancel} isCancelling={isCancelling} canCancel={canCancel} />
      </div>
    </section>
  );
}
