import ProductPrice from '../../products/components/ProductPrice';

const placeholderImage =
  'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns%3D%22http%3A//www.w3.org/2000/svg%22 width%3D%22100%22 height%3D%22100%22 viewBox%3D%220 0 100 100%22%3E%3Crect width%3D%22100%22 height%3D%22100%22 rx%3D%2218%22 fill%3D%22%23e2e8f0%22/%3E%3Cpath d%3D%22M20 68l18-20 14 16 12-13 16 17H20z%22 fill%3D%22%23cbd5e1%22/%3E%3C/svg%3E';

export default function OrderItem({ item }) {
  return (
    <article className="order-item">
      <img
        className="order-item__image"
        src={item.productImageUrl || placeholderImage}
        alt={`${item.productName} product image`}
        onError={(event) => {
          event.currentTarget.src = placeholderImage;
        }}
      />
      <div className="order-item__body">
        <h3 className="order-item__title">{item.productName}</h3>
        <p className="order-item__meta">SKU: {item.productSku}</p>
        <div className="order-item__pricing">
          <span>Qty {item.quantity}</span>
          <ProductPrice value={item.unitPrice} />
          <strong>
            <ProductPrice value={item.lineTotal} />
          </strong>
        </div>
      </div>
    </article>
  );
}
