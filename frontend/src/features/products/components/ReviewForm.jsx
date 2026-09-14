import { useState } from 'react';
import { useAuth } from '../../auth/context/AuthContext';
import { createReview, updateReview, deleteReview } from '../api/reviewApi';

export default function ReviewForm({ productId, existingReview, onReviewSubmitted, onReviewDeleted }) {
  const { isAuthenticated, user } = useAuth();
  
  const [isEditing, setIsEditing] = useState(false);
  const [rating, setRating] = useState(existingReview?.rating || 0);
  const [hoverRating, setHoverRating] = useState(0);
  const [reviewText, setReviewText] = useState(existingReview?.reviewText || '');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState(null);

  if (!isAuthenticated) {
    return (
      <div className="review-form-container" style={{ marginTop: '2rem', padding: '1.5rem', backgroundColor: '#f8fafc', borderRadius: '8px', textAlign: 'center' }}>
        <p style={{ marginBottom: '1rem', color: '#475569' }}>Login to write a review</p>
        <button 
          onClick={() => {
            // Using a simple redirect for now. Adjust if there's a login modal.
            window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
          }}
          style={{ padding: '0.5rem 1.5rem', backgroundColor: '#0f172a', color: 'white', borderRadius: '6px', border: 'none', cursor: 'pointer' }}
        >
          Login
        </button>
      </div>
    );
  }

  if (existingReview && !isEditing) {
    return (
      <div className="review-form-container" style={{ marginTop: '2rem', padding: '1.5rem', backgroundColor: '#f8fafc', borderRadius: '8px' }}>
        <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>Your Review</h3>
        <div style={{ marginBottom: '0.5rem', color: '#fbbf24', fontSize: '1.2rem' }}>
          {'★'.repeat(Math.max(0, Math.min(5, existingReview?.rating || 0)))}{'☆'.repeat(Math.max(0, 5 - Math.min(5, existingReview?.rating || 0)))}
        </div>
        <p style={{ color: '#334155', marginBottom: '1rem', whiteSpace: 'pre-wrap' }}>{existingReview.reviewText}</p>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button 
            onClick={() => {
              setRating(existingReview?.rating || 0);
              setReviewText(existingReview?.reviewText || '');
              setIsEditing(true);
            }}
            style={{ padding: '0.4rem 1rem', backgroundColor: '#e2e8f0', color: '#0f172a', borderRadius: '6px', border: 'none', cursor: 'pointer', fontSize: '0.9rem', fontWeight: 500 }}
          >
            Edit
          </button>
          <button 
            onClick={async () => {
              if (window.confirm('Are you sure you want to delete your review?')) {
                setIsDeleting(true);
                try {
                  await deleteReview(existingReview.id);
                  onReviewDeleted(existingReview.id);
                } catch (err) {
                  alert(err.response?.data?.message || 'Unable to delete review. Please try again.');
                  setIsDeleting(false);
                }
              }
            }}
            disabled={isDeleting}
            style={{ padding: '0.4rem 1rem', backgroundColor: '#fee2e2', color: '#ef4444', borderRadius: '6px', border: 'none', cursor: 'pointer', fontSize: '0.9rem', fontWeight: 500 }}
          >
            {isDeleting ? 'Deleting...' : 'Delete'}
          </button>
        </div>
      </div>
    );
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) {
      setError('Please select a rating.');
      return;
    }
    const trimmedText = reviewText.trim();
    if (trimmedText.length < 5) {
      setError('Review must be at least 5 characters long.');
      return;
    }
    if (trimmedText.length > 1000) {
      setError('Review cannot exceed 1000 characters.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      let savedReview;
      if (isEditing && existingReview) {
        savedReview = await updateReview(existingReview.id, {
          rating,
          title: trimmedText.substring(0, 50),
          reviewText: trimmedText
        });
      } else {
        savedReview = await createReview(productId, {
          rating,
          title: trimmedText.substring(0, 50),
          reviewText: trimmedText
        });
      }
      onReviewSubmitted(savedReview);
      setIsEditing(false);
      setRating(0);
      setReviewText('');
    } catch (err) {
      if (err.response) {
        switch (err.response.status) {
          case 401:
            setError('Please log in to submit a review.');
            break;
          case 403:
            setError('You are not authorized to submit this review.');
            break;
          case 404:
            setError('Product not found.');
            break;
          case 409:
            setError('You have already reviewed this product.');
            break;
          case 400:
            setError(err.response.data?.message || 'Invalid review data. Please check your inputs.');
            break;
          default:
            setError('Unable to submit your review right now. Please try again.');
        }
      } else {
        setError('Unable to submit your review right now. Please try again.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="review-form-container" style={{ marginTop: '2rem', padding: '1.5rem', backgroundColor: '#f8fafc', borderRadius: '8px' }}>
      <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>
        {isEditing ? 'Edit Your Review' : 'Write a Review'}
      </h3>
      {error && <div style={{ color: '#ef4444', marginBottom: '1rem', fontSize: '0.9rem' }}>{error}</div>}
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontSize: '0.9rem', fontWeight: 500, color: '#475569', marginBottom: '0.5rem' }}>Your Rating</label>
          <div style={{ display: 'flex', gap: '0.25rem', fontSize: '1.5rem', color: '#fbbf24', cursor: 'pointer' }}>
            {[1, 2, 3, 4, 5].map((star) => (
              <span 
                key={star}
                onMouseEnter={() => setHoverRating(star)}
                onMouseLeave={() => setHoverRating(0)}
                onClick={() => setRating(star)}
              >
                {star <= (hoverRating || rating) ? '★' : '☆'}
              </span>
            ))}
          </div>
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontSize: '0.9rem', fontWeight: 500, color: '#475569', marginBottom: '0.5rem' }}>Your Review</label>
          <textarea 
            value={reviewText}
            onChange={(e) => setReviewText(e.target.value)}
            disabled={isSubmitting}
            rows={4}
            placeholder="Write your review here..."
            style={{ width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #cbd5e1', resize: 'vertical', fontFamily: 'inherit' }}
            maxLength={1000}
          />
          <div style={{ display: 'flex', justifyContent: 'flex-end', fontSize: '0.8rem', color: '#64748b', marginTop: '0.25rem' }}>
            {reviewText.length}/1000
          </div>
        </div>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button 
            type="submit" 
            disabled={isSubmitting}
            style={{ padding: '0.6rem 1.5rem', backgroundColor: '#0f172a', color: 'white', borderRadius: '6px', border: 'none', cursor: isSubmitting ? 'not-allowed' : 'pointer', fontWeight: 500 }}
          >
            {isSubmitting ? (isEditing ? 'Saving...' : 'Submitting...') : (isEditing ? 'Save Changes' : 'Submit Review')}
          </button>
          {isEditing && (
            <button 
              type="button" 
              onClick={() => {
                setIsEditing(false);
                setRating(existingReview?.rating || 0);
                setReviewText(existingReview?.reviewText || '');
                setError(null);
              }}
              disabled={isSubmitting}
              style={{ padding: '0.6rem 1.5rem', backgroundColor: '#e2e8f0', color: '#0f172a', borderRadius: '6px', border: 'none', cursor: 'pointer', fontWeight: 500 }}
            >
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
}
