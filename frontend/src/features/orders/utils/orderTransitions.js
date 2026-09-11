export function getAvailableTransitions(currentStatus) {
  switch (currentStatus) {
    case 'PENDING':
      return ['CONFIRMED', 'CANCELLED'];
    case 'CONFIRMED':
      return ['PROCESSING', 'CANCELLED'];
    case 'PROCESSING':
      return ['SHIPPED'];
    case 'SHIPPED':
      return ['DELIVERED'];
    case 'DELIVERED':
    case 'CANCELLED':
    default:
      return [];
  }
}
