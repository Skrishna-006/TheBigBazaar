import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { getBrands } from '../features/brands/api/brandApi';
import { getCategories } from '../features/categories/api/categoryApi';
import { getProducts } from '../features/products/api/productApi';
import ProductFilters from '../features/products/components/ProductFilters';
import ProductGrid from '../features/products/components/ProductGrid';
import { normalizeApiError } from '../utils/apiError';
import { getTopDeals } from '../utils/productUtils';

const requestCache = {
  categories: null,
  brands: null,
};

async function loadCategories() {
  if (!requestCache.categories) {
    requestCache.categories = getCategories();
  }
  return requestCache.categories;
}

async function loadBrands() {
  if (!requestCache.brands) {
    requestCache.brands = getBrands();
  }
  return requestCache.brands;
}

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isProductsLoading, setIsProductsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const selectedCategoryId = searchParams.get('categoryId') || '';
  const selectedBrandId = searchParams.get('brandId') || '';
  const searchQuery = searchParams.get('search') || '';
  const showDeals = searchParams.get('deals') === 'true';

  const productParams = useMemo(
    () => ({
      categoryId: selectedCategoryId || undefined,
      brandId: selectedBrandId || undefined,
    }),
    [selectedBrandId, selectedCategoryId]
  );

  const filteredProducts = useMemo(() => {
    let result = products;

    if (showDeals) {
      result = getTopDeals(result, result.length);
    }

    if (!searchQuery) return result;
    const query = searchQuery.toLowerCase().trim();
    return result.filter((product) => {
      const matchName = product.name?.toLowerCase().includes(query);
      const matchBrand = product.brand?.name?.toLowerCase().includes(query);
      const matchCategory = product.category?.name?.toLowerCase().includes(query);
      const matchSku = product.sku?.toLowerCase().includes(query);
      return matchName || matchBrand || matchCategory || matchSku;
    });
  }, [products, searchQuery, showDeals]);

  useEffect(() => {
    let mounted = true;

    async function loadCatalog() {
      setIsInitialLoading(true);
      setErrorMessage('');
      try {
        const [loadedCategories, loadedBrands, loadedProducts] = await Promise.all([
          loadCategories(),
          loadBrands(),
          getProducts(productParams),
        ]);

        if (mounted) {
          setCategories(loadedCategories);
          setBrands(loadedBrands);
          setProducts(loadedProducts);
        }
      } catch (error) {
        const normalized = normalizeApiError(error);
        if (mounted) {
          setErrorMessage(normalized.message);
          setProducts([]);
        }
      } finally {
        if (mounted) {
          setIsInitialLoading(false);
        }
      }
    }

    loadCatalog();

    return () => {
      mounted = false;
    };
  }, [productParams]);

  async function refreshProducts(nextParams = productParams) {
    setIsProductsLoading(true);
    setErrorMessage('');
    try {
      const loadedProducts = await getProducts(nextParams);
      setProducts(loadedProducts);
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message);
      setProducts([]);
    } finally {
      setIsProductsLoading(false);
    }
  }

  function updateParams(nextCategoryId, nextBrandId) {
    const next = {};
    if (nextCategoryId) {
      next.categoryId = nextCategoryId;
    }
    if (nextBrandId) {
      next.brandId = nextBrandId;
    }
    if (searchQuery) {
      next.search = searchQuery;
    }
    if (showDeals) {
      next.deals = 'true';
    }
    setSearchParams(next, { replace: true });
  }

  function handleCategoryChange(nextCategoryId) {
    updateParams(nextCategoryId, selectedBrandId);
  }

  function handleBrandChange(nextBrandId) {
    updateParams(selectedCategoryId, nextBrandId);
  }

  function clearFilters() {
    const next = {};
    if (searchQuery) {
      next.search = searchQuery;
    }
    if (showDeals) {
      next.deals = 'true';
    }
    setSearchParams(next, { replace: true });
  }

  useEffect(() => {
    if (isInitialLoading) {
      return;
    }
    refreshProducts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedCategoryId, selectedBrandId]);

  if (isInitialLoading) {
    return <LoadingSpinner label="Loading products..." />;
  }

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
              <p>Discover active ShopSphere products and narrow them by category or brand.</p>
            </>
          )}
        </div>
      </div>

      <ProductFilters
        categories={categories}
        brands={brands}
        selectedCategoryId={selectedCategoryId}
        selectedBrandId={selectedBrandId}
        onCategoryChange={handleCategoryChange}
        onBrandChange={handleBrandChange}
        onClearFilters={clearFilters}
      />

      {isProductsLoading ? <LoadingSpinner label="Updating products..." /> : null}
      {errorMessage ? <ErrorMessage title="Unable to load catalog" message={errorMessage} /> : null}

      {!isProductsLoading && !errorMessage ? (
        <ProductGrid
          products={filteredProducts}
          onClearFilters={clearFilters}
          emptyTitle={showDeals ? "No deals available right now." : undefined}
          emptyMessage={showDeals ? <p>Check back soon for new offers.</p> : undefined}
        />
      ) : null}
    </section>
  );
}
