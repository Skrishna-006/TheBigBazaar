import { useEffect, useState } from 'react';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import EmptyState from '../../components/common/EmptyState';
import { normalizeApiError } from '../../utils/apiError';
import { createBrand, deleteBrand, getBrands, updateBrand } from '../../features/admin/api/brandAdminApi';

const initialForm = { name: '', description: '', logoUrl: '' };

export default function AdminBrandsPage() {
  const [brands, setBrands] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        setBrands(await getBrands());
      } catch (exception) {
        setError(normalizeApiError(exception, 'Unable to load brands.'));
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setIsSaving(true);
    setError('');
    setSuccess('');
    const isEdit = Boolean(editingId);
    try {
      if (isEdit) await updateBrand(editingId, form);
      else await createBrand(form);
      setBrands(await getBrands());
      setForm(initialForm);
      setEditingId(null);
      setSuccess(isEdit ? 'Brand updated.' : 'Brand created.');
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to save brand.'));
    } finally {
      setIsSaving(false);
    }
  };

  const edit = (brand) => {
    setEditingId(brand.id);
    setForm({ name: brand.name ?? '', description: brand.description ?? '', logoUrl: brand.logoUrl ?? '' });
  };

  const remove = async (brand) => {
    if (!window.confirm(`Deactivate brand "${brand.name}"?`)) return;
    try {
      await deleteBrand(brand.id);
      setBrands(await getBrands());
      setSuccess('Brand updated.');
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to deactivate brand.'));
    }
  };

  if (isLoading) return <LoadingSpinner label="Loading brands..." />;

  return (
    <section className="admin-section">
      <h1>Brands</h1>
      <form className="admin-form" onSubmit={submit}>
        <div className="form-grid form-grid--two">
          <label>Name<input value={form.name} onChange={(e) => setForm((c) => ({ ...c, name: e.target.value }))} required /></label>
          <label>Logo URL<input value={form.logoUrl} onChange={(e) => setForm((c) => ({ ...c, logoUrl: e.target.value }))} /></label>
        </div>
        <label>Description<textarea rows="4" value={form.description} onChange={(e) => setForm((c) => ({ ...c, description: e.target.value }))} /></label>
        {error ? <ErrorMessage title="Brand error" message={error} /> : null}
        {success ? <p className="form-success">{success}</p> : null}
        <button className="primary-btn" disabled={isSaving}>{isSaving ? 'Saving...' : editingId ? 'Update Brand' : 'Create Brand'}</button>
      </form>
      {brands.length === 0 ? <EmptyState title="No brands" message="Create your first brand." /> : (
        <div className="admin-table">
          {brands.map((brand) => (
            <article key={brand.id} className="admin-row">
              <div>
                <strong>{brand.name}</strong>
                <p>{brand.slug}</p>
                <p>{brand.active ? 'Active' : 'Inactive'}</p>
              </div>
              <div className="admin-row__actions">
                <button type="button" className="secondary-btn" onClick={() => edit(brand)}>Edit</button>
                <button type="button" className="danger-btn" onClick={() => remove(brand)}>Deactivate</button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
