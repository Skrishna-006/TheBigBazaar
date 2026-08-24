import EmptyState from '../../../components/common/EmptyState';
import ProductCard from './ProductCard';

export default function ProductGrid({ products, onClearFilters }) {
  if (!products?.length) {
    return (
      <EmptyState
        title="No products found."
        message={
          <>
            Try a different category or brand, or clear the filters to see all products.
            {onClearFilters ? (
              <div className="empty-state__actions">
                <button className="button button--secondary" type="button" onClick={onClearFilters}>
                  Clear Filters
                </button>
              </div>
            ) : null}
          </>
        }
      />
    );
  }

  return (
    <section className="product-grid" aria-label="Product results">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </section>
  );
}
