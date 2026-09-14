const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  // Set viewport to desktop
  await page.setViewportSize({ width: 1280, height: 800 });
  
  const productIds = [
    'dfeb998b-79e1-4363-9846-e12ffa395c02', // iPhone 15
    'ca9e867f-d6a4-47b2-ac4b-d9bc60837eba', // MacBook Air M3
    '2b80dffc-eafb-49f0-9efa-27afad426b14', // MX Master 3S
    '52141b49-7293-40e6-abf3-f9992327abf5', // WH-1000XM5
    'b3bc99e9-c26c-4ff1-8489-7e24d4f89cb2'  // PlayStation 4
  ];

  for (const id of productIds) {
    await page.goto(`http://localhost:5173/products/${id}`);
    await page.waitForSelector('.similar-products', { timeout: 10000 }).catch(() => {});
    
    const similarProducts = await page.$$eval('.similar-products .product-card', cards => {
       return cards.map(c => c.querySelector('.product-name')?.innerText);
    });

    console.log(`Product ID: ${id}`);
    console.log(`Similar Products (${similarProducts.length}):`, similarProducts);
  }
  
  await browser.close();
})();
