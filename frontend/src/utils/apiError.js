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
  return {
    status: status ?? 0,
    message: data?.message || data?.error || fallback.message,
    fieldErrors: data?.fieldErrors,
  };
}
