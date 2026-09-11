export function normalizeApiError(error) {
  const fallback = {
    status: 0,
    message: 'Unexpected error',
    fieldErrors: undefined,
  };

  if (!error?.response) {
    return {
      ...fallback,
      message: error?.message || fallback.message,
    };
  }

  const { status, data } = error.response;
  const message =
    (typeof data === 'string' && data.trim()) ||
    data?.message ||
    data?.error ||
    data?.detail ||
    data?.title ||
    fallback.message;

  return {
    status: status ?? 0,
    message,
    fieldErrors: data?.fieldErrors,
  };
}
