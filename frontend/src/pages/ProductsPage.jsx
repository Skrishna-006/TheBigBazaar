import React, { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { getProducts } from '../features/products/api/productApi';
import ProductGrid from '../features/products/components/ProductGrid';
import FilterSidebar from '../features/products/components/FilterSidebar';
import MobileFilterDrawer from '../features/products/components/MobileFilterDrawer';
import ActiveFilterChips from '../features/products/components/ActiveFilterChips';
import ProductPagination from '../features/products/components/ProductPagination';
import { normalizeApiError } from '../utils/apiError';
import { getTopDeals, calculateDiscount } from '../utils/productUtils';

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [allProducts, setAllProducts] = useState([]);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [isMobileDrawerOpen, setIsMobileDrawerOpen] = useState(false);

  // URL Parameters
  const searchQuery = searchParams.get('search') || '';
  const showDeals = searchParams.get('deals') === 'true';
  const categoryId = searchParams.get('categoryId') || '';
  const brandId = searchParams.get('brandId') || '';
  const minPrice = searchParams.get('minPrice') || '';
  const maxPrice = searchParams.get('maxPrice') || '';
  const rating = searchParams.get('rating') || '';
  const discount = searchParams.get('discount') || '';
  const delivery = searchParams.get('delivery') || '';
  const sort = searchParams.get('sort') || '';

  const activeFilters = useMemo(() => ({
    search: searchQuery,
    deals: showDeals ? 'true' : '',
    categoryId,
    brandId,
    minPrice,
    maxPrice,
    rating,
    discount,
    delivery,
    sort
  }), [searchQuery, showDeals, categoryId, brandId, minPrice, maxPrice, rating, discount, delivery, sort]);

  useEffect(() => {
    let mounted = true;
    async function fetchCatalog() {
      setIsInitialLoading(true);
      setErrorMessage('');
      try {
        const loadedProducts = await getProducts();
        if (mounted) {
          setAllProducts(loadedProducts || []);
        }
      } catch (error) {
        if (mounted) {
          setErrorMessage(normalizeApiError(error).message);
        }
      } finally {
        if (mounted) {
          setIsInitialLoading(false);
        }
      }
    }
    fetchCatalog();
    return () => { mounted = false; };
  }, []);

  // 1. BASE FILTER (Search & Deals)
  const baseProducts = useMemo(() => {
    let result = allProducts;
    
    // Ignore inactive products if property explicitly exists
    result = result.filter(p => p.isActive !== false);

    // Apply Top Deals filtering using existing logic
    if (showDeals) {
      // getTopDeals returns top N, but we want all valid deals. Wait, getTopDeals slices to limit.
      // Let's filter manually based on getTopDeals requirement to preserve all deals:
      result = result.filter(p => {
        const orig = Number(p.originalPrice);
        const curr = Number(p.price);
        return (Number.isFinite(orig) && Number.isFinite(curr) && orig > 0 && curr >= 0 && orig > curr);
      });
    }

    if (searchQuery) {
      const q = searchQuery.toLowerCase().trim();
      result = result.filter((p) => {
        const matchName = p.name?.toLowerCase().includes(q);
        const matchBrand = p.brand?.name?.toLowerCase().includes(q);
        const matchCategory = p.category?.name?.toLowerCase().includes(q);
        const matchSku = p.sku?.toLowerCase().includes(q);
        return matchName || matchBrand || matchCategory || matchSku;
      });
    }
    return result;
  }, [allProducts, showDeals, searchQuery]);

  // 2. CONTEXT AWARE OPTIONS
  const { availableCategories, availableBrands } = useMemo(() => {
    const catMap = new Map();
    const brandMap = new Map();

    // To compute Categories, filter by all independent filters PLUS current Brand (ignore Category)
    baseProducts.forEach(p => {
      let matchCat = true;
      if (brandId && p.brand?.id !== brandId) matchCat = false;
      if (minPrice && Number(p.price) < Number(minPrice)) matchCat = false;
      if (maxPrice && Number(p.price) > Number(maxPrice)) matchCat = false;
      if (rating && (Number(p.rating) || 0) < Number(rating)) matchCat = false;
      if (discount && (calculateDiscount(p.originalPrice, p.price) || 0) < Number(discount)) matchCat = false;
      if (delivery) {
        if (delivery === 'free' && p.deliveryCharge > 0) matchCat = false;
        if (delivery !== 'free' && (p.deliveryDays == null || p.deliveryDays > Number(delivery))) matchCat = false;
      }
      if (matchCat && p.category?.id) catMap.set(p.category.id, p.category);
    });

    // To compute Brands, filter by all independent filters PLUS current Category (ignore Brand)
    baseProducts.forEach(p => {
      let matchBrand = true;
      if (categoryId && p.category?.id !== categoryId) matchBrand = false;
      if (minPrice && Number(p.price) < Number(minPrice)) matchBrand = false;
      if (maxPrice && Number(p.price) > Number(maxPrice)) matchBrand = false;
      if (rating && (Number(p.rating) || 0) < Number(rating)) matchBrand = false;
      if (discount && (calculateDiscount(p.originalPrice, p.price) || 0) < Number(discount)) matchBrand = false;
      if (delivery) {
        if (delivery === 'free' && p.deliveryCharge > 0) matchBrand = false;
        if (delivery !== 'free' && (p.deliveryDays == null || p.deliveryDays > Number(delivery))) matchBrand = false;
      }
      if (matchBrand && p.brand?.id) brandMap.set(p.brand.id, p.brand);
    });

    // ALWAYS ensure currently selected category/brand remains in the list if it was somehow excluded
    // (e.g. invalid combination or zero results, we still want the radio button to show it's selected)
    if (categoryId && !catMap.has(categoryId)) {
      const fallbackCat = allProducts.find(p => p.category?.id === categoryId)?.category;
      if (fallbackCat) catMap.set(fallbackCat.id, fallbackCat);
    }
    if (brandId && !brandMap.has(brandId)) {
      const fallbackBrand = allProducts.find(p => p.brand?.id === brandId)?.brand;
      if (fallbackBrand) brandMap.set(fallbackBrand.id, fallbackBrand);
    }

    return {
      availableCategories: Array.from(catMap.values()),
      availableBrands: Array.from(brandMap.values())
    };
  }, [baseProducts, allProducts, categoryId, brandId, minPrice, maxPrice, rating, discount, delivery]);

  // Ensure selected names are available for Active Chips
  const selectedCategoryName = availableCategories.find(c => c.id === categoryId)?.name || 'Category';
  const selectedBrandName = availableBrands.find(b => b.id === brandId)?.name || 'Brand';
  const chipFilters = { ...activeFilters, categoryName: categoryId ? selectedCategoryName : '', brandName: brandId ? selectedBrandName : '' };

  // 3. FINAL FILTERING (Applying Category, Brand, and Independent Filters)
  const filteredProducts = useMemo(() => {
    return baseProducts.filter(p => {
      // Category
      if (categoryId && p.category?.id !== categoryId && categoryId !== 'undefined' && categoryId !== 'null') return false;
      
      // Brand
      if (brandId && p.brand?.id !== brandId && brandId !== 'undefined' && brandId !== 'null') return false;
      
      // Price
      const price = Number(p.price);
      if (minPrice && price < Number(minPrice)) return false;
      if (maxPrice && price > Number(maxPrice)) return false;
      
      // Rating
      if (rating && (Number(p.rating) || 0) < Number(rating)) return false;
      
      // Discount
      if (discount) {
        const d = calculateDiscount(p.originalPrice, p.price) || 0;
        if (d < Number(discount)) return false;
      }
      
      // Delivery
      if (delivery) {
        if (delivery === 'free' && p.deliveryCharge > 0) return false;
        if (delivery !== 'free' && (p.deliveryDays == null || p.deliveryDays > Number(delivery))) return false;
      }
      
      return true;
    });
  }, [baseProducts, categoryId, brandId, minPrice, maxPrice, rating, discount, delivery]);

  // 4. SORTING
  const sortedProducts = useMemo(() => {
    const arr = [...filteredProducts];
    
    if (sort === 'price_asc') {
      arr.sort((a, b) => Number(a.price) - Number(b.price));
    } else if (sort === 'price_desc') {
      arr.sort((a, b) => Number(b.price) - Number(a.price));
    } else if (sort === 'rating_desc') {
      arr.sort((a, b) => (Number(b.rating) || 0) - (Number(a.rating) || 0));
    } else if (sort === 'discount_desc') {
      arr.sort((a, b) => {
        const d1 = calculateDiscount(a.originalPrice, a.price) || 0;
        const d2 = calculateDiscount(b.originalPrice, b.price) || 0;
        return d2 - d1;
      });
    } else if (sort === 'newest') {
      arr.sort((a, b) => {
        const t1 = a.createdAt ? new Date(a.createdAt).getTime() : 0;
        const t2 = b.createdAt ? new Date(b.createdAt).getTime() : 0;
        return t2 - t1; // fallback to 0 if missing, won't crash
      });
    } else {
      // Default: Relevance
      if (showDeals) {
        // Top Deals ranking logic: Discount > Rating > ReviewCount
        arr.sort((a, b) => {
          const d1 = calculateDiscount(a.originalPrice, a.price) || 0;
          const d2 = calculateDiscount(b.originalPrice, b.price) || 0;
          if (d2 !== d1) return d2 - d1;
          const r1 = Number(a.rating) || 0;
          const r2 = Number(b.rating) || 0;
          if (r2 !== r1) return r2 - r1;
          const rc1 = Number(a.reviewCount) || 0;
          const rc2 = Number(b.reviewCount) || 0;
          return rc2 - rc1;
        });
      } else if (searchQuery) {
        // Search relevance
        const q = searchQuery.toLowerCase().trim();
        arr.sort((a, b) => {
          const aExact = a.name?.toLowerCase() === q ? 1 : 0;
          const bExact = b.name?.toLowerCase() === q ? 1 : 0;
          if (bExact !== aExact) return bExact - aExact;
          
          const aStarts = a.name?.toLowerCase().startsWith(q) ? 1 : 0;
          const bStarts = b.name?.toLowerCase().startsWith(q) ? 1 : 0;
          if (bStarts !== aStarts) return bStarts - aStarts;
          
          return 0; // maintain stable order for the rest
        });
      }
    }
    
    return arr;
  }, [filteredProducts, sort, showDeals, searchQuery]);

  // URL Updates
  const handleFilterChange = (type, value) => {
    const next = new URLSearchParams(searchParams);
    next.delete('page'); // Reset to page 1 on any filter change
    
    if (type === 'price') {
      if (value.min) next.set('minPrice', value.min); else next.delete('minPrice');
      if (value.max) next.set('maxPrice', value.max); else next.delete('maxPrice');
    } else {
      if (value) {
        next.set(type, value);
      } else {
        next.delete(type);
      }
    }
    
    setSearchParams(next, { replace: true });
  };

  const handleRemoveFilter = (type) => {
    const next = new URLSearchParams(searchParams);
    next.delete('page'); // Reset to page 1
    
    if (type === 'price') {
      next.delete('minPrice');
      next.delete('maxPrice');
    } else {
      next.delete(type);
    }
    setSearchParams(next, { replace: true });
  };

  const clearAllFilters = () => {
    const next = new URLSearchParams();
    if (searchQuery) next.set('search', searchQuery);
    if (showDeals) next.set('deals', 'true');
    setSearchParams(next, { replace: true });
  };

  const handlePageChange = (newPage) => {
    const next = new URLSearchParams(searchParams);
    next.set('page', newPage);
    setSearchParams(next);
    window.scrollTo({ top: 0, behavior: 'smooth' }); // Scroll behavior requested
  };

  if (isInitialLoading) {
    return <LoadingSpinner label="Loading products..." />;
  }

  // 5. PAGINATION SLICING
  const PRODUCTS_PER_PAGE = 12;
  const totalItems = sortedProducts.length;
  const totalPages = Math.ceil(totalItems / PRODUCTS_PER_PAGE);

  const rawPage = parseInt(searchParams.get('page'), 10);
  let currentPage = 1;
  if (!isNaN(rawPage) && rawPage > 0) {
    currentPage = rawPage;
    if (totalPages > 0 && currentPage > totalPages) {
      currentPage = totalPages; // clamp
    }
  }

  const startIndex = (currentPage - 1) * PRODUCTS_PER_PAGE;
  const endIndex = startIndex + PRODUCTS_PER_PAGE;
  const currentPageProducts = sortedProducts.slice(startIndex, endIndex);

  // Derived labels
  const startCount = totalItems === 0 ? 0 : startIndex + 1;
  const endCount = Math.min(endIndex, totalItems);
  
  let countText = `Showing ${startCount}–${endCount} of ${totalItems} products`;
  if (showDeals && !searchQuery) countText = `Showing ${startCount}–${endCount} of ${totalItems} deals`;
  else if (searchQuery) countText = `Showing ${startCount}–${endCount} of ${totalItems} products for "${searchQuery}"`;

  return (
    <section className="catalog-page">
      <div className="catalog-page__header">
        <div>
          {showDeals ? (
            <>
              <p className="eyebrow">Top Deals</p>
              <h1>{searchQuery ? `Search Results for "${searchQuery}" in Deals` : 'Top Deals'}</h1>
              <p>Discover the best discounts available right now.</p>
            </>
          ) : (
            <>
              <p className="eyebrow">Browse the catalog</p>
              <h1>{searchQuery ? `Search Results for "${searchQuery}"` : 'Products'}</h1>
              <p>Discover active TheBigBazaar products and narrow them by category or brand.</p>
            </>
          )}
        </div>
      </div>

      {errorMessage ? <ErrorMessage title="Unable to load catalog" message={errorMessage} /> : null}

      {!errorMessage && (
        <div className="catalog-layout">
          {/* Desktop Sidebar */}
          <aside className="catalog-sidebar">
            <FilterSidebar
              filters={activeFilters}
              categories={availableCategories}
              brands={availableBrands}
              onFilterChange={handleFilterChange}
              onClearAll={clearAllFilters}
            />
          </aside>

          {/* Main Grid Area */}
          <main className="catalog-main">
            <div className="catalog-toolbar">
              <span className="catalog-toolbar__count">{countText}</span>
              
              <div className="catalog-toolbar__actions">
                <button 
                  className="button button--secondary catalog-toolbar__mobile-filter"
                  onClick={() => setIsMobileDrawerOpen(true)}
                >
                  Filters
                </button>
                
                <div className="sort-control">
                  <label htmlFor="sort-select">Sort By:</label>
                  <select 
                    id="sort-select" 
                    value={sort} 
                    onChange={(e) => handleFilterChange('sort', e.target.value)}
                  >
                    <option value="">Relevance</option>
                    <option value="price_asc">Price: Low to High</option>
                    <option value="price_desc">Price: High to Low</option>
                    <option value="rating_desc">Rating: High to Low</option>
                    <option value="discount_desc">Discount: High to Low</option>
                    <option value="newest">Newest</option>
                  </select>
                </div>
              </div>
            </div>

            <ActiveFilterChips 
              filters={chipFilters} 
              onRemoveFilter={handleRemoveFilter} 
              onClearAll={clearAllFilters}
            />

            <ProductGrid
              products={currentPageProducts}
              onClearFilters={clearAllFilters}
              emptyTitle={
                totalItems === 0 
                  ? (searchQuery ? `No products found for "${searchQuery}".` : "No products match your filters.")
                  : undefined
              }
              emptyMessage={
                totalItems === 0
                  ? <p>Try removing some filters or changing your search.</p>
                  : undefined
              }
            />

            <ProductPagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
            />
          </main>
        </div>
      )}

      {/* Mobile Drawer */}
      <MobileFilterDrawer
        isOpen={isMobileDrawerOpen}
        onClose={() => setIsMobileDrawerOpen(false)}
        filters={activeFilters}
        categories={availableCategories}
        brands={availableBrands}
        onFilterChange={handleFilterChange}
        onClearAll={clearAllFilters}
      />
    </section>
  );
}
