import EmptyState from '../../../components/common/EmptyState';
import WishlistItem from './WishlistItem';

export default function WishlistGrid({ items, onRemove, onAddToCart, removingProductId, addingProductId }) {
  if (!items.length) {
    return (
      <EmptyState
        title="Your wishlist is empty."
        message={
          <span>
            Save products you like and come back to them later.
          </span>
        }
      />
    );
  }

  return (
    <div className="wishlist-grid">
      {items.map((item) => (
        <WishlistItem
          key={item.productId}
          item={item}
          isRemoving={removingProductId === item.productId}
          isAddingToCart={addingProductId === item.productId}
          onRemove={() => onRemove(item.productId)}
          onAddToCart={() => onAddToCart(item)}
        />
      ))}
    </div>
  );
}
