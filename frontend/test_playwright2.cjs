const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({
    executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    headless: true
  });
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
    
    // Check exclusions, layout, and count
    const sectionVisible = await page.$('.similar-products') !== null;
    if (!sectionVisible) {
       console.log(`Product ID: ${id} - Similar Products NOT FOUND!`);
       continue;
    }

    const similarProducts = await page.$$eval('.similar-products .product-card', cards => {
       return cards.map(c => c.querySelector('a')?.innerText || c.innerText.split('\n')[0]);
    });

    // Check for exact current product in the list
    const currentName = await page.$eval('.product-extended-info .product-info-section.product-description p', p => p.innerText).catch(()=>'');
    const hasDuplicate = similarProducts.some(name => currentName.includes(name));

    console.log(`Product ID: ${id}`);
    console.log(`Similar Products (${similarProducts.length}):`, similarProducts);
    console.log(`Has Duplicate of current? ${hasDuplicate}`);
    console.log('---------------------------');
  }
  
  await browser.close();
})();
