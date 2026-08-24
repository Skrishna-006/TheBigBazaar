export default function EmptyState({ title, message }) {
  return (
    <div className="status-card">
      <strong>{title}</strong>
      {message ? <p>{message}</p> : null}
    </div>
  );
}
