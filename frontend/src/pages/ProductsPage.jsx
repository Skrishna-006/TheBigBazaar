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

  const productParams = useMemo(
    () => ({
      categoryId: selectedCategoryId || undefined,
      brandId: selectedBrandId || undefined,
    }),
    [selectedBrandId, selectedCategoryId]
  );

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
    setSearchParams(next, { replace: true });
  }

  function handleCategoryChange(nextCategoryId) {
    updateParams(nextCategoryId, selectedBrandId);
  }

  function handleBrandChange(nextBrandId) {
    updateParams(selectedCategoryId, nextBrandId);
  }

  function clearFilters() {
    setSearchParams({}, { replace: true });
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
          <p className="eyebrow">Browse the catalog</p>
          <h1>Products</h1>
          <p>Discover active ShopSphere products and narrow them by category or brand.</p>
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

      {!isProductsLoading && !errorMessage ? <ProductGrid products={products} onClearFilters={clearFilters} /> : null}
    </section>
  );
}
