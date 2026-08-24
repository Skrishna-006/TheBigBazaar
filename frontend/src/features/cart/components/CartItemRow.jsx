import ProductPrice from '../../products/components/ProductPrice';

const placeholderImage =
  'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns%3D%22http%3A//www.w3.org/2000/svg%22 width%3D%22960%22 height%3D%22720%22 viewBox%3D%220 0 960 720%22%3E%3Crect width%3D%22960%22 height%3D%22720%22 rx%3D%2232%22 fill%3D%22%23e2e8f0%22/%3E%3Cpath d%3D%22M240 500l120-140 92 106 80-88 188 222H240z%22 fill%3D%22%23cbd5e1%22/%3E%3Ccircle cx%3D%22364%22 cy%3D%22306%22 r%3D%2264%22 fill%3D%22%23cbd5e1%22/%3E%3C/svg%3E';

export default function CartItemRow({ item, onDecrease, onIncrease, onSetQuantity, onRemove, isUpdating }) {
  function handleQuantityChange(event) {
    onSetQuantity(Number(event.target.value));
  }

  return (
    <article className="cart-item">
      <img
        className="cart-item__image"
        src={item.productImageUrl || placeholderImage}
        alt={`${item.productName} image`}
        onError={(event) => {
          event.currentTarget.src = placeholderImage;
        }}
      />
      <div className="cart-item__content">
        <div className="cart-item__header">
          <div>
            <h3>{item.productName}</h3>
            <p className="cart-item__meta">
              {item.brand?.name || 'Brand coming soon'} · {item.category?.name || 'Category coming soon'}
            </p>
          </div>
          {!item.available ? <span className="badge badge--warning">Unavailable</span> : null}
        </div>

        <div className="cart-item__pricing">
          <ProductPrice value={item.unitPrice} />
          <span className="cart-item__line-total">
            Line total: <ProductPrice value={item.lineTotal} />
          </span>
        </div>

        <div className="cart-item__actions">
          <div className="quantity-stepper">
            <button type="button" className="button button--secondary button--small" onClick={onDecrease} disabled={isUpdating || item.quantity <= 1}>
              -
            </button>
            <label className="sr-only" htmlFor={`cart-quantity-${item.productId}`}>
              Quantity for {item.productName}
            </label>
            <input
              id={`cart-quantity-${item.productId}`}
              className="quantity-stepper__input"
              type="number"
              min="1"
              value={item.quantity}
              onChange={handleQuantityChange}
              disabled={isUpdating}
            />
            <button type="button" className="button button--secondary button--small" onClick={onIncrease} disabled={isUpdating}>
              +
            </button>
          </div>

          <div className="cart-item__buttons">
            <button type="button" className="button button--secondary button--small" onClick={onRemove} disabled={isUpdating}>
              Remove
            </button>
          </div>
        </div>
      </div>
    </article>
  );
}
