import { Link } from 'react-router-dom';

export default function HomePage() {
  return (
    <section className="hero-card">
      <p className="eyebrow">React frontend foundation</p>
      <h1>ShopSphere</h1>
      <p className="hero-card__text">
        A clean storefront foundation for products, accounts, and checkout features that will be added in later phases.
      </p>
      <div className="hero-card__actions">
        <Link className="button button--primary" to="/products">
          Shop Products
        </Link>
        <Link className="button button--secondary" to="/register">
          Create Account
        </Link>
      </div>
    </section>
  );
}
