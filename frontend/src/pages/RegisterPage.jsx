import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import { useAuth } from '../features/auth/context/AuthContext';
import { normalizeApiError } from '../utils/apiError';

export default function RegisterPage() {
  const { register, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/products';

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });

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

  async function handleComplete(e) {
    e.preventDefault();
    setErrorMessage('');
    setFieldErrors({});

    const errors = {};
    const emailVal = form.email.trim();
    if (!form.firstName.trim()) errors.firstName = 'First name is required';
    if (!form.lastName.trim()) errors.lastName = 'Last name is required';
    if (!emailVal) {
      errors.email = 'Email address is required';
    } else if (!/^\S+@\S+\.\S+$/.test(emailVal)) {
      errors.email = 'Email address is invalid';
    }
    if (!form.password.trim()) errors.password = 'Password is required';
    else if (form.password.length < 8) errors.password = 'Password must be at least 8 characters long';

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setIsSubmitting(true);
    try {
      await register({
        email: emailVal,
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        password: form.password,
      });
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
        <h1>Register</h1>
        <p>Join TheBigBazaar using your email address.</p>
      </div>

      {errorMessage ? <ErrorMessage title="Registration failed" message={errorMessage} /> : null}

      <form className="auth-form" onSubmit={handleComplete} noValidate>
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
            />
            {fieldErrors.firstName && <p className="field-error">{fieldErrors.firstName}</p>}
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
            />
            {fieldErrors.lastName && <p className="field-error">{fieldErrors.lastName}</p>}
          </div>
        </div>

        <div className="form-field">
          <label htmlFor="register-email">Email address</label>
          <input
            id="register-email"
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            autoComplete="email"
            aria-invalid={Boolean(fieldErrors.email)}
          />
          {fieldErrors.email && <p className="field-error">{fieldErrors.email}</p>}
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
          />
          {fieldErrors.password && <p className="field-error">{fieldErrors.password}</p>}
        </div>

        <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Creating account...' : 'Register'}
        </button>

        <div style={{ textAlign: 'center', margin: '1rem 0' }}>OR</div>

        <a 
          href={`${import.meta.env.VITE_API_BASE_URL ? import.meta.env.VITE_API_BASE_URL.replace('/api/v1', '') : 'http://localhost:8080'}/oauth2/authorization/google`} 
          className="button button--outline" 
          style={{ display: 'block', textAlign: 'center', textDecoration: 'none' }}
        >
          Continue with Google
        </a>
      </form>

      <p className="auth-card__footer">
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </section>
  );
}
