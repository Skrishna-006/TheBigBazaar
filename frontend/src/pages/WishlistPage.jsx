import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { addCartItem } from '../features/cart/api/cartApi';
import { getWishlist, removeFromWishlist } from '../features/wishlist/api/wishlistApi';
import WishlistGrid from '../features/wishlist/components/WishlistGrid';
import { normalizeApiError } from '../utils/apiError';

export default function WishlistPage() {
  const [wishlist, setWishlist] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [removingProductId, setRemovingProductId] = useState(null);
  const [addingProductId, setAddingProductId] = useState(null);

  async function loadWishlist() {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await getWishlist();
      setWishlist(data);
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadWishlist();
  }, []);

  async function handleRemove(productId) {
    setRemovingProductId(productId);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      await removeFromWishlist(productId);
      setWishlist((current) => {
        if (!current) {
          return current;
        }
        const items = current.items.filter((item) => item.productId !== productId);
        return { ...current, items, totalItems: items.length };
      });
      setSuccessMessage('Removed from wishlist.');
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setRemovingProductId(null);
    }
  }

  async function handleAddToCart(item) {
    setAddingProductId(item.productId);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      await addCartItem(item.productId, 1);
      
      try {
        await removeFromWishlist(item.productId);
        setWishlist((current) => {
          if (!current) {
            return current;
          }
          const items = current.items.filter((i) => i.productId !== item.productId);
          return { ...current, items, totalItems: items.length };
        });
        setSuccessMessage('Added to cart and removed from wishlist.');
      } catch (removeError) {
        setErrorMessage(`Added to cart, but could not remove from wishlist: ${normalizeApiError(removeError).message}`);
      }
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setAddingProductId(null);
    }
  }

  if (isLoading) {
    return <LoadingSpinner label="Loading your wishlist..." />;
  }

  return (
    <section className="wishlist-page">
      <div className="wishlist-page__header">
        <div>
          <p className="eyebrow">Saved items</p>
          <h1>Your Wishlist</h1>
          <p>Save products here, compare them later, and move them to cart when you are ready.</p>
        </div>
        <Link className="button button--secondary" to="/products">
          Browse Products
        </Link>
      </div>

      {successMessage ? <div className="success-message">{successMessage}</div> : null}
      {errorMessage ? <ErrorMessage title="Wishlist update failed" message={errorMessage} /> : null}

      <WishlistGrid
        items={wishlist?.items || []}
        removingProductId={removingProductId}
        addingProductId={addingProductId}
        onRemove={handleRemove}
        onAddToCart={handleAddToCart}
      />
    </section>
  );
}
