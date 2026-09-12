import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProducts } from '../api/productApi';
import ProductCard from './ProductCard';
import TrendingNowSkeleton from './TrendingNowSkeleton';
import { getTrendingProducts } from '../../../utils/productUtils';
import { normalizeApiError } from '../../../utils/apiError';

export default function TrendingNowSection() {
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let isMounted = true;

    async function loadTrending() {
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
          setErrorMessage(normalized.message || 'Unable to load trending products at this time.');
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadTrending();

    return () => {
      isMounted = false;
    };
  }, []);

  const trendingProducts = useMemo(() => {
    return getTrendingProducts(products, 8);
  }, [products]);

  // If there are no eligible products and it finished loading without error, render nothing.
  if (!isLoading && !errorMessage && trendingProducts.length === 0) {
    return null;
  }

  // Gracefully hide or show fallback if error occurs
  if (errorMessage && !isLoading) {
    return (
      <section className="trending-now" aria-labelledby="trending-now-heading">
        <div className="trending-now-header">
          <div className="trending-now-header__group">
            <span className="trending-now-header__accent">WHAT'S HOT</span>
            <h2 id="trending-now-heading" className="trending-now-header__title">Trending Now</h2>
            <p className="trending-now-header__subtitle">Popular picks customers are loving right now.</p>
          </div>
        </div>
        <div className="trending-now-notice" role="status">
          <p>Trending products will be available shortly.</p>
        </div>
      </section>
    );
  }

  return (
    <section className="trending-now" aria-labelledby="trending-now-heading">
      <div className="trending-now-header">
        <div className="trending-now-header__group">
          <span className="trending-now-header__accent">WHAT'S HOT</span>
          <h2 id="trending-now-heading" className="trending-now-header__title">Trending Now</h2>
          <p className="trending-now-header__subtitle">Popular picks customers are loving right now.</p>
        </div>
        <Link to="/products?sort=rating_desc" className="trending-now-header__view-all">
          View All <span aria-hidden="true">&rarr;</span>
        </Link>
      </div>

      {isLoading ? (
        <TrendingNowSkeleton />
      ) : (
        <div className="trending-now-grid">
          {trendingProducts.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </section>
  );
}
