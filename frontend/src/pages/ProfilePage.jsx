import { useEffect, useState } from 'react';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { useAuth } from '../features/auth/context/AuthContext';
import { changePassword, getCurrentUser, updateProfile } from '../features/users/api/userApi';
import { normalizeApiError } from '../utils/apiError';

export default function ProfilePage() {
  const { user, syncUser } = useAuth();
  const [profile, setProfile] = useState(user);
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    avatarUrl: '',
  });
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [fieldErrors, setFieldErrors] = useState({});
  const [message, setMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [submittingProfile, setSubmittingProfile] = useState(false);
  const [submittingPassword, setSubmittingPassword] = useState(false);

  useEffect(() => {
    let mounted = true;

    async function loadProfile() {
      setLoading(true);
      try {
        const current = await getCurrentUser();
        if (mounted) {
          setProfile(current);
          syncUser(current);
          setForm({
            firstName: current.firstName || '',
            lastName: current.lastName || '',
            phoneNumber: current.phoneNumber || '',
            avatarUrl: current.avatarUrl || '',
          });
        }
      } catch (error) {
        if (mounted) {
          setErrorMessage(normalizeApiError(error).message);
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    loadProfile();
    return () => {
      mounted = false;
    };
  }, [syncUser]);

  function handleProfileChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function handlePasswordChange(event) {
    const { name, value } = event.target;
    setPasswordForm((current) => ({ ...current, [name]: value }));
  }

  function validateProfile(values) {
    const errors = {};
    if (!values.firstName.trim()) errors.firstName = 'First name is required';
    if (!values.lastName.trim()) errors.lastName = 'Last name is required';
    if (values.phoneNumber && !/^[0-9+()\-\s]{7,20}$/.test(values.phoneNumber.trim())) errors.phoneNumber = 'Phone number is invalid';
    if (values.avatarUrl && values.avatarUrl.length > 500) errors.avatarUrl = 'Avatar URL is too long';
    return errors;
  }

  function validatePassword(values) {
    const errors = {};
    if (!values.currentPassword.trim()) errors.currentPassword = 'Current password is required';
    if (!values.newPassword.trim()) errors.newPassword = 'New password is required';
    else if (values.newPassword.length < 8) errors.newPassword = 'New password must be at least 8 characters long';
    if (!values.confirmPassword.trim()) errors.confirmPassword = 'Please confirm your new password';
    if (values.newPassword && values.confirmPassword && values.newPassword !== values.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }
    return errors;
  }

  async function handleProfileSubmit(event) {
    event.preventDefault();
    const errors = validateProfile(form);
    setFieldErrors(errors);
    setMessage('');
    setErrorMessage('');
    if (Object.keys(errors).length) return;

    setSubmittingProfile(true);
    try {
      const updated = await updateProfile({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        phoneNumber: form.phoneNumber.trim() || undefined,
        avatarUrl: form.avatarUrl.trim() || undefined,
      });
      setProfile(updated);
      syncUser(updated);
      setMessage('Profile updated successfully.');
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message);
      setFieldErrors(normalized.fieldErrors || {});
    } finally {
      setSubmittingProfile(false);
    }
  }

  async function handlePasswordSubmit(event) {
    event.preventDefault();
    const errors = validatePassword(passwordForm);
    setFieldErrors(errors);
    setMessage('');
    setErrorMessage('');
    if (Object.keys(errors).length) return;

    setSubmittingPassword(true);
    try {
      await changePassword(passwordForm);
      setMessage('Password changed successfully.');
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error) {
      const normalized = normalizeApiError(error);
      setErrorMessage(normalized.message);
      setFieldErrors(normalized.fieldErrors || {});
    } finally {
      setSubmittingPassword(false);
    }
  }

  if (loading) return <LoadingSpinner label="Loading profile..." />;

  return (
    <section className="profile-page">
      <div className="profile-card">
        <p className="eyebrow">Your account</p>
        <h1>Profile</h1>
        {message ? <div className="success-message">{message}</div> : null}
        {errorMessage ? <ErrorMessage title="Unable to update profile" message={errorMessage} /> : null}
        <div className="profile-summary">
          <p><strong>Name:</strong> {profile?.firstName} {profile?.lastName}</p>
          <p><strong>Email:</strong> {profile?.email}</p>
          <p><strong>Phone:</strong> {profile?.phoneNumber || 'Not provided'}</p>
          <p><strong>Role:</strong> {profile?.role}</p>
          <p><strong>Avatar:</strong> {profile?.avatarUrl || 'Not provided'}</p>
        </div>
      </div>

      <div className="profile-grid">
        <section className="profile-card">
          <h2>Edit Profile</h2>
          <form className="profile-form" onSubmit={handleProfileSubmit} noValidate>
            {[
              ['firstName', 'First name'],
              ['lastName', 'Last name'],
              ['phoneNumber', 'Phone number'],
              ['avatarUrl', 'Avatar URL'],
            ].map(([field, label]) => (
              <div className="form-field" key={field}>
                <label htmlFor={`profile-${field}`}>{label}</label>
                <input
                  id={`profile-${field}`}
                  name={field}
                  value={form[field]}
                  onChange={handleProfileChange}
                  aria-invalid={Boolean(fieldErrors[field])}
                />
                {fieldErrors[field] ? <p className="field-error">{fieldErrors[field]}</p> : null}
              </div>
            ))}
            <button className="button button--primary" type="submit" disabled={submittingProfile}>
              {submittingProfile ? 'Saving...' : 'Save Profile'}
            </button>
          </form>
        </section>

        <section className="profile-card">
          <h2>Change Password</h2>
          <form className="profile-form" onSubmit={handlePasswordSubmit} noValidate>
            {[
              ['currentPassword', 'Current password'],
              ['newPassword', 'New password'],
              ['confirmPassword', 'Confirm password'],
            ].map(([field, label]) => (
              <div className="form-field" key={field}>
                <label htmlFor={`password-${field}`}>{label}</label>
                <input
                  id={`password-${field}`}
                  name={field}
                  type="password"
                  value={passwordForm[field]}
                  onChange={handlePasswordChange}
                  aria-invalid={Boolean(fieldErrors[field])}
                />
                {fieldErrors[field] ? <p className="field-error">{fieldErrors[field]}</p> : null}
              </div>
            ))}
            <button className="button button--primary" type="submit" disabled={submittingPassword}>
              {submittingPassword ? 'Updating...' : 'Change Password'}
            </button>
          </form>
        </section>
      </div>
    </section>
  );
}
