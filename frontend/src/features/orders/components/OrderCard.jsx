import { Link } from 'react-router-dom';
import OrderStatusBadge from './OrderStatusBadge';
import ProductPrice from '../../products/components/ProductPrice';
import PaymentStatus from '../../payments/components/PaymentStatus';

export default function OrderCard({ order, paymentStatus }) {
  return (
    <article className="order-card">
      <div className="order-card__top">
        <div>
          <h3 className="order-card__title">Order {order.id}</h3>
          <p className="order-card__meta">{new Date(order.createdAt).toLocaleString()}</p>
        </div>
        <OrderStatusBadge status={order.status} />
      </div>

      <div className="order-card__summary">
        <span>{order.items?.length || 0} item(s)</span>
        <strong><ProductPrice value={order.totalAmount} /></strong>
      </div>
      {paymentStatus ? (
        <div className="order-card__payment">
          <span>Payment</span>
          <PaymentStatus status={paymentStatus} />
        </div>
      ) : null}

      <div className="order-card__actions">
        <Link className="button button--secondary button--small" to={`/orders/${order.id}`}>
          View Details
        </Link>
      </div>
    </article>
  );
}
