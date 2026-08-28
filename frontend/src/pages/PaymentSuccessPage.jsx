import { useEffect, useState } from 'react';
import { Link, useParams, useLocation } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ProductPrice from '../features/products/components/ProductPrice';
import { getOrderById } from '../features/orders/api/orderApi';
import { getOrderPayment } from '../features/payments/api/paymentApi';
import PaymentStatus from '../features/payments/components/PaymentStatus';
import { normalizeApiError } from '../utils/apiError';

export default function PaymentSuccessPage() {
  const { orderId } = useParams();
  const location = useLocation();
  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(location.state?.payment || null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let mounted = true;

    async function load() {
      setIsLoading(true);
      setErrorMessage('');
      try {
        const orderData = await getOrderById(orderId);
        if (mounted) setOrder(orderData);
        try {
          const paymentData = await getOrderPayment(orderId);
          if (mounted) setPayment(paymentData);
        } catch (paymentError) {
          const normalized = normalizeApiError(paymentError);
          if (mounted && normalized.status !== 404) setErrorMessage(normalized.message);
        }
      } catch (error) {
        if (mounted) setErrorMessage(normalizeApiError(error).message);
      } finally {
        if (mounted) setIsLoading(false);
      }
    }

    load();
    return () => {
      mounted = false;
    };
  }, [orderId]);

  if (isLoading) return <LoadingSpinner label="Loading payment result..." />;
  if (errorMessage && !order) return <ErrorMessage title="Unable to load payment" message={errorMessage} />;

  return (
    <section className="page-card payment-result">
      <p className="eyebrow">Payment complete</p>
      <h1>Payment successful</h1>
      <p>Your order payment was processed by the development gateway.</p>
      <PaymentStatus status={payment?.status || 'PAID'} />
      <dl className="payment-result__details">
        <div>
          <dt>Order ID</dt>
          <dd>{order?.id || orderId}</dd>
        </div>
        <div>
          <dt>Amount</dt>
          <dd><ProductPrice value={payment?.amount || order?.totalAmount} /></dd>
        </div>
        {payment?.providerReference ? (
          <div>
            <dt>Reference</dt>
            <dd>{payment.providerReference}</dd>
          </div>
        ) : null}
      </dl>
      <div className="payment-result__actions">
        <Link className="button button--primary" to={`/orders/${orderId}`}>View order details</Link>
        <Link className="button button--secondary" to="/products">Continue shopping</Link>
      </div>
    </section>
  );
}
