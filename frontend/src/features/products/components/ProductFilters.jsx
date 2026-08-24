export default function ProductFilters({
  categories,
  brands,
  selectedCategoryId,
  selectedBrandId,
  onCategoryChange,
  onBrandChange,
  onClearFilters,
}) {
  return (
    <section className="filters-card" aria-label="Product filters">
      <div className="filters-grid">
        <div className="form-field">
          <label htmlFor="category-filter">Category</label>
          <select id="category-filter" value={selectedCategoryId} onChange={(event) => onCategoryChange(event.target.value)}>
            <option value="">All Categories</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div className="form-field">
          <label htmlFor="brand-filter">Brand</label>
          <select id="brand-filter" value={selectedBrandId} onChange={(event) => onBrandChange(event.target.value)}>
            <option value="">All Brands</option>
            {brands.map((brand) => (
              <option key={brand.id} value={brand.id}>
                {brand.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="filters-actions">
        <button className="button button--secondary" type="button" onClick={onClearFilters}>
          Clear Filters
        </button>
      </div>
    </section>
  );
}
