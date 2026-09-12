import React, { useState, useEffect, useRef, useMemo } from 'react';
import { NavLink } from 'react-router-dom';
import { getCategories } from '../../features/categories/api/categoryApi';
import { getProducts } from '../../features/products/api/productApi';
import MegaMenu from './MegaMenu';

let cachedCategories = null;
let cachedProducts = null;

export default function CategoryNav({ mobileMenuOpen, closeMobileMenu }) {
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [activeCategory, setActiveCategory] = useState(null);
  const hoverTimeoutRef = useRef(null);

  useEffect(() => {
    let mounted = true;
    async function loadNavData() {
      try {
        if (!cachedCategories) cachedCategories = await getCategories();
        if (!cachedProducts) cachedProducts = await getProducts();
        
        if (mounted) {
          setCategories(cachedCategories || []);
          setProducts(cachedProducts || []);
        }
      } catch (error) {
        console.error("Failed to load nav data", error);
      }
    }
    loadNavData();
    return () => {
      mounted = false;
    };
  }, []);

  const handleMouseEnter = (category) => {
    clearTimeout(hoverTimeoutRef.current);
    hoverTimeoutRef.current = setTimeout(() => {
      setActiveCategory(category);
    }, 100);
  };

  const handleMouseLeave = () => {
    clearTimeout(hoverTimeoutRef.current);
    hoverTimeoutRef.current = setTimeout(() => {
      setActiveCategory(null);
    }, 200);
  };

  const closeMegaMenu = () => {
    clearTimeout(hoverTimeoutRef.current);
    setActiveCategory(null);
    if (closeMobileMenu) {
      closeMobileMenu();
    }
  };

  // Derive active category brands from actual products
  const activeCategoryBrands = useMemo(() => {
    if (!activeCategory || !activeCategory.id || !products || products.length === 0) {
      return [];
    }

    // Filter products matching the category
    const categoryProducts = products.filter(
      (product) => product.category && product.category.id === activeCategory.id
    );

    // Extract unique brands
    const brandMap = new Map();
    for (const product of categoryProducts) {
      if (product.brand && product.brand.id && product.brand.name) {
        brandMap.set(product.brand.id, {
          id: product.brand.id,
          name: product.brand.name
        });
      }
    }
    
    return Array.from(brandMap.values());
  }, [activeCategory, products]);

  // Mobile rendering (Accordion style)
  if (mobileMenuOpen !== undefined) {
    if (!categories || categories.length === 0) return null;

    return (
      <div className="category-nav-mobile">
        <div className="category-nav-mobile__header">
          <span>Categories</span>
        </div>
        <ul className="category-nav-mobile__list">
          {categories.map((cat) => {
            if (!cat || !cat.id) return null;
            return (
              <li key={cat.id} className="category-nav-mobile__item">
                <NavLink 
                  to={`/products?categoryId=${cat.id}`}
                  className={({ isActive }) => `category-nav-mobile__link ${isActive ? 'category-nav-mobile__link--active' : ''}`}
                  onClick={closeMegaMenu}
                >
                  {cat.name}
                </NavLink>
              </li>
            );
          })}
          <li className="category-nav-mobile__item">
            <NavLink 
              to="/products"
              className={({ isActive }) => `category-nav-mobile__link ${isActive ? 'category-nav-mobile__link--active' : ''}`}
              onClick={closeMegaMenu}
            >
              All Products
            </NavLink>
          </li>
        </ul>
      </div>
    );
  }

  // Desktop rendering
  if (!categories || categories.length === 0) return null;

  return (
    <div className="category-nav-strip" onMouseLeave={handleMouseLeave}>
      <div className="category-nav-strip__inner">
        <ul className="category-nav-list">
          {categories.map((cat) => {
            if (!cat || !cat.id) return null;
            return (
              <li 
                key={cat.id} 
                className="category-nav-item"
                onMouseEnter={() => handleMouseEnter(cat)}
              >
                <NavLink
                  to={`/products?categoryId=${cat.id}`}
                  className={({ isActive }) => 
                    `category-nav-link ${isActive || activeCategory?.id === cat.id ? 'category-nav-link--active' : ''}`
                  }
                  onClick={closeMegaMenu}
                >
                  {cat.name}
                </NavLink>
              </li>
            );
          })}
        </ul>
      </div>
      
      {activeCategory && (
        <div 
          className="mega-menu-wrapper"
          onMouseEnter={() => {
            clearTimeout(hoverTimeoutRef.current);
          }}
        >
          <MegaMenu 
            category={activeCategory} 
            brands={activeCategoryBrands} 
            onClose={closeMegaMenu} 
          />
        </div>
      )}
    </div>
  );
}
