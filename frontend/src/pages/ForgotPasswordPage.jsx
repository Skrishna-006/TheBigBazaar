import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import { normalizeApiError } from '../utils/apiError';
import { sendPasswordResetOtp, verifyPasswordResetOtp, completePasswordReset } from '../features/auth/api/authApi';

export default function ForgotPasswordPage() {
  const navigate = useNavigate();

  const [step, setStep] = useState(1);
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const [fieldErrors, setFieldErrors] = useState({});
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  async function handleSendOtp(e) {
    e.preventDefault();
    setErrorMessage('');
    setFieldErrors({});

    const emailVal = email.trim();
    if (!emailVal) {
      setFieldErrors({ email: 'Email address is required' });
      return;
    }
    if (!/^\S+@\S+\.\S+$/.test(emailVal)) {
      setFieldErrors({ email: 'Email address is invalid' });
      return;
    }

    setIsSubmitting(true);
    try {
      await sendPasswordResetOtp(emailVal);
      setStep(2);
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message);
      setFieldErrors(normalized.fieldErrors || {});
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleVerifyOtp(e) {
    e.preventDefault();
    setErrorMessage('');
    setFieldErrors({});

    if (!otp || otp.length !== 6) {
      setFieldErrors({ otp: 'Please enter a valid 6-digit OTP' });
      return;
    }

    setIsSubmitting(true);
    try {
      const { token: receivedToken } = await verifyPasswordResetOtp(email.trim(), otp);
      setToken(receivedToken);
      setStep(3);
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message);
      setFieldErrors(normalized.fieldErrors || {});
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleComplete(e) {
    e.preventDefault();
    setErrorMessage('');
    setFieldErrors({});

    if (!newPassword.trim()) {
      setFieldErrors({ newPassword: 'Password is required' });
      return;
    } else if (newPassword.length < 8) {
      setFieldErrors({ newPassword: 'Password must be at least 8 characters long' });
      return;
    }

    setIsSubmitting(true);
    try {
      await completePasswordReset({
        email: email.trim(),
        token,
        newPassword,
      });
      setSuccessMessage('Your password has been successfully reset.');
      setStep(4);
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
        <p className="eyebrow">Recover account</p>
        <h1>Reset Password</h1>
        <p>Enter your email address to receive a verification code.</p>
      </div>

      {errorMessage ? <ErrorMessage title="Error" message={errorMessage} /> : null}

      {step === 1 && (
        <form className="auth-form" onSubmit={handleSendOtp} noValidate>
          <div className="form-field">
            <label htmlFor="reset-email">Email Address</label>
            <input
              id="reset-email"
              name="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              aria-invalid={Boolean(fieldErrors.email)}
            />
            {fieldErrors.email && <p className="field-error">{fieldErrors.email}</p>}
          </div>
          <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Sending...' : 'Send OTP'}
          </button>
        </form>
      )}

      {step === 2 && (
        <form className="auth-form" onSubmit={handleVerifyOtp} noValidate>
          <div className="form-field">
            <label htmlFor="reset-otp">Enter 6-digit OTP</label>
            <input
              id="reset-otp"
              name="otp"
              type="text"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              autoComplete="one-time-code"
              maxLength="6"
              aria-invalid={Boolean(fieldErrors.otp)}
            />
            {fieldErrors.otp && <p className="field-error">{fieldErrors.otp}</p>}
          </div>
          <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Verifying...' : 'Verify OTP'}
          </button>
          <button 
            type="button" 
            className="button button--outline auth-form__submit" 
            style={{ marginTop: '0.5rem' }}
            onClick={() => setStep(1)}
            disabled={isSubmitting}
          >
            Back
          </button>
        </form>
      )}

      {step === 3 && (
        <form className="auth-form" onSubmit={handleComplete} noValidate>
          <div className="form-field">
            <label htmlFor="new-password">New Password</label>
            <input
              id="new-password"
              name="newPassword"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              autoComplete="new-password"
              aria-invalid={Boolean(fieldErrors.newPassword)}
            />
            {fieldErrors.newPassword && <p className="field-error">{fieldErrors.newPassword}</p>}
          </div>
          <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Saving...' : 'Reset Password'}
          </button>
        </form>
      )}
      
      {step === 4 && (
        <div style={{ textAlign: 'center', margin: '2rem 0' }}>
          <p style={{ color: 'var(--color-success)', fontWeight: '500', marginBottom: '1.5rem' }}>
            {successMessage}
          </p>
          <Link to="/login" className="button button--primary">Go to Login</Link>
        </div>
      )}

      {step !== 4 && (
        <p className="auth-card__footer">
          Remember your password? <Link to="/login">Login</Link>
        </p>
      )}
    </section>
  );
}
