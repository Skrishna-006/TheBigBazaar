import { useEffect, useState } from 'react';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import EmptyState from '../../components/common/EmptyState';
import { normalizeApiError } from '../../utils/apiError';
import { createCategory, deleteCategory, getCategories, updateCategory } from '../../features/admin/api/categoryAdminApi';

const initialForm = { name: '', description: '', imageUrl: '' };

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        setCategories(await getCategories());
      } catch (exception) {
        setError(normalizeApiError(exception, 'Unable to load categories.'));
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
      if (isEdit) await updateCategory(editingId, form);
      else await createCategory(form);
      setCategories(await getCategories());
      setForm(initialForm);
      setEditingId(null);
      setSuccess(isEdit ? 'Category updated.' : 'Category created.');
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to save category.'));
    } finally {
      setIsSaving(false);
    }
  };

  const edit = (category) => {
    setEditingId(category.id);
    setForm({ name: category.name ?? '', description: category.description ?? '', imageUrl: category.imageUrl ?? '' });
  };

  const remove = async (category) => {
    if (!window.confirm(`Deactivate category "${category.name}"?`)) return;
    try {
      await deleteCategory(category.id);
      setCategories(await getCategories());
      setSuccess('Category updated.');
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to deactivate category.'));
    }
  };

  if (isLoading) return <LoadingSpinner label="Loading categories..." />;

  return (
    <section className="admin-section">
      <h1>Categories</h1>
      <form className="admin-form" onSubmit={submit}>
        <div className="form-grid form-grid--two">
          <label>Name<input value={form.name} onChange={(e) => setForm((c) => ({ ...c, name: e.target.value }))} required /></label>
          <label>Image URL<input value={form.imageUrl} onChange={(e) => setForm((c) => ({ ...c, imageUrl: e.target.value }))} /></label>
        </div>
        <label>Description<textarea rows="4" value={form.description} onChange={(e) => setForm((c) => ({ ...c, description: e.target.value }))} /></label>
        {error ? <ErrorMessage title="Category error" message={error} /> : null}
        {success ? <p className="form-success">{success}</p> : null}
        <button className="primary-btn" disabled={isSaving}>{isSaving ? 'Saving...' : editingId ? 'Update Category' : 'Create Category'}</button>
      </form>
      {categories.length === 0 ? <EmptyState title="No categories" message="Create your first category." /> : (
        <div className="admin-table">
          {categories.map((category) => (
            <article key={category.id} className="admin-row">
              <div>
                <strong>{category.name}</strong>
                <p>{category.slug}</p>
                <p>{category.active ? 'Active' : 'Inactive'}</p>
              </div>
              <div className="admin-row__actions">
                <button type="button" className="secondary-btn" onClick={() => edit(category)}>Edit</button>
                <button type="button" className="danger-btn" onClick={() => remove(category)}>Deactivate</button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
