import { useState, useRef, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getProducts } from '../../features/products/api/productApi';

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

let cachedProducts = null;

export default function SearchBar() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get('search') || '');
  const [isFocused, setIsFocused] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  
  const [products, setProducts] = useState(cachedProducts || []);

  const [selectedIndex, setSelectedIndex] = useState(-1);
  const containerRef = useRef(null);

  useEffect(() => {
    setQuery(searchParams.get('search') || '');
  }, [searchParams]);

  const loadData = async () => {
    if (cachedProducts) return;
    setIsLoading(true);
    try {
      const p = await getProducts();
      cachedProducts = p;
      setProducts(p);
    } catch (error) {
      console.error('Failed to load search data', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleFocus = () => {
    setIsFocused(true);
    loadData();
  };

  const handleClickOutside = (e) => {
    if (containerRef.current && !containerRef.current.contains(e.target)) {
      setIsFocused(false);
    }
  };

  useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const trimmedQuery = query.trim().toLowerCase();

  const suggestions = useMemo(() => {
    if (!trimmedQuery) return [];
    
    // Derived unique categories and brands from products
    const uniqueCategories = new Map();
    const uniqueBrands = new Map();
    
    products.forEach(p => {
      if (p.category) uniqueCategories.set(p.category.id, p.category);
      if (p.brand) uniqueBrands.set(p.brand.id, p.brand);
    });
    
    const matchedCategories = Array.from(uniqueCategories.values())
      .filter(c => c.name.toLowerCase().includes(trimmedQuery))
      .slice(0, 3)
      .map(c => ({ type: 'category', item: c }));

    const matchedBrands = Array.from(uniqueBrands.values())
      .filter(b => b.name.toLowerCase().includes(trimmedQuery))
      .slice(0, 3)
      .map(b => ({ type: 'brand', item: b }));

    // Ranking products
    const matchedProducts = products
      .filter(p => {
        return (
          p.name.toLowerCase().includes(trimmedQuery) ||
          p.brand?.name?.toLowerCase().includes(trimmedQuery) ||
          p.category?.name?.toLowerCase().includes(trimmedQuery) ||
          p.sku?.toLowerCase().includes(trimmedQuery)
        );
      })
      .sort((a, b) => {
        const aName = a.name.toLowerCase();
        const bName = b.name.toLowerCase();
        
        // Exact match
        if (aName === trimmedQuery && bName !== trimmedQuery) return -1;
        if (bName === trimmedQuery && aName !== trimmedQuery) return 1;
        
        // Starts with
        if (aName.startsWith(trimmedQuery) && !bName.startsWith(trimmedQuery)) return -1;
        if (bName.startsWith(trimmedQuery) && !aName.startsWith(trimmedQuery)) return 1;
        
        // Brand match
        const aBrandMatch = a.brand?.name?.toLowerCase() === trimmedQuery;
        const bBrandMatch = b.brand?.name?.toLowerCase() === trimmedQuery;
        if (aBrandMatch && !bBrandMatch) return -1;
        if (bBrandMatch && !aBrandMatch) return 1;
        
        // Category match
        const aCatMatch = a.category?.name?.toLowerCase() === trimmedQuery;
        const bCatMatch = b.category?.name?.toLowerCase() === trimmedQuery;
        if (aCatMatch && !bCatMatch) return -1;
        if (bCatMatch && !aCatMatch) return 1;
        
        return 0;
      })
      .slice(0, 5)
      .map(p => ({ type: 'product', item: p }));

    // Mix them: products first, then categories, then brands
    return [...matchedProducts, ...matchedCategories, ...matchedBrands];
  }, [trimmedQuery, products]);

  useEffect(() => {
    setSelectedIndex(-1);
  }, [trimmedQuery, suggestions.length]);

  const handleKeyDown = (e) => {
    if (!isFocused || !trimmedQuery) return;
    
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex(prev => (prev < suggestions.length ? prev + 1 : 0));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex(prev => (prev > -1 ? prev - 1 : suggestions.length)); // suggestions.length means "View all"
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (selectedIndex === -1 || selectedIndex === suggestions.length) {
        submitSearch(query);
      } else {
        const selected = suggestions[selectedIndex];
        handleSuggestionClick(selected);
      }
    } else if (e.key === 'Escape') {
      e.preventDefault();
      setIsFocused(false);
    }
  };

  const getPreservedDealsParam = () => {
    return searchParams.get('deals') === 'true' ? 'deals=true&' : '';
  };

  const submitSearch = (q) => {
    const term = q.trim();
    setIsFocused(false);
    if (term) {
      const dealsParam = getPreservedDealsParam();
      navigate(`/products?${dealsParam}search=${encodeURIComponent(term)}`);
    } else {
      navigate(`/products${searchParams.get('deals') === 'true' ? '?deals=true' : ''}`);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    submitSearch(query);
  };

  const handleSuggestionClick = (suggestion) => {
    setIsFocused(false);
    const dealsParam = getPreservedDealsParam();
    if (suggestion.type === 'product') {
      navigate(`/products/${suggestion.item.id}`);
    } else if (suggestion.type === 'category') {
      navigate(`/products?${dealsParam}categoryId=${suggestion.item.id}`);
    } else if (suggestion.type === 'brand') {
      navigate(`/products?${dealsParam}brandId=${suggestion.item.id}`);
    }
  };

  const clearSearch = () => {
    setQuery('');
    setSelectedIndex(-1);
    // keep focus
  };

  const showDropdown = isFocused && trimmedQuery.length > 0;

  return (
    <div className="site-search-container" ref={containerRef}>
      <form className="site-search-form" onSubmit={handleSubmit}>
        <button type="submit" className="site-search-btn" aria-label="Submit search">
          <SearchIcon />
        </button>
        <input
          type="text"
          className="site-search-input"
          placeholder="Search for products, brands and more..."
          aria-label="Search input"
          role="combobox"
          aria-expanded={showDropdown}
          aria-autocomplete="list"
          aria-controls="search-suggestions"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={handleFocus}
          onKeyDown={handleKeyDown}
        />
        {query && (
          <button type="button" className="site-search-clear" onClick={clearSearch} aria-label="Clear search">
            <XIcon />
          </button>
        )}
      </form>

      {showDropdown && (
        <div 
          id="search-suggestions"
          className="search-dropdown" 
          role="listbox"
        >
          {isLoading && suggestions.length === 0 ? (
            <div className="search-dropdown__loading">Searching...</div>
          ) : suggestions.length === 0 ? (
            <div className="search-dropdown__empty">
              No matching products found
              <div className="search-dropdown__empty-hint">Press Enter to search for "{query}"</div>
            </div>
          ) : (
            <div className="search-dropdown__list">
              {suggestions.map((s, idx) => {
                const isSelected = selectedIndex === idx;
                
                if (s.type === 'product') {
                  const product = s.item;
                  return (
                    <div 
                      key={`product-${product.id}`}
                      role="option"
                      aria-selected={isSelected}
                      className={`search-dropdown__item search-dropdown__item--product ${isSelected ? 'selected' : ''}`}
                      onClick={() => handleSuggestionClick(s)}
                      onMouseEnter={() => setSelectedIndex(idx)}
                    >
                      <div className="search-dropdown__item-img">
                        {product.imageUrl ? (
                          <img src={product.imageUrl} alt="" />
                        ) : (
                          <div className="search-dropdown__img-placeholder"></div>
                        )}
                      </div>
                      <div className="search-dropdown__item-text">
                        <div className="search-dropdown__item-title">{product.name}</div>
                        <div className="search-dropdown__item-subtitle">{product.brand?.name || product.category?.name}</div>
                      </div>
                    </div>
                  );
                } else if (s.type === 'category') {
                  const category = s.item;
                  return (
                    <div
                      key={`category-${category.id}`}
                      role="option"
                      aria-selected={isSelected}
                      className={`search-dropdown__item search-dropdown__item--text ${isSelected ? 'selected' : ''}`}
                      onClick={() => handleSuggestionClick(s)}
                      onMouseEnter={() => setSelectedIndex(idx)}
                    >
                      <div className="search-dropdown__item-icon"><SearchIcon /></div>
                      <div className="search-dropdown__item-text">
                        <div className="search-dropdown__item-title">{category.name}</div>
                        <div className="search-dropdown__item-subtitle">in Categories</div>
                      </div>
                    </div>
                  );
                } else if (s.type === 'brand') {
                  const brand = s.item;
                  return (
                    <div
                      key={`brand-${brand.id}`}
                      role="option"
                      aria-selected={isSelected}
                      className={`search-dropdown__item search-dropdown__item--text ${isSelected ? 'selected' : ''}`}
                      onClick={() => handleSuggestionClick(s)}
                      onMouseEnter={() => setSelectedIndex(idx)}
                    >
                      <div className="search-dropdown__item-icon"><SearchIcon /></div>
                      <div className="search-dropdown__item-text">
                        <div className="search-dropdown__item-title">{brand.name}</div>
                        <div className="search-dropdown__item-subtitle">in Brands</div>
                      </div>
                    </div>
                  );
                }
                return null;
              })}
              <div 
                role="option"
                aria-selected={selectedIndex === suggestions.length}
                className={`search-dropdown__view-all ${selectedIndex === suggestions.length ? 'selected' : ''}`}
                onClick={() => submitSearch(query)}
                onMouseEnter={() => setSelectedIndex(suggestions.length)}
              >
                View all results &rarr;
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
