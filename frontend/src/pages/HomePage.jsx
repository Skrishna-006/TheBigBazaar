import { Link } from 'react-router-dom';
import TopDealsSection from '../features/products/components/TopDealsSection';

export default function HomePage() {
  return (
    <div className="home-page">
      <section className="home-hero">
        <div className="home-hero__content">
          <p className="eyebrow">SHOPSPHERE</p>
          <h1>Everything You Need, All in One Place</h1>
          <p className="home-hero__text">
            Discover quality products, great prices, and a seamless shopping experience.
          </p>
          <div className="home-hero__actions">
            <Link className="button button--primary" to="/products">
              Shop Now
            </Link>
          </div>
        </div>
      </section>

      <TopDealsSection />
    </div>
  );
}

