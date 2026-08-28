import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import { useAuth } from '../features/auth/context/AuthContext';
import { normalizeApiError } from '../utils/apiError';

const initialForm = {
  email: '',
  password: '',
};

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
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
      await login({
        email: form.email.trim(),
        password: form.password,
      });
      setForm(initialForm);
      navigate(from, { replace: true });
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message || 'Invalid email or password.');
      setFieldErrors(normalized.fieldErrors || {});
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="page-card auth-card">
      <div className="auth-card__header">
        <p className="eyebrow">Welcome back</p>
        <h1>Login</h1>
        <p>Sign in to manage your profile, addresses, and orders later.</p>
      </div>

      {errorMessage ? <ErrorMessage title="Login failed" message={errorMessage} /> : null}

      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <div className="form-field">
          <label htmlFor="login-email">Email</label>
          <input
            id="login-email"
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            autoComplete="email"
            aria-invalid={Boolean(fieldErrors.email)}
            aria-describedby={fieldErrors.email ? 'login-email-error' : undefined}
          />
          {fieldErrors.email ? (
            <p className="field-error" id="login-email-error">
              {fieldErrors.email}
            </p>
          ) : null}
        </div>

        <div className="form-field">
          <label htmlFor="login-password">Password</label>
          <input
            id="login-password"
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            autoComplete="current-password"
            aria-invalid={Boolean(fieldErrors.password)}
            aria-describedby={fieldErrors.password ? 'login-password-error' : undefined}
          />
          {fieldErrors.password ? (
            <p className="field-error" id="login-password-error">
              {fieldErrors.password}
            </p>
          ) : null}
        </div>

        <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Signing in...' : 'Login'}
        </button>
      </form>

      <p className="auth-card__footer">
        New here? <Link to="/register">Create an account</Link>
      </p>
    </section>
  );
}
