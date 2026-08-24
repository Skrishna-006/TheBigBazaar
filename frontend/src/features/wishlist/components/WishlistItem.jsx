import { Link } from 'react-router-dom';
import ProductPrice from '../../products/components/ProductPrice';

const placeholderImage =
  'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns%3D%22http%3A//www.w3.org/2000/svg%22 width%3D%22960%22 height%3D%22720%22 viewBox%3D%220 0 960 720%22%3E%3Crect width%3D%22960%22 height%3D%22720%22 rx%3D%2232%22 fill%3D%22%23e2e8f0%22/%3E%3Cpath d%3D%22M240 500l120-140 92 106 80-88 188 222H240z%22 fill%3D%22%23cbd5e1%22/%3E%3Ccircle cx%3D%22364%22 cy%3D%22306%22 r%3D%2264%22 fill%3D%22%23cbd5e1%22/%3E%3C/svg%3E';

function safeImageSrc(imageUrl) {
  return typeof imageUrl === 'string' && imageUrl.trim() ? imageUrl : placeholderImage;
}

export default function WishlistItem({
  item,
  isRemoving,
  isAddingToCart,
  onRemove,
  onAddToCart,
}) {
  const unavailable = !item.available;

  return (
    <article className="wishlist-item">
      <Link to={`/products/${item.productId}`} className="wishlist-item__media-link">
        <img
          className="wishlist-item__image"
          src={safeImageSrc(item.imageUrl)}
          alt={`${item.productName} product image`}
          onError={(event) => {
            event.currentTarget.src = placeholderImage;
          }}
        />
      </Link>

      <div className="wishlist-item__content">
        <div className="wishlist-item__header">
          <div>
            <p className="wishlist-item__meta">
              {item.brand?.name || 'Brand coming soon'} · {item.category?.name || 'Category coming soon'}
            </p>
            <h3 className="wishlist-item__title">
              <Link to={`/products/${item.productId}`}>{item.productName}</Link>
            </h3>
          </div>
          {unavailable ? <span className="badge badge--warning">Unavailable</span> : <span className="badge badge--success">Available</span>}
        </div>

        <ProductPrice value={item.price} />

        <div className="wishlist-item__actions">
          <button
            type="button"
            className="button button--secondary button--small"
            onClick={onRemove}
            disabled={isRemoving}
          >
            {isRemoving ? 'Removing...' : 'Remove'}
          </button>
          <button
            type="button"
            className="button button--primary button--small"
            onClick={onAddToCart}
            disabled={isAddingToCart || unavailable}
          >
            {unavailable ? 'Unavailable' : isAddingToCart ? 'Adding...' : 'Add to cart'}
          </button>
        </div>
      </div>
    </article>
  );
}
