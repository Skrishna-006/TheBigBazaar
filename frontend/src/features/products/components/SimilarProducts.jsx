import { useState, useEffect, useMemo } from 'react';
import { getProducts } from '../api/productApi';
import ProductCard from './ProductCard';
import SimilarProductsSkeleton from './SimilarProductsSkeleton';
import LoadingSpinner from '../../../components/common/LoadingSpinner';

export default function SimilarProducts({ currentProduct }) {
  const [catalog, setCatalog] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    let mounted = true;
    async function loadCatalog() {
      try {
        setIsLoading(true);
        const data = await getProducts();
        if (mounted) {
          setCatalog(Array.isArray(data) ? data : data.content || []);
          setError(false);
        }
      } catch (err) {
        if (mounted) setError(true);
      } finally {
        if (mounted) setIsLoading(false);
      }
    }
    loadCatalog();
    return () => { mounted = false; };
  }, []); // Run once when component mounts

  const similarProducts = useMemo(() => {
    if (!currentProduct || catalog.length === 0) return [];
    
    // Filter active and exclude current product
    const candidates = catalog.filter(p => 
      String(p.id) !== String(currentProduct.id) && 
      p.active !== false
    );

    // Scoring
    const scored = candidates.map(p => {
      let score = 0;
      
      // +50 same category
      if (p.category?.id && currentProduct.category?.id && p.category.id === currentProduct.category.id) {
        score += 50;
      }
      
      // +25 same brand
      if (p.brand?.id && currentProduct.brand?.id && p.brand.id === currentProduct.brand.id) {
        score += 25;
      }
      
      // +15 similar price range (within 20%)
      if (p.price && currentProduct.price) {
        const diffRatio = Math.abs(p.price - currentProduct.price) / currentProduct.price;
        if (diffRatio <= 0.2) {
          score += 15;
        } else if (diffRatio <= 0.5) {
          score += 5;
        }
      }
      
      // +10 similar product type/name keywords
      if (p.name && currentProduct.name) {
        const currentNameWords = currentProduct.name.toLowerCase().split(/\s+/);
        const targetNameWords = p.name.toLowerCase().split(/\s+/);
        const commonWords = currentNameWords.filter(w => w.length > 2 && targetNameWords.includes(w));
        if (commonWords.length > 0) {
          score += 10;
        }
      }

      // +5 rating similarity (within 0.5)
      if (p.rating && currentProduct.rating && Math.abs(p.rating - currentProduct.rating) <= 0.5) {
        score += 5;
      }

      return { product: p, score };
    });

    // Filter out 0 scores (must have some genuine similarity)
    const valid = scored.filter(item => item.score >= 10);

    valid.sort((a, b) => b.score - a.score);

    return valid.slice(0, 6).map(item => item.product);
  }, [currentProduct, catalog]);

  if (error || (!isLoading && similarProducts.length < 2)) {
    return null; // gracefully hide if error or fewer than 2 similar products found
  }

  return (
    <section className="similar-products" style={{ marginTop: '3rem', paddingTop: '2rem', borderTop: '1px solid #e2e8f0' }}>
      <div className="section-header" style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#0f172a' }}>Similar Products</h2>
      </div>
      
      {isLoading ? (
        <SimilarProductsSkeleton />
      ) : (
        <div 
          className="similar-products-grid"
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))',
            gap: '1.5rem'
          }}
        >
          {similarProducts.map(p => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}
    </section>
  );
}
