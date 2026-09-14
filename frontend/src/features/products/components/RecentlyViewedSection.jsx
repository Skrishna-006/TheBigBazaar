import { useEffect, useState, useMemo } from 'react';
import { getProducts } from '../api/productApi';
import ProductCard from './ProductCard';
import RecentlyViewedSkeleton from './RecentlyViewedSkeleton';
import { getRecentlyViewedIds, removeRecentlyViewed } from '../../../utils/recentlyViewedUtils';

export default function RecentlyViewedSection() {
  const [productIds, setProductIds] = useState([]);
  const [catalog, setCatalog] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // Sync with localStorage on mount and when the custom event fires
  useEffect(() => {
    function loadIds() {
      setProductIds(getRecentlyViewedIds());
    }
    
    loadIds(); // Initial load
    
    window.addEventListener('thebigbazaar:recently-viewed-updated', loadIds);
    return () => {
      window.removeEventListener('thebigbazaar:recently-viewed-updated', loadIds);
    };
  }, []);

  // Fetch the full catalog once per mount (if we have any IDs to map against)
  useEffect(() => {
    let isMounted = true;
    
    // We only fetch the catalog if the user actually has history to avoid unnecessary bandwidth
    if (productIds.length === 0 && catalog.length === 0) {
      setIsLoading(false);
      return;
    }

    // Only fetch once when the component first realizes it needs data
    if (catalog.length > 0) return;

    async function loadCatalog() {
      setIsLoading(true);
      try {
        const loadedProducts = await getProducts();
        if (isMounted && Array.isArray(loadedProducts)) {
          setCatalog(loadedProducts);
        }
      } catch (error) {
        // We fail silently and don't render recently viewed rather than breaking the homepage
        console.warn('Recently Viewed catalog load error:', error);
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadCatalog();
    
    return () => {
      isMounted = false;
    };
  }, [productIds.length, catalog.length]);

  // Match IDs to active products
  const displayProducts = useMemo(() => {
    if (productIds.length === 0 || catalog.length === 0) return [];
    
    const validProducts = [];
    
    productIds.forEach(id => {
      const product = catalog.find(p => p.id === id);
      
      if (!product) {
        // Product no longer exists in catalog
        removeRecentlyViewed(id);
      } else if (product.isActive === false) {
        // Product became inactive
        removeRecentlyViewed(id);
      } else {
        validProducts.push(product);
      }
    });
    
    return validProducts;
  }, [productIds, catalog]);

  if (isLoading && productIds.length > 0) {
    return (
      <section className="recently-viewed" aria-labelledby="recently-viewed-heading">
        <div className="recently-viewed-header">
          <h2 id="recently-viewed-heading" className="recently-viewed-header__title">Recently Viewed</h2>
          <p className="recently-viewed-header__subtitle">Pick up where you left off</p>
        </div>
        <RecentlyViewedSkeleton />
      </section>
    );
  }

  if (displayProducts.length === 0) {
    return null; // Do not occupy space if empty
  }

  return (
    <section className="recently-viewed" aria-labelledby="recently-viewed-heading">
      <div className="recently-viewed-header">
        <h2 id="recently-viewed-heading" className="recently-viewed-header__title">Recently Viewed</h2>
        <p className="recently-viewed-header__subtitle">Pick up where you left off</p>
      </div>

      <div className="recently-viewed-grid">
        {displayProducts.map(product => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </section>
  );
}
