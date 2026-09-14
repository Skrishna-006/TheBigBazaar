import { Link } from 'react-router-dom';

const year = new Date().getFullYear();

export default function Footer() {
  return (
    <footer className="site-footer-premium">
      <div className="site-footer-premium__inner">
        <div className="site-footer-premium__brand">
          <h2 className="site-footer-premium__logo">TheBigBazaar</h2>
          <p className="site-footer-premium__tagline">"Everything you need, all in one place."</p>
          <div className="site-footer-premium__newsletter">
            <h3>Stay in the loop</h3>
            <p>Get updates about new products and special offers.</p>
            <div className="newsletter-form-visual">
              <input type="email" placeholder="Enter your email" disabled aria-label="Email address" />
              <button type="button" disabled>Subscribe</button>
            </div>
          </div>
        </div>

        <div className="site-footer-premium__columns">
          <div className="site-footer-premium__links">
            <h3>Shop</h3>
            <ul>
              <li><Link to="/products">Products</Link></li>
              <li><Link to="/products">Categories</Link></li>
              <li><Link to="/products">New Arrivals</Link></li>
              <li><Link to="/products">Deals</Link></li>
            </ul>
          </div>

          <div className="site-footer-premium__links">
            <h3>Customer Care</h3>
            <ul>
              <li><span className="text-muted">Contact Us</span></li>
              <li><span className="text-muted">Help Center</span></li>
              <li><span className="text-muted">Shipping Information</span></li>
              <li><span className="text-muted">Returns & Refunds</span></li>
            </ul>
          </div>

          <div className="site-footer-premium__links">
            <h3>Account</h3>
            <ul>
              <li><Link to="/profile">My Profile</Link></li>
              <li><Link to="/orders">My Orders</Link></li>
              <li><Link to="/wishlist">Wishlist</Link></li>
              <li><Link to="/addresses">Addresses</Link></li>
            </ul>
          </div>

          <div className="site-footer-premium__links">
            <h3>Company</h3>
            <ul>
              <li><span className="text-muted">About TheBigBazaar</span></li>
              <li><span className="text-muted">Privacy Policy</span></li>
              <li><span className="text-muted">Terms & Conditions</span></li>
            </ul>
          </div>
        </div>
      </div>

      <div className="site-footer-premium__bottom">
        <div className="site-footer-premium__bottom-inner">
          <p>&copy; {year} TheBigBazaar. All rights reserved.</p>
          <p className="site-footer-premium__bottom-right">Secure Payments &bull; Fast Delivery &bull; Trusted Shopping</p>
        </div>
      </div>
    </footer>
  );
}
