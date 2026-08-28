import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ProductPrice from '../features/products/components/ProductPrice';
import { getOrderById } from '../features/orders/api/orderApi';
import { getOrderPayment, initiatePayment } from '../features/payments/api/paymentApi';
import PaymentStatus from '../features/payments/components/PaymentStatus';
import PaymentMethodSelector from '../features/payments/components/PaymentMethodSelector';
import { normalizeApiError } from '../utils/apiError';

export default function PaymentFailurePage() {
  const { orderId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(location.state?.payment || null);
  const [paymentMethod, setPaymentMethod] = useState(payment?.paymentMethod || 'CARD');
  const [isLoading, setIsLoading] = useState(true);
  const [isRetrying, setIsRetrying] = useState(false);
  const [errorMessage, setErrorMessage] = useState(location.state?.error || '');

  useEffect(() => {
    let mounted = true;

    async function load() {
      setIsLoading(true);
      try {
        const orderData = await getOrderById(orderId);
        if (mounted) setOrder(orderData);
        try {
          const paymentData = await getOrderPayment(orderId);
          if (mounted) {
            setPayment(paymentData);
            setPaymentMethod(paymentData.paymentMethod || 'CARD');
          }
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

  async function handleRetry() {
    setErrorMessage('');
    setIsRetrying(true);
    try {
      const result = await initiatePayment(orderId, paymentMethod, `${orderId}:retry:${Date.now()}`);
      setPayment(result);
      if (result.status === 'PAID' || result.status === 'AUTHORIZED') {
        navigate(`/payment/success/${orderId}`, { replace: true, state: { payment: result } });
      } else {
        setErrorMessage(result.failureReason || 'Payment failed again.');
        setPayment(result);
      }
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setIsRetrying(false);
    }
  }

  if (isLoading) return <LoadingSpinner label="Loading payment failure..." />;
  if (errorMessage && !order) return <ErrorMessage title="Unable to load payment" message={errorMessage} />;

  return (
    <section className="page-card payment-result payment-result--failure">
      <p className="eyebrow">Payment failed</p>
      <h1>We couldn&apos;t complete your payment</h1>
      <PaymentStatus status={payment?.status || 'FAILED'} />
      {errorMessage ? <ErrorMessage title="Payment issue" message={errorMessage} /> : null}
      <dl className="payment-result__details">
        <div>
          <dt>Order ID</dt>
          <dd>{order?.id || orderId}</dd>
        </div>
        <div>
          <dt>Amount</dt>
          <dd><ProductPrice value={payment?.amount || order?.totalAmount} /></dd>
        </div>
        {payment?.failureReason ? (
          <div>
            <dt>Failure reason</dt>
            <dd>{payment.failureReason}</dd>
          </div>
        ) : null}
      </dl>
      <div className="payment-failure__actions">
        <PaymentMethodSelector value={paymentMethod} onChange={setPaymentMethod} disabled={isRetrying} />
        <div className="payment-result__actions">
          <button type="button" className="button button--primary" onClick={handleRetry} disabled={isRetrying}>
            {isRetrying ? 'Retrying...' : 'Retry payment'}
          </button>
          <Link className="button button--secondary" to={`/orders/${orderId}`}>Back to order details</Link>
          <Link className="button button--ghost" to="/products">Back to products</Link>
        </div>
      </div>
    </section>
  );
}
