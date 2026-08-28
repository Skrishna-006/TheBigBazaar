import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import EmptyState from '../components/common/EmptyState';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { getAddresses } from '../features/addresses/api/addressApi';
import { getCart } from '../features/cart/api/cartApi';
import { createOrder } from '../features/orders/api/orderApi';
import ProductPrice from '../features/products/components/ProductPrice';
import { normalizeApiError } from '../utils/apiError';

export default function CheckoutPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isPlacingOrder, setIsPlacingOrder] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const items = cart?.items || [];
  const hasCartItems = items.length > 0;

  useEffect(() => {
    let mounted = true;

    async function loadCheckoutData() {
      setIsLoading(true);
      setErrorMessage('');
      try {
        const [cartData, addressData] = await Promise.all([getCart(), getAddresses()]);
        if (!mounted) {
          return;
        }
        setCart(cartData);
        setAddresses(addressData || []);
        const defaultAddress = (addressData || []).find((address) => address.defaultAddress) || addressData?.[0] || null;
        setSelectedAddressId(defaultAddress?.id || '');
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

    loadCheckoutData();

    return () => {
      mounted = false;
    };
  }, []);

  async function handlePlaceOrder() {
    setErrorMessage('');
    setSuccessMessage('');

    if (!hasCartItems) {
      setErrorMessage('Your cart is empty.');
      return;
    }
    if (!selectedAddressId) {
      setErrorMessage('Please select a shipping address.');
      return;
    }

    setIsPlacingOrder(true);
    try {
      const order = await createOrder(selectedAddressId);
      navigate(`/payment/${order.id}`, { replace: true });
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setIsPlacingOrder(false);
    }
  }

  if (isLoading) {
    return <LoadingSpinner label="Loading checkout..." />;
  }

  if (errorMessage && !cart) {
    return <ErrorMessage title="Unable to load checkout" message={errorMessage} />;
  }

  if (!hasCartItems) {
    return (
      <EmptyState
        title="Your cart is empty."
        message={
          <span>
            Add products before checking out. <Link to="/products">Continue shopping</Link>
          </span>
        }
      />
    );
  }

  return (
    <section className="checkout-page">
      <div className="checkout-page__header">
        <div>
          <p className="eyebrow">Checkout</p>
          <h1>Review and place your order</h1>
        </div>
        <Link className="button button--secondary" to="/cart">
          Back to cart
        </Link>
      </div>

      {errorMessage ? <ErrorMessage title="Checkout failed" message={errorMessage} /> : null}
      {successMessage ? <div className="success-message" aria-live="polite">{successMessage}</div> : null}

      {!addresses.length ? (
        <EmptyState
          title="You need a shipping address before placing the order."
          message={
            <span>
              Add one in your address book. <Link to="/addresses">Go to addresses</Link>
            </span>
          }
        />
      ) : (
        <div className="checkout-layout">
          <div className="checkout-panel">
            <h2>Shipping address</h2>
            <div className="checkout-addresses" role="radiogroup" aria-label="Shipping address">
              {addresses.map((address) => (
                <label key={address.id} className={`checkout-address${selectedAddressId === address.id ? ' checkout-address--selected' : ''}`}>
                  <input
                    type="radio"
                    name="shipping-address"
                    value={address.id}
                    checked={selectedAddressId === address.id}
                    onChange={() => setSelectedAddressId(address.id)}
                  />
                  <span className="checkout-address__name">
                    {address.firstName} {address.lastName}
                    {address.isDefault ? <span className="default-badge">Default</span> : null}
                  </span>
                  <span>{address.phone}</span>
                  <span>{address.addressLine1}</span>
                  {address.addressLine2 ? <span>{address.addressLine2}</span> : null}
                  <span>
                    {address.city}, {address.state} {address.postalCode}
                  </span>
                  <span>{address.country}</span>
                </label>
              ))}
            </div>
          </div>

          <div className="checkout-panel">
            <h2>Order review</h2>
            <div className="checkout-items">
              {items.map((item) => (
                <article key={item.productId} className="checkout-item">
                  <img
                    className="checkout-item__image"
                    src={item.productImageUrl}
                    alt={`${item.productName} product image`}
                    onError={(event) => {
                      event.currentTarget.src =
                        'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns%3D%22http%3A//www.w3.org/2000/svg%22 width%3D%22100%22 height%3D%22100%22 viewBox%3D%220 0 100 100%22%3E%3Crect width%3D%22100%22 height%3D%22100%22 rx%3D%2218%22 fill%3D%22%23e2e8f0%22/%3E%3C/svg%3E';
                    }}
                  />
                  <div className="checkout-item__body">
                    <strong>{item.productName}</strong>
                    <span>SKU: {item.productSku}</span>
                    <span>Qty {item.quantity}</span>
                    <span><ProductPrice value={item.unitPrice} /></span>
                    <strong><ProductPrice value={item.lineTotal} /></strong>
                  </div>
                </article>
              ))}
            </div>

            <dl className="order-totals">
              <div>
                <dt>Subtotal</dt>
                <dd><ProductPrice value={cart.subtotal} /></dd>
              </div>
              <div>
                <dt>Shipping</dt>
                <dd>₹0.00</dd>
              </div>
              <div>
                <dt>Discount</dt>
                <dd>₹0.00</dd>
              </div>
              <div className="order-totals__total">
                <dt>Total</dt>
                <dd><ProductPrice value={cart.subtotal} /></dd>
              </div>
            </dl>

            <button type="button" className="button button--primary" onClick={handlePlaceOrder} disabled={isPlacingOrder || !selectedAddressId}>
              {isPlacingOrder ? 'Placing order...' : 'Place Order'}
            </button>
            <p className="checkout-note">
              Order totals are calculated by the backend. This page sends only the selected shipping address ID.
            </p>
          </div>
        </div>
      )}
    </section>
  );
}
