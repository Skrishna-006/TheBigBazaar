import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import { useAuth } from '../features/auth/context/AuthContext';
import { sendRegistrationOtp, verifyRegistrationOtp } from '../features/auth/api/authApi';
import { normalizeApiError } from '../utils/apiError';

export default function RegisterPage() {
  const { completeRegistration, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/products';

  const [step, setStep] = useState(1);
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [token, setToken] = useState('');
  
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
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

  async function handleSendOtp(e) {
    e.preventDefault();
    setErrorMessage('');
    setFieldErrors({});

    const errors = {};
    const emailVal = email.trim();
    if (!form.firstName.trim()) errors.firstName = 'First name is required';
    if (!form.lastName.trim()) errors.lastName = 'Last name is required';
    if (!emailVal) {
      errors.email = 'Email address is required';
    } else if (!/^\S+@\S+\.\S+$/.test(emailVal)) {
      errors.email = 'Email address is invalid';
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setIsSubmitting(true);
    try {
      await sendRegistrationOtp(emailVal);
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
      const { token: receivedToken } = await verifyRegistrationOtp(email.trim(), otp);
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

    const errors = {};
    if (!form.password.trim()) errors.password = 'Password is required';
    else if (form.password.length < 8) errors.password = 'Password must be at least 8 characters long';

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setIsSubmitting(true);
    try {
      await completeRegistration({
        email: email.trim(),
        token,
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
        <p className="eyebrow">Step {step} of 3</p>
        <h1>Register</h1>
        <p>Join TheBigBazaar using your email address.</p>
      </div>

      {errorMessage ? <ErrorMessage title="Registration failed" message={errorMessage} /> : null}

      {step === 1 && (
        <form className="auth-form" onSubmit={handleSendOtp} noValidate>
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
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              aria-invalid={Boolean(fieldErrors.email)}
            />
            {fieldErrors.email ? (
              <p className="field-error" id="register-email-error">
                {fieldErrors.email}
              </p>
            ) : null}
          </div>
          <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Sending...' : 'Send OTP'}
          </button>
        </form>
      )}

      {step === 2 && (
        <form className="auth-form" onSubmit={handleVerifyOtp} noValidate>
          <div className="form-field">
            <label htmlFor="register-otp">Enter 6-digit OTP</label>
            <input
              id="register-otp"
              name="otp"
              type="text"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              autoComplete="one-time-code"
              maxLength="6"
              aria-invalid={Boolean(fieldErrors.otp)}
              aria-describedby={fieldErrors.otp ? 'register-otp-error' : undefined}
            />
            {fieldErrors.otp ? (
              <p className="field-error" id="register-otp-error">
                {fieldErrors.otp}
              </p>
            ) : null}
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
            {isSubmitting ? 'Creating account...' : 'Complete Registration'}
          </button>
        </form>
      )}

      <p className="auth-card__footer">
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </section>
  );
}
