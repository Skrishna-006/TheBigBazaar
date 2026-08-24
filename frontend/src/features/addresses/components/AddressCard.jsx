export default function AddressCard({ address, onEdit, onDelete, onSetDefault }) {
  return (
    <article className="address-card">
      <div className="address-card__header">
        <div>
          <h3>
            {address.firstName} {address.lastName}
          </h3>
          {address.defaultAddress ? <span className="badge badge--default">Default</span> : null}
        </div>
        {address.phone ? <p>{address.phone}</p> : null}
      </div>

      <div className="address-card__body">
        <p>{address.addressLine1}</p>
        {address.addressLine2 ? <p>{address.addressLine2}</p> : null}
        <p>
          {address.city}, {address.state} {address.postalCode}
        </p>
        <p>{address.country}</p>
      </div>

      <div className="address-card__actions">
        <button type="button" className="button button--secondary button--small" onClick={onEdit}>
          Edit
        </button>
        <button type="button" className="button button--secondary button--small" onClick={onDelete}>
          Delete
        </button>
        {!address.defaultAddress ? (
          <button type="button" className="button button--primary button--small" onClick={onSetDefault}>
            Set as Default
          </button>
        ) : null}
      </div>
    </article>
  );
}
