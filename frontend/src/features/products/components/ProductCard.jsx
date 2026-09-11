import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import ProductPrice from './ProductPrice';
import { useAuth } from '../../auth/context/AuthContext';
import { addCartItem } from '../../cart/api/cartApi';
import { normalizeApiError } from '../../../utils/apiError';
import WishlistButton from '../../wishlist/components/WishlistButton';
import { calculateDiscount, calculateDeliveryDate } from '../../../utils/productUtils';

const placeholderImage =
  'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns%3D%22http%3A//www.w3.org/2000/svg%22 width%3D%22960%22 height%3D%22720%22 viewBox%3D%220 0 960 720%22%3E%3Crect width%3D%22960%22 height%3D%22720%22 rx%3D%2232%22 fill%3D%22%23e2e8f0%22/%3E%3Cpath d%3D%22M240 500l120-140 92 106 80-88 188 222H240z%22 fill%3D%22%23cbd5e1%22/%3E%3Ccircle cx%3D%22364%22 cy%3D%22306%22 r%3D%2264%22 fill%3D%22%23cbd5e1%22/%3E%3C/svg%3E';

function safeImageSrc(imageUrl) {
  return typeof imageUrl === 'string' && imageUrl.trim() ? imageUrl : placeholderImage;
}

export default function ProductCard({ product }) {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [isAdding, setIsAdding] = useState(false);
  const [successMessage, setSuccessMessage] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);

  const handleAddToCart = async (e) => {
    e.preventDefault(); // Prevent linking if nested
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    setIsAdding(true);
    setSuccessMessage(null);
    setErrorMessage(null);

    try {
      await addCartItem(product.id, 1);
      setSuccessMessage('Added to cart');
    } catch (err) {
      setErrorMessage(normalizeApiError(err).message);
    } finally {
      setIsAdding(false);
    }
  };

  const discount = calculateDiscount(product.originalPrice, product.price);
  const deliveryDate = calculateDeliveryDate(product.deliveryDays);

  return (
    <article className="product-card" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Link to={`/products/${product.id}`} className="product-card__media-link">
        <img
          className="product-card__image"
          src={safeImageSrc(product.imageUrl)}
          alt={`${product.name} product image`}
          onError={(event) => {
            event.currentTarget.src = placeholderImage;
          }}
        />
        {discount != null && discount > 0 && (
          <span className="product-card__badge product-card__badge--discount">
            {discount}% OFF
          </span>
        )}
      </Link>
      <div className="product-card__body" style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
        <h3 className="product-card__title" style={{ marginBottom: '0.25rem' }}>
          <Link to={`/products/${product.id}`}>{product.name}</Link>
        </h3>

        {product.rating != null && (
          <div style={{ fontSize: '0.875rem', color: '#555', marginBottom: '0.5rem' }}>
            ⭐ {product.rating.toFixed(1)} ({product.reviewCount || 0} ratings)
          </div>
        )}
        
        <div style={{ marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
          <ProductPrice value={product.price} />
          {discount && (
            <>
              <span style={{ textDecoration: 'line-through', color: '#888', fontSize: '0.875rem' }}>
                <ProductPrice value={product.originalPrice} />
              </span>
              <span style={{ color: '#d9534f', fontWeight: 'bold', fontSize: '0.875rem' }}>
                {discount}% OFF
              </span>
            </>
          )}
        </div>

        {deliveryDate && (
          <div style={{ fontSize: '0.875rem', color: '#444', marginBottom: '0.75rem' }}>
            {product.deliveryCharge === 0 || product.deliveryCharge == null
              ? `🚚 Free delivery by ${deliveryDate}`
              : `🚚 Delivery ₹${product.deliveryCharge} by ${deliveryDate}`}
          </div>
        )}

        <div className="product-card__actions" style={{ marginTop: 'auto', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <Link className="button button--secondary button--small" to={`/products/${product.id}`} style={{ flex: 1, textAlign: 'center' }}>
              View details
            </Link>
            <button
              type="button"
              className="button button--primary button--small"
              style={{ flex: 1 }}
              onClick={handleAddToCart}
              disabled={isAdding}
            >
              {isAdding ? 'Adding...' : 'Add to cart'}
            </button>
          </div>
          <WishlistButton productId={product.id} productName={product.name} />
        </div>
        {(successMessage || errorMessage) && (
          <div style={{ marginTop: '0.75rem', fontSize: '0.875rem' }}>
            {successMessage && <span style={{ color: 'var(--color-success-dark, green)' }}>{successMessage}</span>}
            {errorMessage && <span style={{ color: 'var(--color-danger-dark, red)' }}>{errorMessage}</span>}
          </div>
        )}
      </div>
    </article>
  );
}
