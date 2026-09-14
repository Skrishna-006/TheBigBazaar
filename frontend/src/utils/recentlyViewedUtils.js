const OLD_STORAGE_KEY = 'shopsphere_recently_viewed';
const STORAGE_KEY = 'thebigbazaar_recently_viewed';
const MAX_HISTORY = 8;
const EVENT_NAME = 'thebigbazaar:recently-viewed-updated';

function getSafeStorage() {
  try {
    // Migration logic
    if (localStorage.getItem(OLD_STORAGE_KEY) && !localStorage.getItem(STORAGE_KEY)) {
      localStorage.setItem(STORAGE_KEY, localStorage.getItem(OLD_STORAGE_KEY));
      localStorage.removeItem(OLD_STORAGE_KEY);
    }
    
    const data = localStorage.getItem(STORAGE_KEY);
    if (!data) return [];
    
    const parsed = JSON.parse(data);
    if (!Array.isArray(parsed)) return [];
    
    return parsed.filter(item => item && typeof item.productId === 'string');
  } catch (error) {
    console.warn('Recently Viewed localStorage error:', error);
    return [];
  }
}

export function getRecentlyViewedIds() {
  return getSafeStorage().map(item => item.productId);
}

export function addRecentlyViewed(productId) {
  if (!productId || typeof productId !== 'string') return;
  
  try {
    let history = getSafeStorage();
    
    // Remove existing entry to avoid duplicates
    history = history.filter(item => item.productId !== productId);
    
    // Prepend the new view
    history.unshift({
      productId,
      viewedAt: Date.now()
    });
    
    // Limit to max history
    history = history.slice(0, MAX_HISTORY);
    
    localStorage.setItem(STORAGE_KEY, JSON.stringify(history));
    
    // Dispatch custom event for cross-component sync
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event(EVENT_NAME));
    }
  } catch (error) {
    console.warn('Recently Viewed localStorage write error:', error);
  }
}

export function removeRecentlyViewed(productId) {
  if (!productId) return;
  
  try {
    let history = getSafeStorage();
    history = history.filter(item => item.productId !== productId);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(history));
    
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event(EVENT_NAME));
    }
  } catch (error) {
    console.warn('Recently Viewed localStorage write error:', error);
  }
}

export function clearRecentlyViewed() {
  try {
    localStorage.removeItem(STORAGE_KEY);
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event(EVENT_NAME));
    }
  } catch (error) {
    console.warn('Recently Viewed localStorage clear error:', error);
  }
}
