import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { useAuth } from '../features/auth/context/AuthContext';
import { addCartItem } from '../features/cart/api/cartApi';
import { getProductById } from '../features/products/api/productApi';
import ProductPrice from '../features/products/components/ProductPrice';
import WishlistButton from '../features/wishlist/components/WishlistButton';
import { normalizeApiError } from '../utils/apiError';
import { calculateDiscount, calculateDeliveryDate } from '../utils/productUtils';
import { addRecentlyViewed } from '../utils/recentlyViewedUtils';
import { ProductDescription, DeliveryInformation, ProductHighlights, ProductSpecifications, SoldBy, ProductReviews } from '../features/products/components/ProductInfoSections';
import SimilarProducts from '../features/products/components/SimilarProducts';

const placeholderImage =
  'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns%3D%22http%3A//www.w3.org/2000/svg%22 width%3D%22960%22 height%3D%22720%22 viewBox%3D%220 0 960 720%22%3E%3Crect width%3D%22960%22 height%3D%22720%22 rx%3D%2232%22 fill%3D%22%23e2e8f0%22/%3E%3Cpath d%3D%22M240 500l120-140 92 106 80-88 188 222H240z%22 fill%3D%22%23cbd5e1%22/%3E%3Ccircle cx%3D%22364%22 cy%3D%22306%22 r%3D%2264%22 fill%3D%22%23cbd5e1%22/%3E%3C/svg%3E';

export default function ProductDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [product, setProduct] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [notFound, setNotFound] = useState(false);
  const [isAddingToCart, setIsAddingToCart] = useState(false);
  const [cartMessage, setCartMessage] = useState('');

  useEffect(() => {
    let mounted = true;

    async function loadProduct() {
      setIsLoading(true);
      setErrorMessage('');
      setNotFound(false);

      try {
        const data = await getProductById(id);
        if (mounted) {
          setProduct(data);
          if (data && data.id) {
            addRecentlyViewed(data.id);
          }
        }
      } catch (error) {
        const normalized = normalizeApiError(error);
        if (mounted) {
          if (normalized.status === 404) {
            setNotFound(true);
          } else {
            setErrorMessage(normalized.message);
          }
          setProduct(null);
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    loadProduct();

    return () => {
      mounted = false;
    };
  }, [id]);

  if (isLoading) {
    return <LoadingSpinner label="Loading product..." />;
  }

  if (notFound) {
    return <ErrorMessage title="Product not found" message="This product is no longer available." />;
  }

  if (errorMessage) {
    return <ErrorMessage title="Unable to load product" message={errorMessage} />;
  }

  if (!product) {
    return null;
  }

  async function handleAddToCart() {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/products/${id}` } } });
      return;
    }

    setIsAddingToCart(true);
    setErrorMessage('');
    setCartMessage('');

    try {
      await addCartItem(product.id, 1);
      setCartMessage('Added to cart.');
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setIsAddingToCart(false);
    }
  }

  const discount = calculateDiscount(product.originalPrice, product.price);
  const deliveryDate = calculateDeliveryDate(product.deliveryDays);

  return (
    <section className="product-detail">
      <Link className="product-detail__back" to="/products">
        ← Back to products
      </Link>
      <div className="product-detail__card">
        <div className="product-detail__media">
          <img
            className="product-detail__image"
            src={product.imageUrl || placeholderImage}
            alt={`${product.name} product image`}
            onError={(event) => {
              event.currentTarget.src = placeholderImage;
            }}
          />
        </div>
        <div className="product-detail__info">
          <p className="eyebrow">Product details</p>
          <h1>{product.name}</h1>
          <div className="product-detail__meta">
            <span>{product.brand?.name || 'Brand coming soon'}</span>
            <span>&middot;</span>
            <span>{product.category?.name || 'Category coming soon'}</span>
            <br />
            <span>SKU: {product.sku}</span>
          </div>

          {product.rating != null && (
            <div style={{ fontSize: '1rem', color: '#555', marginTop: '0.75rem', marginBottom: '1rem' }}>
              ⭐ {product.rating.toFixed(1)} ({product.reviewCount || 0} ratings)
            </div>
          )}

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap', marginBottom: '0.5rem', marginTop: product.rating == null ? '1rem' : '0' }}>
            <ProductPrice value={product.price} />
            {discount && (
              <>
                <span style={{ textDecoration: 'line-through', color: '#888', fontSize: '1rem' }}>
                  <ProductPrice value={product.originalPrice} />
                </span>
                <span style={{ color: '#d9534f', fontWeight: 'bold', fontSize: '1rem' }}>
                  {discount}% OFF
                </span>
              </>
            )}
          </div>

          {cartMessage ? <div className="success-message">{cartMessage}</div> : null}
          <div className="product-detail__actions">
            <button type="button" className="button button--primary" onClick={handleAddToCart} disabled={isAddingToCart}>
              {isAuthenticated ? (isAddingToCart ? 'Adding...' : 'Add to cart') : 'Login to add to cart'}
            </button>
            <WishlistButton productId={product.id} productName={product.name} />
            <Link className="button button--secondary" to="/cart">
              View cart
            </Link>
          </div>
          <p className="product-detail__note">Cart keeps the current product price and revalidates stock later.</p>
        </div>
      </div>

      <div className="product-extended-info">
        <ProductDescription description={product.description} />
        <ProductHighlights highlights={product.highlights} />
        <ProductSpecifications specifications={product.specifications} />
        <SoldBy seller={product.seller} />
        <DeliveryInformation deliveryDate={deliveryDate} deliveryCharge={product.deliveryCharge} />
        <ProductReviews productId={product.id} initialRating={product.rating} initialReviewCount={product.reviewCount} />
        <SimilarProducts currentProduct={product} />
      </div>
    </section>
  );
}
