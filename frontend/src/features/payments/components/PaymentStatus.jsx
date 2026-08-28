const statusClassMap = {
  PENDING: 'payment-status--pending',
  AUTHORIZED: 'payment-status--authorized',
  PAID: 'payment-status--paid',
  FAILED: 'payment-status--failed',
  CANCELLED: 'payment-status--cancelled',
  REFUNDED: 'payment-status--refunded',
};

export default function PaymentStatus({ status }) {
  const className = statusClassMap[status] || 'payment-status--default';
  return <span className={`payment-status ${className}`}>{status || 'UNKNOWN'}</span>;
}
