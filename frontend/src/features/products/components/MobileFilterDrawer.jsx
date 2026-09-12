import React, { useEffect } from 'react';
import FilterSidebar from './FilterSidebar';

export default function MobileFilterDrawer({ isOpen, onClose, ...sidebarProps }) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && isOpen) onClose();
    };
    window.addEventListener('keydown', handleEscape);
    return () => window.removeEventListener('keydown', handleEscape);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="mobile-drawer-overlay" onClick={onClose}>
      <div 
        className="mobile-drawer-content" 
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Filter Options"
      >
        <div className="mobile-drawer-header">
          <h2>Filters</h2>
          <button onClick={onClose} className="mobile-drawer-close" aria-label="Close filters">
            &times;
          </button>
        </div>
        <div className="mobile-drawer-body">
          <FilterSidebar {...sidebarProps} />
        </div>
        <div className="mobile-drawer-footer">
          <button className="button button--secondary" onClick={sidebarProps.onClearAll}>Clear All</button>
          <button className="button button--primary" onClick={onClose}>Apply Filters</button>
        </div>
      </div>
    </div>
  );
}
