export function calculateDiscount(originalPrice, currentPrice) {
  if (
    originalPrice == null ||
    currentPrice == null ||
    originalPrice === '' ||
    currentPrice === ''
  ) {
    return null;
  }

  const orig = Number(originalPrice);
  const curr = Number(currentPrice);

  if (
    Number.isFinite(orig) &&
    Number.isFinite(curr) &&
    orig > 0 &&
    curr >= 0 &&
    orig > curr
  ) {
    const discount = Math.round(((orig - curr) / orig) * 100);
    return discount > 0 ? discount : null;
  }
  return null;
}

export function getTopDeals(products, limit = 8) {
  if (!Array.isArray(products)) {
    return [];
  }

  return products
    .filter((product) => {
      if (
        product?.originalPrice == null ||
        product?.price == null ||
        product?.originalPrice === '' ||
        product?.price === ''
      ) {
        return false;
      }
      const orig = Number(product.originalPrice);
      const curr = Number(product.price);
      return (
        Number.isFinite(orig) &&
        Number.isFinite(curr) &&
        orig > 0 &&
        curr >= 0 &&
        orig > curr
      );
    })
    .map((product) => {
      const orig = Number(product.originalPrice);
      const curr = Number(product.price);
      const discount = Math.round(((orig - curr) / orig) * 100);
      return { product, discount };
    })
    .filter((item) => item.discount > 0)
    .sort((a, b) => {
      // 1. Highest discount percentage
      if (b.discount !== a.discount) {
        return b.discount - a.discount;
      }
      // 2. Higher rating
      const ratingA = Number(a.product.rating) || 0;
      const ratingB = Number(b.product.rating) || 0;
      if (ratingB !== ratingA) {
        return ratingB - ratingA;
      }
      // 3. Higher review count
      const reviewsA = Number(a.product.reviewCount) || 0;
      const reviewsB = Number(b.product.reviewCount) || 0;
      return reviewsB - reviewsA;
    })
    .slice(0, limit)
    .map((item) => item.product);
}

export function calculateDeliveryDate(deliveryDays) {
  if (deliveryDays != null && deliveryDays >= 0) {
    const date = new Date();
    date.setDate(date.getDate() + deliveryDays);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }
  return null;
}
