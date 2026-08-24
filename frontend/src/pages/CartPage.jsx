import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { addCartItem, clearCart, getCart, removeCartItem, updateCartItemQuantity } from '../features/cart/api/cartApi';
import CartItemRow from '../features/cart/components/CartItemRow';
import CartSummary from '../features/cart/components/CartSummary';
import { normalizeApiError } from '../utils/apiError';

const emptyAddForm = {
  productId: '',
  quantity: 1,
};

export default function CartPage() {
  const [cart, setCart] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [form, setForm] = useState(emptyAddForm);
  const [updatingProductId, setUpdatingProductId] = useState(null);

  const items = cart?.items || [];

  const cartStats = useMemo(
    () => ({
      totalItemCount: cart?.totalItemCount || 0,
      subtotal: cart?.subtotal || 0,
    }),
    [cart]
  );

  async function loadCart() {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await getCart();
      setCart(data);
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadCart();
  }, []);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: name === 'quantity' ? Number(value) : value }));
  }

  async function handleAddItem(event) {
    event.preventDefault();
    setFieldErrors({});
    setErrorMessage('');
    setSuccessMessage('');

    if (!form.productId.trim()) {
      setFieldErrors({ productId: 'Product ID is required' });
      return;
    }
    if (!form.quantity || form.quantity < 1) {
      setFieldErrors({ quantity: 'Quantity must be at least 1' });
      return;
    }

    setIsSubmitting(true);
    try {
      const data = await addCartItem(form.productId.trim(), form.quantity);
      setCart(data);
      setForm(emptyAddForm);
      setSuccessMessage('Item added to cart.');
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message);
      setFieldErrors(normalized.fieldErrors || {});
    } finally {
      setIsSubmitting(false);
    }
  }

  async function changeQuantity(productId, nextQuantity) {
    if (!nextQuantity || nextQuantity < 1) {
      setErrorMessage('Quantity must be greater than zero');
      return;
    }
    setUpdatingProductId(productId);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      const data = await updateCartItemQuantity(productId, nextQuantity);
      setCart(data);
      setSuccessMessage('Cart updated.');
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setUpdatingProductId(null);
    }
  }

  async function handleRemove(productId) {
    setUpdatingProductId(productId);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      await removeCartItem(productId);
      await loadCart();
      setSuccessMessage('Item removed from cart.');
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setUpdatingProductId(null);
    }
  }

  async function handleClearCart() {
    setIsSubmitting(true);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      await clearCart();
      await loadCart();
      setSuccessMessage('Cart cleared.');
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setIsSubmitting(false);
    }
  }

  if (isLoading) {
    return <LoadingSpinner label="Loading your cart..." />;
  }

  return (
    <section className="cart-page">
      <div className="cart-page__header">
        <div>
          <p className="eyebrow">Shopping basket</p>
          <h1>Your Cart</h1>
          <p>Cart items stay user-owned and do not reserve stock until checkout.</p>
        </div>
        <Link className="button button--secondary" to="/products">
          Continue shopping
        </Link>
      </div>

      {successMessage ? <div className="success-message">{successMessage}</div> : null}
      {errorMessage ? <ErrorMessage title="Cart update failed" message={errorMessage} /> : null}

      <section className="cart-add">
        <h2>Add a product by ID</h2>
        <form className="cart-add__form" onSubmit={handleAddItem} noValidate>
          <div className="form-field">
            <label htmlFor="cart-product-id">Product ID</label>
            <input
              id="cart-product-id"
              name="productId"
              value={form.productId}
              onChange={handleChange}
              aria-invalid={Boolean(fieldErrors.productId)}
            />
            {fieldErrors.productId ? <p className="field-error">{fieldErrors.productId}</p> : null}
          </div>
          <div className="form-field cart-add__quantity">
            <label htmlFor="cart-quantity">Quantity</label>
            <input
              id="cart-quantity"
              name="quantity"
              type="number"
              min="1"
              value={form.quantity}
              onChange={handleChange}
              aria-invalid={Boolean(fieldErrors.quantity)}
            />
            {fieldErrors.quantity ? <p className="field-error">{fieldErrors.quantity}</p> : null}
          </div>
          <button type="submit" className="button button--primary" disabled={isSubmitting}>
            {isSubmitting ? 'Adding...' : 'Add Item'}
          </button>
        </form>
      </section>

      {!items.length ? (
        <EmptyState
          title="Your cart is empty."
          message={
            <span>
              Browse products and add items from the product detail page.
            </span>
          }
        />
      ) : (
        <div className="cart-layout">
          <div className="cart-items">
            {items.map((item) => (
              <CartItemRow
                key={item.productId}
                item={item}
                isUpdating={updatingProductId === item.productId}
                onDecrease={() => changeQuantity(item.productId, item.quantity - 1)}
                onIncrease={() => changeQuantity(item.productId, item.quantity + 1)}
                onSetQuantity={(nextQuantity) => changeQuantity(item.productId, nextQuantity)}
                onRemove={() => handleRemove(item.productId)}
              />
            ))}
          </div>

          <CartSummary
            totalItemCount={cartStats.totalItemCount}
            subtotal={cartStats.subtotal}
            onClearCart={handleClearCart}
            isClearing={isSubmitting}
          />
        </div>
      )}
    </section>
  );
}
