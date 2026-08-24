import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import ErrorMessage from '../components/common/ErrorMessage';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { getProductById } from '../features/products/api/productApi';
import ProductPrice from '../features/products/components/ProductPrice';
import { normalizeApiError } from '../utils/apiError';

const placeholderImage =
  'data:image/svg+xml;charset=UTF-8,%3Csvg xmlns%3D%22http%3A//www.w3.org/2000/svg%22 width%3D%22960%22 height%3D%22720%22 viewBox%3D%220 0 960 720%22%3E%3Crect width%3D%22960%22 height%3D%22720%22 rx%3D%2232%22 fill%3D%22%23e2e8f0%22/%3E%3Cpath d%3D%22M240 500l120-140 92 106 80-88 188 222H240z%22 fill%3D%22%23cbd5e1%22/%3E%3Ccircle cx%3D%22364%22 cy%3D%22306%22 r%3D%2264%22 fill%3D%22%23cbd5e1%22/%3E%3C/svg%3E';

export default function ProductDetailsPage() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let mounted = true;

    async function loadProduct() {
      setIsLoading(true);
      setErrorMessage('');
      setNotFound(false);

      try {
        const data = await getProductById(id);
        if (mounted) {
          setProduct(data);
        }
      } catch (error) {
        const normalized = normalizeApiError(error);
        if (mounted) {
          if (normalized.status === 404) {
            setNotFound(true);
          } else {
            setErrorMessage(normalized.message);
          }
          setProduct(null);
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    loadProduct();

    return () => {
      mounted = false;
    };
  }, [id]);

  if (isLoading) {
    return <LoadingSpinner label="Loading product..." />;
  }

  if (notFound) {
    return <ErrorMessage title="Product not found" message="This product is no longer available." />;
  }

  if (errorMessage) {
    return <ErrorMessage title="Unable to load product" message={errorMessage} />;
  }

  if (!product) {
    return null;
  }

  return (
    <section className="product-detail">
      <Link className="product-detail__back" to="/products">
        ← Back to products
      </Link>
      <div className="product-detail__card">
        <div className="product-detail__media">
          <img
            className="product-detail__image"
            src={product.imageUrl || placeholderImage}
            alt={`${product.name} product image`}
            onError={(event) => {
              event.currentTarget.src = placeholderImage;
            }}
          />
        </div>
        <div className="product-detail__info">
          <p className="eyebrow">Product details</p>
          <h1>{product.name}</h1>
          <div className="product-detail__meta">
            <span>{product.brand?.name || 'Brand coming soon'}</span>
            <span>{product.category?.name || 'Category coming soon'}</span>
            <span>SKU: {product.sku}</span>
          </div>
          <ProductPrice value={product.price} />
          {product.description ? <p className="product-detail__description">{product.description}</p> : null}
          <p className="product-detail__note">Cart coming soon.</p>
        </div>
      </div>
    </section>
  );
}
