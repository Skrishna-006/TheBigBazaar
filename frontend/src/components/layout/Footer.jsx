const year = new Date().getFullYear();

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="site-footer__inner">
        <p>ShopSphere © {year}</p>
        <p>Built with React, Vite, and React Router.</p>
      </div>
    </footer>
  );
}
