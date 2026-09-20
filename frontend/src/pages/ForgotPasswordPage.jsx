import { useState } from 'react';
import { Link } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import { requestPasswordReset } from '../features/auth/api/authApi';
import { normalizeApiError } from '../utils/apiError';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  async function handleRequestReset(e) {
    e.preventDefault();
    setErrorMessage('');
    setFieldErrors({});
    setSuccessMessage('');

    const emailVal = email.trim();
    if (!emailVal) {
      setFieldErrors({ email: 'Email address is required' });
      return;
    }

    setIsSubmitting(true);
    try {
      await requestPasswordReset(emailVal);
      setSuccessMessage('If an account exists for this email, a password reset link has been sent.');
      setEmail('');
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
        <h1>Forgot Password</h1>
        <p>Reset your password to regain access to your account.</p>
      </div>

      {errorMessage && <ErrorMessage title="Error" message={errorMessage} />}
      {successMessage && <div className="alert alert--success">{successMessage}</div>}

      <form className="auth-form" onSubmit={handleRequestReset} noValidate>
        <div className="form-field">
          <label htmlFor="reset-email">Email address</label>
          <input
            id="reset-email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
            aria-invalid={Boolean(fieldErrors.email)}
          />
          {fieldErrors.email && <p className="field-error">{fieldErrors.email}</p>}
        </div>
        <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Sending Request...' : 'Request Reset'}
        </button>
      </form>

      <p className="auth-card__footer">
        Remembered your password? <Link to="/login">Login</Link>
      </p>
    </section>
  );
}
