export default function LoadingSpinner({ label = 'Loading...' }) {
  return (
    <div className="status-card" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <p>{label}</p>
    </div>
  );
}
