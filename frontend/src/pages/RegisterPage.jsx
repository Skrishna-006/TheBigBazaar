import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import { useAuth } from '../features/auth/context/AuthContext';
import { normalizeApiError } from '../utils/apiError';

const initialForm = {
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  phoneNumber: '',
};

export default function RegisterPage() {
  const { register, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/products';

  const [form, setForm] = useState(initialForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      navigate(from, { replace: true });
    }
  }, [from, isAuthenticated, navigate]);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function validate(values) {
    const errors = {};
    if (!values.email.trim()) {
      errors.email = 'Email is required';
    }
    if (!values.password.trim()) {
      errors.password = 'Password is required';
    } else if (values.password.trim().length < 8) {
      errors.password = 'Password must be at least 8 characters long';
    }
    if (!values.firstName.trim()) {
      errors.firstName = 'First name is required';
    }
    if (!values.lastName.trim()) {
      errors.lastName = 'Last name is required';
    }
    if (values.phoneNumber.trim() && !/^[0-9+()\-\s]{7,20}$/.test(values.phoneNumber.trim())) {
      errors.phoneNumber = 'Phone number is invalid';
    }
    return errors;
  }

  async function handleSubmit(event) {
    event.preventDefault();

    const validationErrors = validate(form);
    setFieldErrors(validationErrors);
    setErrorMessage('');

    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);
    try {
      await register({
        email: form.email.trim(),
        password: form.password,
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        phoneNumber: form.phoneNumber.trim() || undefined,
      });
      setForm(initialForm);
      navigate(from, { replace: true });
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message);
      setFieldErrors(normalized.fieldErrors || {});
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="page-card auth-card">
      <div className="auth-card__header">
        <p className="eyebrow">Create your account</p>
        <h1>Register</h1>
        <p>Join ShopSphere to save your profile and start using the authenticated features later.</p>
      </div>

      {errorMessage ? <ErrorMessage title="Registration failed" message={errorMessage} /> : null}

      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <div className="form-grid">
          <div className="form-field">
            <label htmlFor="register-first-name">First name</label>
            <input
              id="register-first-name"
              name="firstName"
              type="text"
              value={form.firstName}
              onChange={handleChange}
              autoComplete="given-name"
              aria-invalid={Boolean(fieldErrors.firstName)}
              aria-describedby={fieldErrors.firstName ? 'register-first-name-error' : undefined}
            />
            {fieldErrors.firstName ? (
              <p className="field-error" id="register-first-name-error">
                {fieldErrors.firstName}
              </p>
            ) : null}
          </div>

          <div className="form-field">
            <label htmlFor="register-last-name">Last name</label>
            <input
              id="register-last-name"
              name="lastName"
              type="text"
              value={form.lastName}
              onChange={handleChange}
              autoComplete="family-name"
              aria-invalid={Boolean(fieldErrors.lastName)}
              aria-describedby={fieldErrors.lastName ? 'register-last-name-error' : undefined}
            />
            {fieldErrors.lastName ? (
              <p className="field-error" id="register-last-name-error">
                {fieldErrors.lastName}
              </p>
            ) : null}
          </div>
        </div>

        <div className="form-field">
          <label htmlFor="register-email">Email</label>
          <input
            id="register-email"
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            autoComplete="email"
            aria-invalid={Boolean(fieldErrors.email)}
            aria-describedby={fieldErrors.email ? 'register-email-error' : undefined}
          />
          {fieldErrors.email ? (
            <p className="field-error" id="register-email-error">
              {fieldErrors.email}
            </p>
          ) : null}
        </div>

        <div className="form-field">
          <label htmlFor="register-password">Password</label>
          <input
            id="register-password"
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            autoComplete="new-password"
            aria-invalid={Boolean(fieldErrors.password)}
            aria-describedby={fieldErrors.password ? 'register-password-error' : undefined}
          />
          {fieldErrors.password ? (
            <p className="field-error" id="register-password-error">
              {fieldErrors.password}
            </p>
          ) : null}
        </div>

        <div className="form-field">
          <label htmlFor="register-phone-number">Phone number</label>
          <input
            id="register-phone-number"
            name="phoneNumber"
            type="tel"
            value={form.phoneNumber}
            onChange={handleChange}
            autoComplete="tel"
            aria-invalid={Boolean(fieldErrors.phoneNumber)}
            aria-describedby={fieldErrors.phoneNumber ? 'register-phone-number-error' : undefined}
          />
          {fieldErrors.phoneNumber ? (
            <p className="field-error" id="register-phone-number-error">
              {fieldErrors.phoneNumber}
            </p>
          ) : null}
        </div>

        <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Creating account...' : 'Register'}
        </button>
      </form>

      <p className="auth-card__footer">
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </section>
  );
}
