import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import { resetPassword } from '../features/auth/api/authApi';
import { normalizeApiError } from '../utils/apiError';

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const email = searchParams.get('email');

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [fieldErrors, setFieldErrors] = useState({});
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!token || !email) {
      setErrorMessage('Invalid or missing reset token/email.');
    }
  }, [token, email]);

  async function handleSubmit(e) {
    e.preventDefault();
    setErrorMessage('');
    setFieldErrors({});

    if (!token || !email) {
      setErrorMessage('Invalid or missing reset token/email.');
      return;
    }

    const errors = {};
    if (!newPassword.trim()) errors.newPassword = 'New password is required';
    else if (newPassword.length < 8) errors.newPassword = 'Password must be at least 8 characters long';
    
    if (newPassword !== confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setIsSubmitting(true);
    try {
      await resetPassword({
        email: email,
        token: token,
        newPassword
      });
      navigate('/login', { state: { message: 'Password has been reset successfully. Please log in.' } });
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
        <h1>Set New Password</h1>
        <p>Please enter your new password below.</p>
      </div>

      {errorMessage && <ErrorMessage title="Error" message={errorMessage} />}

      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <div className="form-field">
          <label htmlFor="new-password">New Password</label>
          <input
            id="new-password"
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            autoComplete="new-password"
            aria-invalid={Boolean(fieldErrors.newPassword)}
          />
          {fieldErrors.newPassword && <p className="field-error">{fieldErrors.newPassword}</p>}
        </div>
        
        <div className="form-field">
          <label htmlFor="confirm-password">Confirm Password</label>
          <input
            id="confirm-password"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            autoComplete="new-password"
            aria-invalid={Boolean(fieldErrors.confirmPassword)}
          />
          {fieldErrors.confirmPassword && <p className="field-error">{fieldErrors.confirmPassword}</p>}
        </div>

        <button 
          className="button button--primary auth-form__submit" 
          type="submit" 
          disabled={isSubmitting || !token || !email}
        >
          {isSubmitting ? 'Resetting...' : 'Reset Password'}
        </button>
      </form>

      <p className="auth-card__footer">
        Remembered your password? <Link to="/login">Login</Link>
      </p>
    </section>
  );
}
