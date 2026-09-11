import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProducts } from '../api/productApi';
import ProductCard from './ProductCard';
import TopDealsSkeleton from './TopDealsSkeleton';
import { getTopDeals } from '../../../utils/productUtils';
import { normalizeApiError } from '../../../utils/apiError';

export default function TopDealsSection() {
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let isMounted = true;

    async function loadDeals() {
      setIsLoading(true);
      setErrorMessage('');

      try {
        const loadedProducts = await getProducts();
        if (isMounted) {
          setProducts(Array.isArray(loadedProducts) ? loadedProducts : []);
        }
      } catch (error) {
        if (isMounted) {
          const normalized = normalizeApiError(error);
          setErrorMessage(normalized.message || 'Unable to load deals at this time.');
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadDeals();

    return () => {
      isMounted = false;
    };
  }, []);

  const topDeals = useMemo(() => {
    return getTopDeals(products, 8);
  }, [products]);

  // Gracefully hide or show fallback if error occurs
  if (errorMessage && !isLoading) {
    return (
      <section className="top-deals" aria-labelledby="top-deals-heading">
        <div className="top-deals__header">
          <div className="top-deals__heading-group">
            <span className="top-deals__accent-badge">BEST OFFERS</span>
            <h2 id="top-deals-heading" className="top-deals__title">Top Deals</h2>
            <p className="top-deals__subtitle">Grab the best deals before they're gone.</p>
          </div>
          <Link to="/products?deals=true" className="top-deals__view-all">
            View All Deals <span aria-hidden="true">&rarr;</span>
          </Link>
        </div>
        <div className="top-deals__notice" role="status">
          <p>Great deals are coming soon. Check back shortly!</p>
        </div>
      </section>
    );
  }

  return (
    <section className="top-deals" aria-labelledby="top-deals-heading">
      <div className="top-deals__header">
        <div className="top-deals__heading-group">
          <span className="top-deals__accent-badge">BEST OFFERS</span>
          <h2 id="top-deals-heading" className="top-deals__title">Top Deals</h2>
          <p className="top-deals__subtitle">Grab the best deals before they're gone.</p>
        </div>
        <Link to="/products?deals=true" className="top-deals__view-all">
          View All Deals <span aria-hidden="true">&rarr;</span>
        </Link>
      </div>

      {isLoading ? (
        <TopDealsSkeleton />
      ) : topDeals.length === 0 ? (
        <div className="top-deals__empty">
          <p>Great deals are coming soon.</p>
        </div>
      ) : (
        <div className="top-deals__grid">
          {topDeals.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </section>
  );
}
