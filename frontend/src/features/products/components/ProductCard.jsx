import { Link } from 'react-router-dom';
import ProductPrice from './ProductPrice';

const placeholderImage =
  'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns%3D%22http%3A//www.w3.org/2000/svg%22 width%3D%22960%22 height%3D%22720%22 viewBox%3D%220 0 960 720%22%3E%3Crect width%3D%22960%22 height%3D%22720%22 rx%3D%2232%22 fill%3D%22%23e2e8f0%22/%3E%3Cpath d%3D%22M240 500l120-140 92 106 80-88 188 222H240z%22 fill%3D%22%23cbd5e1%22/%3E%3Ccircle cx%3D%22364%22 cy%3D%22306%22 r%3D%2264%22 fill%3D%22%23cbd5e1%22/%3E%3C/svg%3E';

function safeImageSrc(imageUrl) {
  return typeof imageUrl === 'string' && imageUrl.trim() ? imageUrl : placeholderImage;
}

export default function ProductCard({ product }) {
  return (
    <article className="product-card">
      <Link to={`/products/${product.id}`} className="product-card__media-link">
        <img
          className="product-card__image"
          src={safeImageSrc(product.imageUrl)}
          alt={`${product.name} product image`}
          onError={(event) => {
            event.currentTarget.src = placeholderImage;
          }}
        />
      </Link>
      <div className="product-card__body">
        <div className="product-card__meta">
          <span>{product.brand?.name || 'Brand coming soon'}</span>
          <span>{product.category?.name || 'Category coming soon'}</span>
        </div>
        <h3 className="product-card__title">
          <Link to={`/products/${product.id}`}>{product.name}</Link>
        </h3>
        <ProductPrice value={product.price} />
        <div className="product-card__actions">
          <Link className="button button--secondary button--small" to={`/products/${product.id}`}>
            View details
          </Link>
          <span className="product-card__coming-soon">Cart coming soon</span>
        </div>
      </div>
    </article>
  );
}
