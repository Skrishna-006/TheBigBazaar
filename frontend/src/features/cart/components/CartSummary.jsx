import ProductPrice from '../../products/components/ProductPrice';
import { Link } from 'react-router-dom';

export default function CartSummary({ totalItemCount, subtotal, onClearCart, isClearing }) {
  return (
    <aside className="cart-summary">
      <h2>Cart Summary</h2>
      <div className="cart-summary__row">
        <span>Total items</span>
        <strong>{totalItemCount}</strong>
      </div>
      <div className="cart-summary__row">
        <span>Subtotal</span>
        <strong>
          <ProductPrice value={subtotal} />
        </strong>
      </div>
      {totalItemCount > 0 && (
        <Link className="button button--primary" to="/checkout" style={{ width: '100%', marginBottom: '1rem' }}>
          Proceed to Checkout
        </Link>
      )}
      <button type="button" className="button button--secondary" onClick={onClearCart} disabled={isClearing} style={{ width: '100%' }}>
        {isClearing ? 'Clearing...' : 'Clear Cart'}
      </button>
      <p className="cart-summary__note">Cart does not reserve stock. Final validation happens during checkout later.</p>
    </aside>
  );
}
