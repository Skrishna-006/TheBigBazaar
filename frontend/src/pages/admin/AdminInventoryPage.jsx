import { useEffect, useState } from 'react';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import EmptyState from '../../components/common/EmptyState';
import { normalizeApiError } from '../../utils/apiError';
import { adjustInventory, createInventory, getInventoryByProductId, getInventoryMovements, getLowStockInventory } from '../../features/admin/api/inventoryAdminApi';

export default function AdminInventoryPage() {
  const [lowStock, setLowStock] = useState([]);
  const [productId, setProductId] = useState('');
  const [inventory, setInventory] = useState(null);
  const [movements, setMovements] = useState([]);
  const [createForm, setCreateForm] = useState({ productId: '', quantity: '', lowStockThreshold: '' });
  const [adjustForm, setAdjustForm] = useState({ type: 'ADJUSTMENT', quantity: '', reason: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  const loadLowStock = async () => {
    setLowStock(await getLowStockInventory());
  };

  const loadProduct = async (nextProductId = productId) => {
    if (!nextProductId) return;
    setInventory(await getInventoryByProductId(nextProductId));
    setMovements(await getInventoryMovements(nextProductId));
  };

  useEffect(() => {
    (async () => {
      try {
        await loadLowStock();
      } catch (exception) {
        setError(normalizeApiError(exception, 'Unable to load inventory.'));
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  const handleCreate = async (event) => {
    event.preventDefault();
    try {
      await createInventory({ ...createForm, quantity: Number(createForm.quantity), lowStockThreshold: Number(createForm.lowStockThreshold) });
      setSuccess('Inventory created.');
      setCreateForm({ productId: '', quantity: '', lowStockThreshold: '' });
      await loadLowStock();
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to create inventory.'));
    }
  };

  const handleAdjust = async (event) => {
    event.preventDefault();
    try {
      await adjustInventory(productId, { ...adjustForm, quantity: Number(adjustForm.quantity) });
      setSuccess('Inventory updated.');
      await loadProduct();
      await loadLowStock();
    } catch (exception) {
      setError(normalizeApiError(exception, 'Unable to adjust inventory.'));
    }
  };

  if (isLoading) return <LoadingSpinner label="Loading inventory..." />;

  return (
    <section className="admin-section">
      <h1>Inventory</h1>
      {error ? <ErrorMessage title="Inventory error" message={error} /> : null}
      {success ? <p className="form-success">{success}</p> : null}
      <div className="admin-panel-grid">
        <form className="admin-form" onSubmit={handleCreate}>
          <h2>Create inventory</h2>
          <label>Product ID<input value={createForm.productId} onChange={(e) => setCreateForm((c) => ({ ...c, productId: e.target.value }))} required /></label>
          <div className="form-grid form-grid--two">
            <label>Quantity<input type="number" min="0" value={createForm.quantity} onChange={(e) => setCreateForm((c) => ({ ...c, quantity: e.target.value }))} required /></label>
            <label>Low stock threshold<input type="number" min="0" value={createForm.lowStockThreshold} onChange={(e) => setCreateForm((c) => ({ ...c, lowStockThreshold: e.target.value }))} required /></label>
          </div>
          <button className="primary-btn" type="submit">Create</button>
        </form>
        <form className="admin-form" onSubmit={handleAdjust}>
          <h2>Adjust inventory</h2>
          <label>Product ID<input value={productId} onChange={(e) => setProductId(e.target.value)} onBlur={() => loadProduct()} required /></label>
          <div className="form-grid form-grid--two">
            <label>Type
              <select value={adjustForm.type} onChange={(e) => setAdjustForm((c) => ({ ...c, type: e.target.value }))}>
                <option value="ADJUSTMENT">ADJUSTMENT</option>
                <option value="STOCK_IN">STOCK_IN</option>
                <option value="STOCK_OUT">STOCK_OUT</option>
              </select>
            </label>
            <label>Quantity<input type="number" min="0" value={adjustForm.quantity} onChange={(e) => setAdjustForm((c) => ({ ...c, quantity: e.target.value }))} required /></label>
          </div>
          <label>Reason<input value={adjustForm.reason} onChange={(e) => setAdjustForm((c) => ({ ...c, reason: e.target.value }))} required /></label>
          <button className="primary-btn" type="submit">Apply</button>
        </form>
      </div>
      <div className="admin-panel-grid">
        <article className="admin-card">
          <h2>Current inventory</h2>
          {inventory ? (
            <div className="detail-stack">
              <p><strong>Quantity:</strong> {inventory.quantity}</p>
              <p><strong>Reserved:</strong> {inventory.reservedQuantity}</p>
              <p><strong>Available:</strong> {inventory.availableQuantity}</p>
              <p><strong>Threshold:</strong> {inventory.lowStockThreshold}</p>
            </div>
          ) : (
            <EmptyState title="No inventory selected" message="Enter a product id to inspect stock." />
          )}
        </article>
        <article className="admin-card">
          <h2>Movement history</h2>
          {movements.length === 0 ? <EmptyState title="No movements" message="Movement history will appear here." /> : (
            <div className="detail-stack">
              {movements.map((movement) => (
                <div key={movement.id} className="admin-mini-row">
                  <strong>{movement.type}</strong>
                  <span>{movement.quantity}</span>
                  <span>{movement.reason}</span>
                </div>
              ))}
            </div>
          )}
        </article>
      </div>
      <article className="admin-card">
        <h2>Low stock</h2>
        {lowStock.length === 0 ? <EmptyState title="No low stock items" message="Inventory is healthy." /> : (
          <div className="admin-table">
            {lowStock.map((item) => (
              <div key={item.id} className="admin-row">
                <div>
                  <strong>{item.productName}</strong>
                  <p>Available {item.availableQuantity}</p>
                </div>
                <div>
                  <p>Threshold {item.lowStockThreshold}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </article>
    </section>
  );
}
