import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getProducts } from '../../products/api/productApi';

let cachedDerivedBrands = null;

export default function ShopByBrand() {
  const [brands, setBrands] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    let mounted = true;
    async function loadBrandsFromProducts() {
      if (cachedDerivedBrands) {
        setBrands(cachedDerivedBrands);
        setIsLoading(false);
        return;
      }
      try {
        const products = await getProducts();
        if (mounted) {
          const brandMap = new Map();
          
          if (Array.isArray(products)) {
            for (const product of products) {
              // Skip inactive products if the flag exists and is explicitly false
              if (product.isActive === false) continue;
              
              if (product.brand && product.brand.id && product.brand.name) {
                brandMap.set(product.brand.id, {
                  id: product.brand.id,
                  name: product.brand.name
                });
              }
            }
          }
          
          const derived = Array.from(brandMap.values());
          cachedDerivedBrands = derived;
          setBrands(derived);
        }
      } catch (err) {
        if (mounted) {
          setError(true);
        }
        console.error('Failed to load products for Shop by Brand section', err);
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    loadBrandsFromProducts();
    return () => {
      mounted = false;
    };
  }, []);

  if (error) {
    return (
      <section className="shop-by-brand">
        <div className="shop-by-brand__header">
          <h2 className="shop-by-brand__title">Shop by Brand</h2>
        </div>
        <div className="shop-by-brand__empty">
          <p>Brands will be available soon.</p>
        </div>
      </section>
    );
  }

  return (
    <section className="shop-by-brand">
      <div className="shop-by-brand__header">
        <h2 className="shop-by-brand__title">Shop by Brand</h2>
      </div>

      {isLoading ? (
        <div className="shop-by-brand__grid">
          {Array.from({ length: 6 }).map((_, index) => (
            <div key={index} className="shop-by-brand__skeleton-card" />
          ))}
        </div>
      ) : brands.length === 0 ? (
        <div className="shop-by-brand__empty">
          <p>Brands will be available soon.</p>
        </div>
      ) : (
        <div className="shop-by-brand__grid">
          {brands.map((brand) => {
            if (!brand || !brand.id) return null;
            
            return (
              <Link 
                key={brand.id} 
                to={`/products?brandId=${brand.id}`} 
                className="shop-by-brand__card"
              >
                {brand.name}
              </Link>
            );
          })}
        </div>
      )}
    </section>
  );
}
