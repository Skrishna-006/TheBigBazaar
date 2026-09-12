import { NavLink, Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../features/auth/context/AuthContext';
import { useState, useRef, useEffect } from 'react';
import SearchBar from './SearchBar';
import CategoryNav from './CategoryNav';

// Icons
const SearchIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#64748b" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8"></circle>
    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
  </svg>
);

const XIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#64748b" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="6" x2="6" y2="18"></line>
    <line x1="6" y1="6" x2="18" y2="18"></line>
  </svg>
);

const UserIcon = () => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
    <circle cx="12" cy="7" r="4"></circle>
  </svg>
);

const CartIcon = () => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="9" cy="21" r="1"></circle>
    <circle cx="20" cy="21" r="1"></circle>
    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
  </svg>
);

const PackageIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
    <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
    <line x1="12" y1="22.08" x2="12" y2="12"></line>
  </svg>
);

const MapPinIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
    <circle cx="12" cy="10" r="3"></circle>
  </svg>
);

const HeartIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
  </svg>
);

const LogoutIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
    <polyline points="16 17 21 12 16 7"></polyline>
    <line x1="21" y1="12" x2="9" y2="12"></line>
  </svg>
);

export default function Header() {
  const { isAuthenticated, logout, isAdmin } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const dropdownRef = useRef(null);
  const timeoutRef = useRef(null);
  
  const navigate = useNavigate();

  const handleMouseEnter = () => {
    clearTimeout(timeoutRef.current);
    setDropdownOpen(true);
  };

  const handleMouseLeave = () => {
    timeoutRef.current = setTimeout(() => {
      setDropdownOpen(false);
    }, 150);
  };

  // Close when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    }
    
    function handleEscape(event) {
      if (event.key === 'Escape') {
        setDropdownOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleEscape);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEscape);
    };
  }, []);

  const closeDropdown = () => setDropdownOpen(false);
  const closeMobileMenu = () => setMobileMenuOpen(false);

  const handleLogout = () => {
    closeDropdown();
    closeMobileMenu();
    logout();
  };

  return (
    <header className="site-header-premium">
      <div className="site-header-premium__inner">
        {/* Mobile Hamburger */}
        <button 
          className="site-header-premium__hamburger" 
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle menu"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="3" y1="12" x2="21" y2="12"></line>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <line x1="3" y1="18" x2="21" y2="18"></line>
          </svg>
        </button>

        {/* LEFT: BRAND & NAV */}
        <div className="site-header-premium__left">
          <Link to="/" className="site-header-premium__brand" aria-label="ShopSphere home" onClick={closeMobileMenu}>
            ShopSphere
          </Link>
          <nav className={`site-header-premium__nav ${mobileMenuOpen ? 'site-header-premium__nav--open' : ''}`} aria-label="Primary">
            <NavLink to="/" className={({ isActive }) => `site-nav-premium__link${isActive ? ' site-nav-premium__link--active' : ''}`} end onClick={closeMobileMenu}>Home</NavLink>
            <NavLink to="/products" className={({ isActive }) => `site-nav-premium__link${isActive ? ' site-nav-premium__link--active' : ''}`} onClick={closeMobileMenu}>Products</NavLink>
            {isAdmin() && isAuthenticated ? (
              <NavLink to="/admin" className={({ isActive }) => `site-nav-premium__link${isActive ? ' site-nav-premium__link--active' : ''}`} onClick={closeMobileMenu}>Admin</NavLink>
            ) : null}

            {/* Mobile Categories Accordion */}
            {mobileMenuOpen && (
              <CategoryNav mobileMenuOpen={mobileMenuOpen} closeMobileMenu={closeMobileMenu} />
            )}

            {/* Fallback Auth links for mobile inside menu when logged out */}
            {!isAuthenticated && mobileMenuOpen && (
              <div className="site-header-premium__mobile-auth">
                <NavLink to="/login" className="site-nav-premium__link" onClick={closeMobileMenu}>Login</NavLink>
                <NavLink to="/register" className="site-nav-premium__link" onClick={closeMobileMenu}>Register</NavLink>
              </div>
            )}
          </nav>
        </div>

        <div className="site-header-premium__search">
          <SearchBar />
        </div>

        {/* RIGHT: ICONS */}
        <div className="site-header-premium__right">
          {!isAuthenticated ? (
            <div className="site-header-premium__auth-links">
              <NavLink to="/login" className="site-nav-premium__link">Login</NavLink>
              <NavLink to="/register" className="site-nav-premium__link">Register</NavLink>
            </div>
          ) : (
            <div 
              className="site-header-premium__profile-area" 
              ref={dropdownRef}
              onMouseEnter={handleMouseEnter}
              onMouseLeave={handleMouseLeave}
            >
              <button 
                className={`site-header-premium__icon-btn ${dropdownOpen ? 'site-header-premium__icon-btn--active' : ''}`}
                aria-label="Account"
                aria-expanded={dropdownOpen}
                onClick={() => setDropdownOpen(!dropdownOpen)}
              >
                <UserIcon />
              </button>

              {dropdownOpen && (
                <div className="site-header-premium__dropdown">
                  <div className="site-header-premium__dropdown-content">
                    <Link to="/profile" className="dropdown-item" onClick={closeDropdown}>
                      <UserIcon /> <span>My Profile</span>
                    </Link>
                    <Link to="/orders" className="dropdown-item" onClick={closeDropdown}>
                      <PackageIcon /> <span>Orders</span>
                    </Link>
                    <Link to="/wishlist" className="dropdown-item" onClick={closeDropdown}>
                      <HeartIcon /> <span>Wishlist</span>
                    </Link>
                    <Link to="/addresses" className="dropdown-item" onClick={closeDropdown}>
                      <MapPinIcon /> <span>Saved Addresses</span>
                    </Link>
                    <div className="dropdown-divider"></div>
                    <button className="dropdown-item dropdown-item--logout" onClick={handleLogout}>
                      <LogoutIcon /> <span>Logout</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}

          <Link to="/cart" className="site-header-premium__icon-btn" aria-label="Cart">
            <CartIcon />
          </Link>
        </div>
      </div>
      
      {/* Desktop Category Navigation Bar */}
      <CategoryNav />
    </header>
  );
}
