import { Link } from 'react-router-dom';
import React from 'react';

export default function MegaMenu({ category, brands, onClose }) {
  if (!category || !category.id) return null;

  // Render up to 8 matching brands
  const popularBrands = brands ? brands.slice(0, 8) : [];

  return (
    <div className="mega-menu" onMouseLeave={onClose}>
      <div className="mega-menu__inner">
        <div className="mega-menu__columns">
          {/* Column 1: Category Info & View All */}
          <div className="mega-menu__column">
            <h3 className="mega-menu__heading">Shop {category.name}</h3>
            <p className="mega-menu__desc">
              Discover the best products in our {category.name} collection.
            </p>
            <Link
              to={`/products?categoryId=${category.id}`}
              className="mega-menu__view-all"
              onClick={onClose}
            >
              View All {category.name} →
            </Link>
          </div>

          {/* Column 2: Popular Brands within this category (Functional) */}
          {popularBrands.length > 0 && (
            <div className="mega-menu__column">
              <h3 className="mega-menu__heading">Popular Brands</h3>
              <ul className="mega-menu__list">
                {popularBrands.map((brand) => {
                  if (!brand || !brand.id) return null;
                  return (
                    <li key={brand.id}>
                      <Link
                        to={`/products?categoryId=${category.id}&brandId=${brand.id}`}
                        className="mega-menu__link"
                        onClick={onClose}
                      >
                        {brand.name}
                      </Link>
                    </li>
                  );
                })}
              </ul>
            </div>
          )}

          {/* Column 3: Quick Links */}
          <div className="mega-menu__column">
            <h3 className="mega-menu__heading">Discover</h3>
            <ul className="mega-menu__list">
              <li>
                <Link to="/products?deals=true" className="mega-menu__link" onClick={onClose}>
                  Top Deals
                </Link>
              </li>
              <li>
                <Link to="/products" className="mega-menu__link" onClick={onClose}>
                  New Arrivals
                </Link>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
