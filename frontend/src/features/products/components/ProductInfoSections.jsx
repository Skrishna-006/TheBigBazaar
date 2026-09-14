import { useState, useEffect } from 'react';
import axiosClient from '../../../api/axiosClient';
import { useAuth } from '../../auth/context/AuthContext';
import ReviewForm from './ReviewForm';

export function ProductDescription({ description }) {
  if (!description) return null;

  return (
    <div className="product-info-section product-description">
      <h2 className="section-title">Description</h2>
      <p style={{ color: '#334155', lineHeight: '1.6' }}>{description}</p>
    </div>
  );
}

export function DeliveryInformation({ deliveryDate, deliveryCharge }) {
  if (!deliveryDate) return null;

  return (
    <div className="product-info-section delivery-information">
      <h2 className="section-title">Delivery Information</h2>
      <div className="delivery-content" style={{ fontSize: '1rem', color: '#334155', fontWeight: 500 }}>
        {deliveryCharge === 0 || deliveryCharge == null
          ? `🚚 Free delivery by ${deliveryDate}`
          : `🚚 Delivery ₹${deliveryCharge} by ${deliveryDate}`}
      </div>
    </div>
  );
}

export function ProductHighlights({ highlights }) {
  if (!Array.isArray(highlights) || highlights.length === 0) return null;

  return (
    <div className="product-info-section product-highlights">
      <h2 className="section-title">Product Highlights</h2>
      <ul className="highlights-list" style={{ listStyleType: 'disc', paddingLeft: '1.5rem', margin: 0, color: '#334155' }}>
        {highlights.map((h, i) => (
          <li key={i} style={{ marginBottom: '0.5rem', lineHeight: '1.5' }}>{h}</li>
        ))}
      </ul>
    </div>
  );
}

export function ProductSpecifications({ specifications }) {
  if (!specifications || typeof specifications !== 'object' || Array.isArray(specifications) || Object.keys(specifications).length === 0) return null;

  // Detect if specifications are grouped (i.e. value is an object) or flat (value is string)
  const isGrouped = Object.values(specifications).some(val => typeof val === 'object' && val !== null);

  return (
    <div className="product-info-section product-specifications">
      <h2 className="section-title">Specifications</h2>
      {isGrouped ? (
        <div className="specs-groups">
          {Object.entries(specifications).map(([groupName, groupSpecs]) => (
            <div key={groupName} className="spec-group" style={{ marginBottom: '2rem' }}>
              <h3 className="spec-group-title" style={{ fontSize: '1.1rem', fontWeight: 600, color: '#1e293b', marginBottom: '1rem', paddingBottom: '0.5rem', borderBottom: '1px solid #e2e8f0' }}>{groupName}</h3>
              <div className="specs-table" style={{ display: 'flex', flexDirection: 'column' }}>
                {Object.entries(groupSpecs).map(([key, value]) => (
                  <div key={key} className="spec-table-row" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '1rem', padding: '0.75rem 0', borderBottom: '1px solid #f1f5f9' }}>
                    <div className="spec-name" style={{ color: '#64748b', fontSize: '0.9rem' }}>{key}</div>
                    <div className="spec-value" style={{ color: '#0f172a', fontWeight: 500, fontSize: '0.95rem' }}>{value}</div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="specs-grid">
          {Object.entries(specifications).map(([key, value]) => (
            <div key={key} className="spec-row">
              <div className="spec-name">{key}</div>
              <div className="spec-value">{String(value)}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export function SoldBy({ seller }) {
  if (!seller) return null;

  return (
    <div className="product-info-section seller-card">
      <h2 className="section-title">Sold By</h2>
      <div className="seller-content">
        <div className="seller-header">
          {seller.logoUrl ? (
            <div className="seller-logo">
              <img src={seller.logoUrl} alt={seller.name} />
            </div>
          ) : (
            <div className="seller-icon-fallback">🏪</div>
          )}
          <div className="seller-info">
            <h3 className="seller-name">{seller.name}</h3>
            <div className="seller-meta">
              <span className="seller-rating">★ {seller.rating?.toFixed(1) || '0.0'}</span>
              <span className="seller-stat">{seller.ratingCount || 0} Ratings</span>
              <span className="seller-stat">{seller.followerCount || 0} Followers</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export function ProductReviews({ productId, initialRating, initialReviewCount }) {
  const { user, isAuthenticated } = useAuth();
  const [reviews, setReviews] = useState([]);
  const [summary, setSummary] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  const loadReviewsData = async () => {
    try {
      const [reviewsRes, summaryRes] = await Promise.all([
        axiosClient.get(`/products/${productId}/reviews`),
        axiosClient.get(`/products/${productId}/reviews/summary`)
      ]);
      const fetchedReviews = reviewsRes.data?.content || reviewsRes.data || [];
      setReviews(Array.isArray(fetchedReviews) ? fetchedReviews : []);
      setSummary(summaryRes.data);
    } catch (e) {
      console.warn('Could not load reviews:', e);
    }
  };

  useEffect(() => {
    let mounted = true;
    async function loadReviews() {
      setIsLoading(true);
      try {
        const [reviewsRes, summaryRes] = await Promise.all([
          axiosClient.get(`/products/${productId}/reviews`),
          axiosClient.get(`/products/${productId}/reviews/summary`)
        ]);
        if (mounted) {
          const fetchedReviews = reviewsRes.data?.content || reviewsRes.data || [];
          setReviews(Array.isArray(fetchedReviews) ? fetchedReviews : []);
          setSummary(summaryRes.data);
        }
      } catch (e) {
        console.warn('Could not load reviews:', e);
      } finally {
        if (mounted) setIsLoading(false);
      }
    }
    loadReviews();
    return () => { mounted = false; };
  }, [productId]);

  const handleReviewSubmitted = (savedReview) => {
    loadReviewsData();
  };

  const handleReviewDeleted = (reviewId) => {
    loadReviewsData();
  };

  const displayRating = summary ? summary.averageRating : initialRating;
  const displayCount = summary ? summary.totalReviews : initialReviewCount;
  
  const userExistingReview = isAuthenticated && user ? reviews.find(r => r.userId === user.id) : null;

  if (isLoading) {
    return (
      <div className="product-info-section product-reviews">
        <h2 className="section-title">Product Ratings & Reviews</h2>
        <div style={{ padding: '2rem', textAlign: 'center', color: '#64748b' }}>
          Loading reviews...
        </div>
      </div>
    );
  }

  return (
    <div className="product-info-section product-reviews">
      <h2 className="section-title">Product Ratings & Reviews</h2>
      
      {displayCount === 0 || !displayCount ? (
        <div className="no-reviews" style={{ color: '#475569', marginBottom: '1.5rem' }}>
          <p>No reviews yet.</p>
        </div>
      ) : (
        <>
          <div className="reviews-header-summary" style={{ display: 'flex', flexWrap: 'wrap', gap: '2rem', marginBottom: '2rem', padding: '1.5rem', backgroundColor: '#f8fafc', borderRadius: '8px' }}>
            <div className="overall-rating" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
              <span className="rating-number" style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#0f172a' }}>{displayRating?.toFixed(1) || '0.0'} ★</span>
              <div className="rating-stars" style={{ color: '#fbbf24', fontSize: '1.2rem', margin: '0.25rem 0' }}>{'★'.repeat(Math.max(0, Math.min(5, Math.round(displayRating || 0))))}{'☆'.repeat(Math.max(0, 5 - Math.min(5, Math.round(displayRating || 0))))}</div>
              <span className="rating-count" style={{ color: '#64748b', fontSize: '0.9rem' }}>{displayCount} Ratings & Reviews</span>
            </div>
            
            {summary && summary.ratingDistribution && (
              <div className="rating-distribution" style={{ flex: 1, minWidth: '250px' }}>
                {[5, 4, 3, 2, 1].map(star => {
                  const count = summary.ratingDistribution[star] || 0;
                  const percentage = displayCount > 0 ? (count / displayCount) * 100 : 0;
                  const labels = {5: 'Excellent', 4: 'Very Good', 3: 'Good', 2: 'Average', 1: 'Poor'};
                  return (
                    <div key={star} className="dist-row" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem', fontSize: '0.85rem' }}>
                      <span className="dist-label" style={{ width: '70px', color: '#475569' }}>{star} ★ {labels[star]}</span>
                      <div className="dist-bar" style={{ flex: 1, height: '8px', backgroundColor: '#e2e8f0', borderRadius: '4px', overflow: 'hidden' }}>
                        <div className="dist-fill" style={{ height: '100%', backgroundColor: '#3b82f6', width: percentage + '%' }}></div>
                      </div>
                      <span className="dist-count" style={{ width: '30px', textAlign: 'right', color: '#64748b' }}>{count}</span>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </>
      )}

      <hr style={{ border: 'none', borderTop: '1px solid #e2e8f0', margin: '2rem 0' }} />

      <ReviewForm 
        productId={productId} 
        existingReview={userExistingReview} 
        onReviewSubmitted={handleReviewSubmitted} 
        onReviewDeleted={handleReviewDeleted} 
      />

      {displayCount > 0 && (
        <div className="reviews-list" style={{ marginTop: '2rem' }}>
          {reviews.map(review => (
            <div key={review.id} className="review-item" style={{ padding: '1.5rem', borderBottom: '1px solid #f1f5f9' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                <div className="review-stars" style={{ color: '#fbbf24', letterSpacing: '2px' }}>{'★'.repeat(Math.max(0, Math.min(5, review.rating || 0)))}{'☆'.repeat(Math.max(0, 5 - Math.min(5, review.rating || 0)))}</div>
                <div className="review-date" style={{ color: '#94a3b8', fontSize: '0.85rem' }}>{new Date(review.createdAt).toLocaleDateString()}</div>
              </div>
              <p className="review-text" style={{ color: '#334155', lineHeight: '1.5', marginBottom: '1rem', whiteSpace: 'pre-wrap' }}>{review.reviewText}</p>
              <div className="review-footer" style={{ display: 'flex', gap: '1rem', fontSize: '0.85rem' }}>
                <span className="review-author" style={{ fontWeight: 500, color: '#0f172a' }}>{review.reviewerName}</span>
                {review.verifiedPurchase && <span className="verified-badge" style={{ color: '#16a34a', display: 'flex', alignItems: 'center', gap: '4px' }}>✓ Verified Purchase</span>}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
