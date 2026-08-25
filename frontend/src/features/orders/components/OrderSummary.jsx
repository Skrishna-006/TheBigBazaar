import OrderStatusBadge from './OrderStatusBadge';
import ProductPrice from '../../products/components/ProductPrice';

export default function OrderSummary({ order, onCancel, isCancelling, canCancel }) {
  return (
    <aside className="order-summary">
      <div className="order-summary__header">
        <div>
          <p className="eyebrow">Order</p>
          <h2>{order.id}</h2>
        </div>
        <OrderStatusBadge status={order.status} />
      </div>

      <dl className="order-summary__details">
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
        <div className="order-summary__total">
          <dt>Total</dt>
          <dd><ProductPrice value={order.totalAmount} /></dd>
        </div>
      </dl>

      {canCancel ? (
        <button type="button" className="button button--secondary" onClick={onCancel} disabled={isCancelling}>
          {isCancelling ? 'Cancelling...' : 'Cancel Order'}
        </button>
      ) : null}
    </aside>
  );
}
