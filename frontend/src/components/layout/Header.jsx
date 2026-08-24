import { NavLink, Link } from 'react-router-dom';
import { useAuth } from '../../features/auth/context/AuthContext';

export default function Header() {
  const { user, isAuthenticated, logout, isAdmin } = useAuth();

  const navItems = [
    { to: '/', label: 'Home', end: true },
    { to: '/products', label: 'Products' },
    { to: '/cart', label: 'Cart' },
  ];

  return (
    <header className="site-header">
      <div className="site-header__inner">
        <Link to="/" className="brand-mark" aria-label="ShopSphere home">
          ShopSphere
        </Link>
        <nav className="site-nav" aria-label="Primary">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `site-nav__link${isActive ? ' site-nav__link--active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
          {!isAuthenticated ? (
            <>
              <NavLink to="/login" className={({ isActive }) => `site-nav__link${isActive ? ' site-nav__link--active' : ''}`}>
                Login
              </NavLink>
              <NavLink to="/register" className={({ isActive }) => `site-nav__link${isActive ? ' site-nav__link--active' : ''}`}>
                Register
              </NavLink>
            </>
          ) : (
            <>
              <NavLink to="/profile" className={({ isActive }) => `site-nav__link${isActive ? ' site-nav__link--active' : ''}`}>
                Profile
              </NavLink>
              <NavLink to="/addresses" className={({ isActive }) => `site-nav__link${isActive ? ' site-nav__link--active' : ''}`}>
                Addresses
              </NavLink>
              {isAdmin() ? (
                <NavLink to="/admin" className={({ isActive }) => `site-nav__link${isActive ? ' site-nav__link--active' : ''}`}>
                  Admin
                </NavLink>
              ) : null}
              <button type="button" className="site-nav__button" onClick={logout}>
                Logout
              </button>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
