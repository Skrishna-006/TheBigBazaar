export default function ErrorMessage({ title = 'Something went wrong', message }) {
  return (
    <div className="status-card status-card--error" role="alert">
      <strong>{title}</strong>
      {message ? <p>{message}</p> : null}
    </div>
  );
}
