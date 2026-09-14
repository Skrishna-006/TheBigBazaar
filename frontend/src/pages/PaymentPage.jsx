import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import EmptyState from '../components/common/EmptyState';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ProductPrice from '../features/products/components/ProductPrice';
import { getOrderById } from '../features/orders/api/orderApi';
import { getOrderPayment, initiatePayment } from '../features/payments/api/paymentApi';
import PaymentMethodSelector from '../features/payments/components/PaymentMethodSelector';
import PaymentStatus from '../features/payments/components/PaymentStatus';
import { normalizeApiError } from '../utils/apiError';

const STORAGE_PREFIX = 'thebigbazaar:payment:idempotency:';

function getStorageKey(orderId) {
  return `${STORAGE_PREFIX}${orderId}`;
}

function createAttemptKey(orderId) {
  const randomPart = typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`;
  const key = `${orderId}:${randomPart}`;
  sessionStorage.setItem(getStorageKey(orderId), key);
  return key;
}

export default function PaymentPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const attemptKeyRef = useRef(null);
  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let mounted = true;

    async function loadData() {
      setIsLoading(true);
      setErrorMessage('');
      try {
        const orderData = await getOrderById(orderId);
        if (!mounted) return;
        setOrder(orderData);

        try {
          const paymentData = await getOrderPayment(orderId);
          if (mounted) {
            setPayment(paymentData);
          }
        } catch (paymentError) {
          const normalized = normalizeApiError(paymentError);
          if (mounted && normalized.status !== 404) {
            setErrorMessage(normalized.message);
          }
          if (mounted) {
            setPayment(null);
          }
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

    loadData();

    return () => {
      mounted = false;
    };
  }, [orderId]);

  useEffect(() => {
    if (!attemptKeyRef.current) {
      attemptKeyRef.current = sessionStorage.getItem(getStorageKey(orderId)) || createAttemptKey(orderId);
    }
  }, [orderId]);

  const existingPaymentStatus = payment?.status;
  const canInitiate = useMemo(() => {
    if (!order) return false;
    if (existingPaymentStatus === 'PAID' || existingPaymentStatus === 'AUTHORIZED' || existingPaymentStatus === 'PENDING') {
      return false;
    }
    return true;
  }, [existingPaymentStatus, order]);

  async function handlePay() {
    setErrorMessage('');
    setMessage('');
    setIsSubmitting(true);

    try {
      const key = attemptKeyRef.current || createAttemptKey(orderId);
      const result = await initiatePayment(orderId, paymentMethod, key);
      setPayment(result);
      sessionStorage.removeItem(getStorageKey(orderId));
      attemptKeyRef.current = null;
      if (result.status === 'PAID' || result.status === 'AUTHORIZED') {
        navigate(`/payment/success/${orderId}`, { replace: true, state: { payment: result } });
      } else {
        navigate(`/payment/failure/${orderId}`, { replace: true, state: { payment: result } });
      }
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message);
      navigate(`/payment/failure/${orderId}`, { replace: true, state: { error: normalized.message } });
    } finally {
      setIsSubmitting(false);
    }
  }

  if (isLoading) {
    return <LoadingSpinner label="Loading payment..." />;
  }

  if (errorMessage && !order) {
    return <ErrorMessage title="Unable to load payment" message={errorMessage} />;
  }

  if (!order) {
    return <EmptyState title="Order not found." message={<span><Link to="/orders">Back to orders</Link></span>} />;
  }

  const shipping = order.shippingAddress || {};

  return (
    <section className="payment-page">
      <div className="payment-page__header">
        <div>
          <p className="eyebrow">Payment</p>
          <h1>Complete payment for order {order.id}</h1>
          <p>Amount is shown exactly as returned by the backend.</p>
        </div>
        <div className="payment-page__actions">
          <Link className="button button--secondary" to={`/orders/${order.id}`}>Order details</Link>
          <Link className="button button--ghost" to="/products">Continue shopping</Link>
        </div>
      </div>

      {message ? <div className="success-message" aria-live="polite">{message}</div> : null}
      {errorMessage ? <ErrorMessage title="Payment error" message={errorMessage} /> : null}

      <div className="payment-layout">
        <div className="page-card payment-card">
          <h2>Order summary</h2>
          <dl className="payment-summary">
            <div>
              <dt>Order ID</dt>
              <dd>{order.id}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{order.status}</dd>
            </div>
            <div>
              <dt>Subtotal</dt>
              <dd><ProductPrice value={order.subtotal} /></dd>
            </div>
            <div>
              <dt>Shipping</dt>
              <dd><ProductPrice value={order.shippingAmount} /></dd>
            </div>
            <div>
              <dt>Discount</dt>
              <dd><ProductPrice value={order.discountAmount} /></dd>
            </div>
            <div className="payment-summary__total">
              <dt>Total</dt>
              <dd><ProductPrice value={order.totalAmount} /></dd>
            </div>
          </dl>
          <div className="payment-shipping">
            <h3>Shipping information</h3>
            <address>
              <strong>{shipping.fullName}</strong>
              <span>{shipping.phoneNumber}</span>
              <span>{shipping.addressLine1}</span>
              {shipping.addressLine2 ? <span>{shipping.addressLine2}</span> : null}
              <span>{shipping.city}, {shipping.state} {shipping.postalCode}</span>
              <span>{shipping.country}</span>
            </address>
          </div>
        </div>

        <div className="page-card payment-card">
          <h2>Payment method</h2>
          {payment ? (
            <div className="payment-existing">
              <PaymentStatus status={payment.status} />
              <p>Payment already exists for this order.</p>
              <p>Method: {payment.paymentMethod}</p>
              <p>Amount: <ProductPrice value={payment.amount} /></p>
              {payment.providerReference ? <p>Reference: {payment.providerReference}</p> : null}
              {payment.failureReason ? <p>Failure: {payment.failureReason}</p> : null}
              <Link className="button button--primary" to={payment.status === 'PAID' ? `/payment/success/${order.id}` : `/payment/failure/${order.id}`}>
                View payment result
              </Link>
            </div>
          ) : (
            <>
              <PaymentMethodSelector value={paymentMethod} onChange={setPaymentMethod} disabled={isSubmitting || !canInitiate} />
              <button
                type="button"
                className="button button--primary"
                onClick={handlePay}
                disabled={isSubmitting || !canInitiate}
              >
                {isSubmitting ? 'Processing payment...' : 'Pay now'}
              </button>
              {!canInitiate ? <p className="payment-note">A payment already exists for this order.</p> : null}
            </>
          )}
        </div>
      </div>
    </section>
  );
}
