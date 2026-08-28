import { NavLink } from 'react-router-dom';

const items = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/categories', label: 'Categories' },
  { to: '/admin/brands', label: 'Brands' },
  { to: '/admin/products', label: 'Products' },
  { to: '/admin/inventory', label: 'Inventory' },
  { to: '/admin/orders', label: 'Orders' },
];

export default function AdminSidebar() {
  return (
    <aside className="admin-sidebar">
      <div className="admin-sidebar__brand">Admin Panel</div>
      <nav className="admin-sidebar__nav" aria-label="Admin navigation">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) => `admin-sidebar__link${isActive ? ' admin-sidebar__link--active' : ''}`}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
