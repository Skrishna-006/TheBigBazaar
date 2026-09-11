export default function TopDealsSkeleton() {
  const skeletonCards = [1, 2, 3, 4];

  return (
    <div className="top-deals__grid" aria-busy="true" aria-label="Loading top deals">
      {skeletonCards.map((item) => (
        <article key={item} className="product-card product-card--skeleton">
          <div className="skeleton-image" />
          <div className="product-card__body" style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
            <div className="skeleton-line skeleton-line--title" />
            <div className="skeleton-line skeleton-line--rating" />
            <div className="skeleton-line skeleton-line--price" />
            <div className="skeleton-line skeleton-line--delivery" />
            <div className="skeleton-actions">
              <div className="skeleton-button" />
              <div className="skeleton-button" />
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}
