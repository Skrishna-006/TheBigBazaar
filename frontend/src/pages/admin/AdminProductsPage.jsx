import { useEffect, useState } from 'react';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import EmptyState from '../../components/common/EmptyState';
import { normalizeApiError } from '../../utils/apiError';
import { getCategories } from '../../features/admin/api/categoryAdminApi';
import { getBrands } from '../../features/admin/api/brandAdminApi';
import { createProduct, deleteProduct, getAdminProducts, updateProduct } from '../../features/admin/api/productAdminApi';
import ProductForm from '../../features/admin/components/ProductForm';

export default function AdminProductsPage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [editingProduct, setEditingProduct] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  const load = async () => {
    const [productData, categoryData, brandData] = await Promise.all([getAdminProducts(), getCategories(), getBrands()]);
    setProducts(productData);
    setCategories(categoryData.filter((category) => category.active !== false));
    setBrands(brandData.filter((brand) => brand.active !== false));
  };

  useEffect(() => {
    (async () => {
      try {
        await load();
      } catch (exception) {
        setError(normalizeApiError(exception, 'Unable to load products.'));
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  const submit = async (payload) => {
    setIsSaving(true);
    setError('');
    setSuccess('');
    const isEdit = Boolean(editingProduct);
    try {
      if (isEdit) await updateProduct(editingProduct.id, payload);
      else await createProduct(payload);
      await load();
      setEditingProduct(null);
      setSuccess(isEdit ? 'Product updated.' : 'Product created.');
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to save product.'));
    } finally {
      setIsSaving(false);
    }
  };

  const remove = async (product) => {
    if (!window.confirm(`Deactivate product "${product.name}"?`)) return;
    try {
      await deleteProduct(product.id);
      await load();
      setSuccess('Product deactivated.');
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to deactivate product.'));
    }
  };

  const editingInitialValues = editingProduct
    ? {
        name: editingProduct.name ?? '',
        sku: editingProduct.sku ?? '',
        description: editingProduct.description ?? '',
        price: editingProduct.price ?? '',
        categoryId: editingProduct.category?.id ?? '',
        brandId: editingProduct.brand?.id ?? '',
        imageUrl: editingProduct.imageUrl ?? '',
      }
    : undefined;

  if (isLoading) return <LoadingSpinner label="Loading products..." />;

  return (
    <section className="admin-section">
      <h1>Products</h1>
      <ProductForm
        initialValues={editingInitialValues}
        categories={categories}
        brands={brands}
        onSubmit={submit}
        submitLabel={editingProduct ? 'Update Product' : 'Create Product'}
        isSubmitting={isSaving}
        error={error}
        success={success}
      />
      {products.length === 0 ? <EmptyState title="No products" message="Create your first product." /> : (
        <div className="admin-table">
          {products.map((product) => (
            <article key={product.id} className="admin-row">
              <div>
                <strong>{product.name}</strong>
                <p>{product.sku}</p>
                <p>{product.category?.name} · {product.brand?.name}</p>
                <p>{product.active ? 'Active' : 'Inactive'}</p>
              </div>
              <div className="admin-row__actions">
                <button type="button" className="secondary-btn" onClick={() => setEditingProduct(product)}>Edit</button>
                <button type="button" className="danger-btn" onClick={() => remove(product)}>Deactivate</button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
