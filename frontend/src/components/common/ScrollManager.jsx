import { useEffect, useRef } from 'react';
import { useLocation, useNavigationType } from 'react-router-dom';

export default function ScrollManager() {
  const location = useLocation();
  const navigationType = useNavigationType();
  const prevLocation = useRef(location);

  // 1. Track and store scroll position for the Products page
  useEffect(() => {
    let timeoutId;
    const handleScroll = () => {
      if (location.pathname === '/products') {
        if (timeoutId) return;
        timeoutId = setTimeout(() => {
          const key = `shopsphere-products-scroll-${location.search}`;
          sessionStorage.setItem(key, window.scrollY.toString());
          timeoutId = null;
        }, 100);
      }
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => {
      window.removeEventListener('scroll', handleScroll);
      if (timeoutId) clearTimeout(timeoutId);
    };
  }, [location]);

  // 2. Handle scroll restoration on location changes
  useEffect(() => {
    const currentPath = location.pathname;
    const prevPath = prevLocation.current.pathname;

    const isProducts = currentPath === '/products';
    const isProductDetails = currentPath.startsWith('/products/') && currentPath !== '/products';
    const wasProductDetails = prevPath.startsWith('/products/') && prevPath !== '/products';

    const performScroll = (targetY) => {
      if (targetY === 0) {
        window.scrollTo({ top: 0, behavior: 'auto' });
        return;
      }

      let attempts = 0;
      const attemptScroll = () => {
        // Wait for asynchronous data loading (e.g. catalog grid) to populate the DOM.
        // We add a safety timeout (30 attempts * 50ms = 1.5s)
        if (document.documentElement.scrollHeight > targetY || attempts > 30) {
          window.scrollTo({ top: targetY, behavior: 'auto' });
        } else {
          attempts++;
          setTimeout(attemptScroll, 50);
        }
      };
      attemptScroll();
    };

    // If POP (Browser Back/Forward)
    if (navigationType === 'POP') {
      if (isProducts) {
        const key = `shopsphere-products-scroll-${location.search}`;
        const saved = sessionStorage.getItem(key);
        performScroll(saved ? parseInt(saved, 10) : 0);
      } else if (isProductDetails) {
        performScroll(0); // Optional: Could let browser handle POP for details, but explicit 0 is safer based on rules
      }
    } 
    // If PUSH or REPLACE
    else {
      if (isProducts) {
        if (wasProductDetails) {
          // Navbar click returning specifically from Product Details to Products
          const key = `shopsphere-products-scroll-${location.search}`;
          const saved = sessionStorage.getItem(key);
          performScroll(saved ? parseInt(saved, 10) : 0);
        } else {
          // Intentional navigation from Home, Cart, or fresh visit
          performScroll(0);
        }
      } else if (isProductDetails) {
        // Entering a product details page
        performScroll(0);
      } else {
        // Entering any other page
        performScroll(0);
      }
    }

    prevLocation.current = location;
  }, [location, navigationType]);

  return null;
}
