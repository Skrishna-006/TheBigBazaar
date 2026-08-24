export default function ProductPrice({ value }) {
  const amount = typeof value === 'number' ? value : Number(value ?? 0);
  const formatted = Number.isFinite(amount)
    ? new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 2,
      }).format(amount)
    : '₹0.00';

  return <span className="product-price">{formatted}</span>;
}
