import { useEffect, useState } from 'react';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { createAddress, deleteAddress, getAddresses, setDefaultAddress, updateAddress } from '../features/addresses/api/addressApi';
import AddressCard from '../features/addresses/components/AddressCard';
import AddressForm from '../features/addresses/components/AddressForm';
import { normalizeApiError } from '../utils/apiError';

export default function AddressesPage() {
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingAction, setLoadingAction] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  async function loadAddresses() {
    setLoading(true);
    setErrorMessage('');
    try {
      const data = await getAddresses();
      setAddresses(data);
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAddresses();
  }, []);

  function openCreateForm() {
    setEditingAddress(null);
    setFieldErrors({});
    setIsFormOpen(true);
  }

  function openEditForm(address) {
    setEditingAddress(address);
    setFieldErrors({});
    setIsFormOpen(true);
  }

  function closeForm() {
    setIsFormOpen(false);
    setEditingAddress(null);
    setFieldErrors({});
  }

  async function handleSubmit(values) {
    setLoadingAction(true);
    setErrorMessage('');
    setSuccessMessage('');
    setFieldErrors({});
    try {
      const payload = { ...values };
      if (!payload.addressLine2?.trim()) {
        delete payload.addressLine2;
      }
      if (!payload.phone?.trim()) {
        delete payload.phone;
      }
      const request = editingAddress
        ? updateAddress(editingAddress.id, payload)
        : createAddress(payload);
      await request;
      setSuccessMessage(editingAddress ? 'Address updated successfully.' : 'Address added successfully.');
      closeForm();
      await loadAddresses();
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message);
      setFieldErrors(normalized.fieldErrors || {});
    } finally {
      setLoadingAction(false);
    }
  }

  async function handleDelete(address) {
    if (!window.confirm('Are you sure you want to delete this address?')) return;
    setLoadingAction(true);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      await deleteAddress(address.id);
      setSuccessMessage('Address deleted successfully.');
      await loadAddresses();
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setLoadingAction(false);
    }
  }

  async function handleSetDefault(address) {
    setLoadingAction(true);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      await setDefaultAddress(address.id);
      setSuccessMessage('Default address updated.');
      await loadAddresses();
    } catch (error) {
      setErrorMessage(normalizeApiError(error).message);
    } finally {
      setLoadingAction(false);
    }
  }

  if (loading) return <LoadingSpinner label="Loading addresses..." />;

  return (
    <section className="addresses-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Saved locations</p>
          <h1>Addresses</h1>
        </div>
        <button type="button" className="button button--primary" onClick={openCreateForm}>
          Add Address
        </button>
      </div>

      {successMessage ? <div className="success-message">{successMessage}</div> : null}
      {errorMessage ? <ErrorMessage title="Address action failed" message={errorMessage} /> : null}

      {isFormOpen ? (
        <section className="profile-card">
          <h2>{editingAddress ? 'Edit Address' : 'Add Address'}</h2>
          <AddressForm
            initialValues={editingAddress || undefined}
            onSubmit={handleSubmit}
            onCancel={closeForm}
            isSubmitting={loadingAction}
            fieldErrors={fieldErrors}
            mode={editingAddress ? 'edit' : 'create'}
          />
        </section>
      ) : null}

      {!addresses.length ? (
        <EmptyState
          title="No saved addresses yet."
          message={
            <button type="button" className="button button--secondary" onClick={openCreateForm}>
              Add Address
            </button>
          }
        />
      ) : (
        <div className="address-grid">
          {addresses.map((address) => (
            <AddressCard
              key={address.id}
              address={address}
              onEdit={() => openEditForm(address)}
              onDelete={() => handleDelete(address)}
              onSetDefault={() => handleSetDefault(address)}
            />
          ))}
        </div>
      )}
    </section>
  );
}
