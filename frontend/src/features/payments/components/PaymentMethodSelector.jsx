const paymentMethods = [
  { value: 'CARD', label: 'Card', description: 'Development mock payment flow. No card details are collected.' },
  { value: 'UPI', label: 'UPI', description: 'Development mock payment flow. No UPI PIN is collected.' },
  { value: 'NET_BANKING', label: 'Net Banking', description: 'Development mock payment flow.' },
  { value: 'WALLET', label: 'Wallet', description: 'Development mock payment flow.' },
  { value: 'COD', label: 'Cash on Delivery', description: 'Development mock payment flow.' },
];

export default function PaymentMethodSelector({ value, onChange, disabled = false }) {
  return (
    <fieldset className="payment-method-selector" disabled={disabled}>
      <legend>Payment method</legend>
      <p className="payment-note">This checkout uses TheBigBazaar&apos;s development payment gateway.</p>
      <div className="payment-method-selector__grid" role="radiogroup" aria-label="Payment methods">
        {paymentMethods.map((method) => (
          <label
            key={method.value}
            className={`payment-method-option${value === method.value ? ' payment-method-option--selected' : ''}`}
          >
            <input
              type="radio"
              name="payment-method"
              value={method.value}
              checked={value === method.value}
              onChange={() => onChange(method.value)}
            />
            <span className="payment-method-option__title">{method.label}</span>
            <span className="payment-method-option__description">{method.description}</span>
          </label>
        ))}
      </div>
    </fieldset>
  );
}
