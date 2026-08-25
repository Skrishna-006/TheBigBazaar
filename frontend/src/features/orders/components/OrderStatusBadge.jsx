const statusClassMap = {
  PENDING: 'order-status-badge--pending',
  CONFIRMED: 'order-status-badge--confirmed',
  PROCESSING: 'order-status-badge--processing',
  SHIPPED: 'order-status-badge--shipped',
  DELIVERED: 'order-status-badge--delivered',
  CANCELLED: 'order-status-badge--cancelled',
};

export default function OrderStatusBadge({ status }) {
  const statusClass = statusClassMap[status] || 'order-status-badge--default';

  return <span className={`order-status-badge ${statusClass}`}>{status}</span>;
}
