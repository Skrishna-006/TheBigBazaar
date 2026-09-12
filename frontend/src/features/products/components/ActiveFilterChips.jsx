import React from 'react';

export default function ActiveFilterChips({ filters, onRemoveFilter, onClearAll }) {
  const activeChips = [];

  if (filters.search) {
    activeChips.push({ id: 'search', label: `Search: "${filters.search}"`, type: 'search' });
  }
  if (filters.deals === 'true') {
    activeChips.push({ id: 'deals', label: 'Top Deals', type: 'deals' });
  }
  if (filters.categoryName) {
    activeChips.push({ id: 'categoryId', label: filters.categoryName, type: 'categoryId' });
  }
  if (filters.brandName) {
    activeChips.push({ id: 'brandId', label: filters.brandName, type: 'brandId' });
  }
  if (filters.minPrice || filters.maxPrice) {
    const min = filters.minPrice ? `₹${filters.minPrice}` : '0';
    const max = filters.maxPrice ? `₹${filters.maxPrice}` : 'Any';
    activeChips.push({ id: 'price', label: `${min} - ${max}`, type: 'price' });
  }
  if (filters.rating) {
    activeChips.push({ id: 'rating', label: `${filters.rating}★ & above`, type: 'rating' });
  }
  if (filters.discount) {
    activeChips.push({ id: 'discount', label: `${filters.discount}%+ Off`, type: 'discount' });
  }
  if (filters.delivery) {
    let label = 'Delivery';
    if (filters.delivery === 'free') label = 'Free Delivery';
    else label = `Within ${filters.delivery} days`;
    activeChips.push({ id: 'delivery', label, type: 'delivery' });
  }

  if (activeChips.length === 0) return null;

  return (
    <div className="active-filters" style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', alignItems: 'center', marginBottom: '1rem' }}>
      <span style={{ fontSize: '0.875rem', color: '#64748b', fontWeight: 500 }}>Active Filters:</span>
      {activeChips.map((chip) => (
        <button
          key={chip.id}
          className="active-filters__chip"
          onClick={() => onRemoveFilter(chip.type)}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.35rem',
            padding: '0.25rem 0.6rem',
            background: 'var(--color-primary-light, #eff6ff)',
            color: 'var(--color-primary-dark, #1d4ed8)',
            border: '1px solid var(--color-primary-border, #bfdbfe)',
            borderRadius: '999px',
            fontSize: '0.875rem',
            fontWeight: 500,
            cursor: 'pointer',
            transition: 'all 0.2s',
          }}
          aria-label={`Remove filter ${chip.label}`}
        >
          {chip.label}
          <span style={{ fontSize: '1.1rem', lineHeight: 1 }}>&times;</span>
        </button>
      ))}
      {activeChips.length > 1 && (
        <button
          onClick={onClearAll}
          style={{
            background: 'none',
            border: 'none',
            color: '#64748b',
            fontSize: '0.875rem',
            textDecoration: 'underline',
            cursor: 'pointer',
            padding: '0.25rem 0.5rem',
          }}
        >
          Clear All
        </button>
      )}
    </div>
  );
}
