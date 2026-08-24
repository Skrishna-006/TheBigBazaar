import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/context/AuthContext';
import { addToWishlist, isWishlisted as checkWishlisted, removeFromWishlist } from '../api/wishlistApi';
import { normalizeApiError } from '../../../utils/apiError';

export default function WishlistButton({ productId, productName, className = '' }) {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [wishlisted, setWishlisted] = useState(false);
  const [isChecking, setIsChecking] = useState(true);
  const [isUpdating, setIsUpdating] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    let mounted = true;

    async function loadStatus() {
      if (!isAuthenticated || !productId) {
        if (mounted) {
          setIsChecking(false);
        }
        return;
      }

      setIsChecking(true);
      try {
        const data = await checkWishlisted(productId);
        if (mounted) {
          setWishlisted(Boolean(data?.wishlisted));
        }
      } catch {
        if (mounted) {
          setWishlisted(false);
        }
      } finally {
        if (mounted) {
          setIsChecking(false);
        }
      }
    }

    loadStatus();

    return () => {
      mounted = false;
    };
  }, [isAuthenticated, productId]);

  async function handleClick() {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/products/${productId}` } } });
      return;
    }

    setIsUpdating(true);
    setMessage('');
    try {
      if (wishlisted) {
        await removeFromWishlist(productId);
        setWishlisted(false);
        setMessage('Removed from wishlist.');
      } else {
        await addToWishlist(productId);
        setWishlisted(true);
        setMessage('Added to wishlist.');
      }
    } catch (error) {
      setMessage(normalizeApiError(error).message);
    } finally {
      setIsUpdating(false);
    }
  }

  return (
    <div className={`wishlist-button-group ${className}`.trim()}>
      <button
        type="button"
        className={`button ${wishlisted ? 'button--secondary' : 'button--primary'}`}
        onClick={handleClick}
        disabled={isChecking || isUpdating}
        aria-busy={isChecking || isUpdating}
        aria-label={wishlisted ? `Remove ${productName} from wishlist` : `Add ${productName} to wishlist`}
      >
        {isChecking ? 'Checking...' : isUpdating ? (wishlisted ? 'Removing...' : 'Adding...') : wishlisted ? 'Remove from wishlist' : 'Add to wishlist'}
      </button>
      {message ? <p className="wishlist-button__message">{message}</p> : null}
    </div>
  );
}
