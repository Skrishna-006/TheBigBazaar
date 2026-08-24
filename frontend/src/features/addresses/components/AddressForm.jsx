import { useEffect, useState } from 'react';

const emptyForm = {
  firstName: '',
  lastName: '',
  phone: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  postalCode: '',
  country: '',
  defaultAddress: false,
};

export default function AddressForm({ initialValues, onSubmit, onCancel, isSubmitting, fieldErrors = {}, mode = 'create' }) {
  const [form, setForm] = useState(emptyForm);

  useEffect(() => {
    setForm({
      ...emptyForm,
      ...initialValues,
      defaultAddress: Boolean(initialValues?.defaultAddress),
    });
  }, [initialValues]);

  function handleChange(event) {
    const { name, value, type, checked } = event.target;
    setForm((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit(form);
  }

  return (
    <form className="profile-form" onSubmit={handleSubmit} noValidate>
      <div className="form-grid">
        {[
          ['firstName', 'First name'],
          ['lastName', 'Last name'],
        ].map(([field, label]) => (
          <div className="form-field" key={field}>
            <label htmlFor={`address-${field}`}>{label}</label>
            <input
              id={`address-${field}`}
              name={field}
              value={form[field]}
              onChange={handleChange}
              aria-invalid={Boolean(fieldErrors[field])}
            />
            {fieldErrors[field] ? <p className="field-error">{fieldErrors[field]}</p> : null}
          </div>
        ))}
      </div>

      <div className="form-field">
        <label htmlFor="address-phone">Phone</label>
        <input id="address-phone" name="phone" value={form.phone} onChange={handleChange} aria-invalid={Boolean(fieldErrors.phone)} />
        {fieldErrors.phone ? <p className="field-error">{fieldErrors.phone}</p> : null}
      </div>

      {[
        ['addressLine1', 'Address line 1'],
        ['addressLine2', 'Address line 2'],
        ['city', 'City'],
        ['state', 'State'],
        ['postalCode', 'Postal code'],
        ['country', 'Country'],
      ].map(([field, label]) => (
        <div className="form-field" key={field}>
          <label htmlFor={`address-${field}`}>{label}</label>
          <input
            id={`address-${field}`}
            name={field}
            value={form[field]}
            onChange={handleChange}
            aria-invalid={Boolean(fieldErrors[field])}
          />
          {fieldErrors[field] ? <p className="field-error">{fieldErrors[field]}</p> : null}
        </div>
      ))}

      <label className="checkbox-row" htmlFor="address-default">
        <input id="address-default" name="defaultAddress" type="checkbox" checked={form.defaultAddress} onChange={handleChange} />
        Mark as default address
      </label>

      <div className="form-actions">
        {onCancel ? (
          <button type="button" className="button button--secondary" onClick={onCancel}>
            Cancel
          </button>
        ) : null}
        <button type="submit" className="button button--primary" disabled={isSubmitting}>
          {isSubmitting ? (mode === 'create' ? 'Adding...' : 'Saving...') : mode === 'create' ? 'Add Address' : 'Save Address'}
        </button>
      </div>
    </form>
  );
}
