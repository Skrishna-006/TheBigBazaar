import React from 'react';

export default function TrendingNowSkeleton() {
  return (
    <div className="trending-now-grid">
      {Array.from({ length: 4 }).map((_, idx) => (
        <div key={idx} className="product-card skeleton-card">
          <div className="skeleton-card__image-placeholder"></div>
          <div className="skeleton-card__content">
            <div className="skeleton-card__title"></div>
            <div className="skeleton-card__subtitle"></div>
            <div className="skeleton-card__price"></div>
            <div className="skeleton-card__action"></div>
          </div>
        </div>
      ))}
    </div>
  );
}
