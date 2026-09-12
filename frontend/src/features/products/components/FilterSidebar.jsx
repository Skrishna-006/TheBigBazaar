import React, { useState } from 'react';

export default function FilterSidebar({
  filters,
  categories,
  brands,
  onFilterChange,
  onClearAll,
}) {
  const [customMin, setCustomMin] = useState(filters.minPrice || '');
  const [customMax, setCustomMax] = useState(filters.maxPrice || '');

  React.useEffect(() => {
    setCustomMin(filters.minPrice || '');
    setCustomMax(filters.maxPrice || '');
  }, [filters.minPrice, filters.maxPrice]);

  const handlePriceApply = () => {
    onFilterChange('price', { min: customMin, max: customMax });
  };

  const handlePricePreset = (min, max) => {
    setCustomMin(min || '');
    setCustomMax(max || '');
    onFilterChange('price', { min, max });
  };

  return (
    <div className="filter-sidebar">
      <div className="filter-sidebar__header">
        <h3>Filters</h3>
        <button className="filter-sidebar__clear" onClick={onClearAll}>Clear All</button>
      </div>

      <div className="filter-sidebar__section">
        <h4 className="filter-sidebar__title">Category</h4>
        <div className="filter-sidebar__options">
          {categories.map((category) => (
            <label key={category.id} className="filter-sidebar__label">
              <input
                type="radio"
                name="category"
                checked={filters.categoryId === category.id}
                onChange={() => onFilterChange('categoryId', category.id)}
              />
              <span className="filter-sidebar__text">{category.name}</span>
            </label>
          ))}
        </div>
      </div>

      <div className="filter-sidebar__section">
        <h4 className="filter-sidebar__title">Brand</h4>
        <div className="filter-sidebar__options">
          {brands.map((brand) => (
            <label key={brand.id} className="filter-sidebar__label">
              <input
                type="radio"
                name="brand"
                checked={filters.brandId === brand.id}
                onChange={() => onFilterChange('brandId', brand.id)}
              />
              <span className="filter-sidebar__text">{brand.name}</span>
            </label>
          ))}
        </div>
      </div>

      <div className="filter-sidebar__section">
        <h4 className="filter-sidebar__title">Price</h4>
        <div className="filter-sidebar__options">
          {[
            { label: 'Under ₹5,000', min: null, max: 5000 },
            { label: '₹5,000 - ₹10,000', min: 5000, max: 10000 },
            { label: '₹10,000 - ₹25,000', min: 10000, max: 25000 },
            { label: '₹25,000 - ₹50,000', min: 25000, max: 50000 },
            { label: '₹50,000 - ₹1,00,000', min: 50000, max: 100000 },
            { label: 'Over ₹1,00,000', min: 100000, max: null },
          ].map((preset, idx) => (
            <label key={idx} className="filter-sidebar__label">
              <input
                type="radio"
                name="price_preset"
                checked={filters.minPrice == preset.min && filters.maxPrice == preset.max}
                onChange={() => handlePricePreset(preset.min, preset.max)}
              />
              <span className="filter-sidebar__text">{preset.label}</span>
            </label>
          ))}
        </div>
        <div className="filter-sidebar__custom-price">
          <input
            type="number"
            placeholder="Min"
            value={customMin}
            onChange={(e) => setCustomMin(e.target.value)}
            className="filter-sidebar__input"
          />
          <span>to</span>
          <input
            type="number"
            placeholder="Max"
            value={customMax}
            onChange={(e) => setCustomMax(e.target.value)}
            className="filter-sidebar__input"
          />
          <button onClick={handlePriceApply} className="button button--secondary button--small">Go</button>
        </div>
      </div>

      <div className="filter-sidebar__section">
        <h4 className="filter-sidebar__title">Customer Rating</h4>
        <div className="filter-sidebar__options">
          {[4, 3, 2].map((rating) => (
            <label key={rating} className="filter-sidebar__label">
              <input
                type="radio"
                name="rating"
                checked={filters.rating === String(rating)}
                onChange={() => onFilterChange('rating', String(rating))}
              />
              <span className="filter-sidebar__text">{rating}★ & above</span>
            </label>
          ))}
        </div>
      </div>

      <div className="filter-sidebar__section">
        <h4 className="filter-sidebar__title">Discount</h4>
        <div className="filter-sidebar__options">
          {[10, 20, 30, 40, 50].map((discount) => (
            <label key={discount} className="filter-sidebar__label">
              <input
                type="radio"
                name="discount"
                checked={filters.discount === String(discount)}
                onChange={() => onFilterChange('discount', String(discount))}
              />
              <span className="filter-sidebar__text">{discount}% or more</span>
            </label>
          ))}
        </div>
      </div>

      <div className="filter-sidebar__section">
        <h4 className="filter-sidebar__title">Delivery</h4>
        <div className="filter-sidebar__options">
          {[
            { id: 'free', label: 'Free Delivery' },
            { id: '2', label: 'Within 2 days' },
            { id: '3', label: 'Within 3 days' },
            { id: '5', label: 'Within 5 days' },
          ].map((delivery) => (
            <label key={delivery.id} className="filter-sidebar__label">
              <input
                type="radio"
                name="delivery"
                checked={filters.delivery === delivery.id}
                onChange={() => onFilterChange('delivery', delivery.id)}
              />
              <span className="filter-sidebar__text">{delivery.label}</span>
            </label>
          ))}
        </div>
      </div>
    </div>
  );
}
