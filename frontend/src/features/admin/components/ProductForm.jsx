import { useEffect, useState } from 'react';

const initialState = {
  name: '',
  sku: '',
  description: '',
  price: '',
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
          Price
          <input name="price" type="number" step="0.01" min="0" value={form.price} onChange={handleChange} required />
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
        <label>
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
