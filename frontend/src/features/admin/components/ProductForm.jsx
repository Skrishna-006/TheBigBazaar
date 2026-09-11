import { useEffect, useState } from 'react';

const initialState = {
  name: '',
  sku: '',
  description: '',
  price: '',
  originalPrice: '',
  rating: '',
  reviewCount: '',
  deliveryCharge: '',
  deliveryDays: '',
  categoryId: '',
  brandId: '',
  imageUrl: '',
};

export default function ProductForm({
  initialValues = initialState,
  categories = [],
  brands = [],
  onSubmit,
  submitLabel = 'Save Product',
  isSubmitting = false,
  error,
  success,
}) {
  const [form, setForm] = useState({ ...initialState, ...initialValues });

  useEffect(() => {
    setForm({ ...initialState, ...initialValues });
  }, [initialValues]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    onSubmit({
      ...form,
      price: form.price === '' ? '' : Number(form.price),
      originalPrice: form.originalPrice === '' ? null : Number(form.originalPrice),
      rating: form.rating === '' ? null : Number(form.rating),
      reviewCount: form.reviewCount === '' ? null : Number(form.reviewCount),
      deliveryCharge: form.deliveryCharge === '' ? null : Number(form.deliveryCharge),
      deliveryDays: form.deliveryDays === '' ? null : Number(form.deliveryDays),
    });
  };

  return (
    <form className="admin-form" onSubmit={handleSubmit}>
      <div className="form-grid form-grid--two">
        <label>
          Name
          <input name="name" value={form.name} onChange={handleChange} required />
        </label>
        <label>
          SKU
          <input name="sku" value={form.sku} onChange={handleChange} required />
        </label>
        <label>
          Selling Price (₹)
          <input name="price" type="number" step="0.01" min="0" value={form.price} onChange={handleChange} required />
        </label>
        <label>
          Original Price (₹)
          <input name="originalPrice" type="number" step="0.01" min={form.price || "0"} value={form.originalPrice} onChange={handleChange} />
        </label>
        <label>
          Delivery Charge (₹)
          <input name="deliveryCharge" type="number" step="0.01" min="0" value={form.deliveryCharge} onChange={handleChange} />
        </label>
        <label>
          Delivery Days
          <input name="deliveryDays" type="number" step="1" min="1" value={form.deliveryDays} onChange={handleChange} />
        </label>
        <label>
          Rating (0-5)
          <input name="rating" type="number" step="0.1" min="0" max="5" value={form.rating} onChange={handleChange} />
        </label>
        <label>
          Review Count
          <input name="reviewCount" type="number" step="1" min="0" value={form.reviewCount} onChange={handleChange} />
        </label>
        <label>
          Category
          <select name="categoryId" value={form.categoryId} onChange={handleChange} required>
            <option value="">Select category</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Brand
          <select name="brandId" value={form.brandId} onChange={handleChange} required>
            <option value="">Select brand</option>
            {brands.map((brand) => (
              <option key={brand.id} value={brand.id}>
                {brand.name}
              </option>
            ))}
          </select>
        </label>
        <label style={{ gridColumn: '1 / -1' }}>
          Image URL
          <input name="imageUrl" value={form.imageUrl} onChange={handleChange} />
        </label>
      </div>
      <label>
        Description
        <textarea name="description" rows="4" value={form.description} onChange={handleChange} />
      </label>
      {error ? <p className="form-error">{error}</p> : null}
      {success ? <p className="form-success">{success}</p> : null}
      <button className="primary-btn" type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Saving...' : submitLabel}
      </button>
    </form>
  );
}
