const fs = require('fs');

async function run() {
  const catalog = await fetch('http://localhost:8080/api/v1/products').then(res => res.json());
  
  const currentProduct = catalog.find(p => p.id === 'dfeb998b-79e1-4363-9846-e12ffa395c02'); // iPhone 15
  
  const candidates = catalog.filter(p => 
    String(p.id) !== String(currentProduct.id) && 
    p.active !== false
  );
  
  const scored = candidates.map(p => {
    let score = 0;
    
    if (p.category?.id && currentProduct.category?.id && p.category.id === currentProduct.category.id) score += 50;
    if (p.brand?.id && currentProduct.brand?.id && p.brand.id === currentProduct.brand.id) score += 25;
    
    if (p.price && currentProduct.price) {
      const diffRatio = Math.abs(p.price - currentProduct.price) / currentProduct.price;
      if (diffRatio <= 0.2) score += 15;
      else if (diffRatio <= 0.5) score += 5;
    }
    
    if (p.name && currentProduct.name) {
      const currentNameWords = currentProduct.name.toLowerCase().split(/\s+/);
      const targetNameWords = p.name.toLowerCase().split(/\s+/);
      const commonWords = currentNameWords.filter(w => w.length > 2 && targetNameWords.includes(w));
      if (commonWords.length > 0) score += 10;
    }
    
    if (p.rating && currentProduct.rating && Math.abs(p.rating - currentProduct.rating) <= 0.5) score += 5;
    
    return { name: p.name, score };
  });
  
  const valid = scored.filter(item => item.score >= 10);
  valid.sort((a, b) => b.score - a.score);
  
  console.log("iPhone 15 recommendations:");
  console.log(valid.slice(0, 6));

  const macbook = catalog.find(p => p.id === 'ca9e867f-d6a4-47b2-ac4b-d9bc60837eba'); // MacBook Air M3
  const scoredMacbook = catalog.filter(p => p.id !== macbook.id && p.active !== false).map(p => {
    let score = 0;
    if (p.category?.id === macbook.category?.id) score += 50;
    if (p.brand?.id === macbook.brand?.id) score += 25;
    if (p.rating && macbook.rating && Math.abs(p.rating - macbook.rating) <= 0.5) score += 5;
    return { name: p.name, score };
  }).filter(item => item.score >= 10).sort((a, b) => b.score - a.score);

  console.log("\nMacBook Air recommendations:");
  console.log(scoredMacbook.slice(0, 6));
}

run();
