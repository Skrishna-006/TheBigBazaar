const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({
    executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    headless: true
  });
  const page = await browser.newPage();
  
  // Track API calls
  let apiCallCount = 0;
  page.on('request', request => {
    if (request.url().includes('/api/v1/products') && request.method() === 'GET') {
      apiCallCount++;
    }
  });

  await page.setViewportSize({ width: 1280, height: 800 });
  
  const productIds = [
    'dfeb998b-79e1-4363-9846-e12ffa395c02', // iPhone 15
    'ca9e867f-d6a4-47b2-ac4b-d9bc60837eba', // MacBook Air M3
    '2b80dffc-eafb-49f0-9efa-27afad426b14', // MX Master 3S
    '52141b49-7293-40e6-abf3-f9992327abf5', // WH-1000XM5
    'b3bc99e9-c26c-4ff1-8489-7e24d4f89cb2'  // PlayStation 4
  ];

  for (const id of productIds) {
    console.log(`\n==========================================`);
    console.log(`Testing Product ID: ${id}`);
    await page.goto(`http://localhost:5174/products/${id}`);
    await page.waitForSelector('.similar-products', { timeout: 30000 }).catch(() => {});
    await page.waitForTimeout(500); // give it a moment to render
    
    const sectionVisible = await page.$('.similar-products') !== null;
    if (!sectionVisible) {
       console.log(`Similar Products NOT FOUND!`);
       continue;
    }

    const currentName = await page.$eval('.product-extended-info .product-info-section.product-description p', p => p.innerText).catch(()=>'');
    const header = await page.$eval('.similar-products h2', h2 => h2.innerText).catch(()=>'');
    console.log(`Current Product Description Name Match (or null): ${currentName}`);
    console.log(`Section Header: ${header}`);

    const similarProducts = await page.$$eval('.similar-products .product-card', cards => {
       return cards.map(c => {
         const name = c.querySelector('h3 a')?.innerText || c.querySelector('h3')?.innerText || 'Unknown';
         const link = c.querySelector('a')?.getAttribute('href') || 'No link';
         return { name, link };
       });
    });

    const hasDuplicate = similarProducts.some(p => currentName.includes(p.name) || p.link.includes(id));

    console.log(`Similar Products rendered count: ${similarProducts.length}`);
    similarProducts.forEach((p, i) => console.log(`  ${i+1}. ${p.name} (${p.link})`));
    console.log(`Has Duplicate of current? ${hasDuplicate}`);
  }

  console.log(`\n--- CLICK TEST ---`);
  apiCallCount = 0; // reset
  await page.goto(`http://localhost:5174/products/dfeb998b-79e1-4363-9846-e12ffa395c02`);
  await page.waitForSelector('.similar-products .product-card a');
  
  const initialUrl = page.url();
  console.log(`Initial URL: ${initialUrl}`);
  
  // Click first similar product
  await page.click('.similar-products .product-card a');
  await page.waitForTimeout(1000); // Wait for navigation and react render
  
  const newUrl = page.url();
  console.log(`New URL after click: ${newUrl}`);
  console.log(`URL changed successfully: ${initialUrl !== newUrl}`);
  
  const newCurrentName = await page.$eval('.product-extended-info .product-info-section.product-description p', p => p.innerText).catch(()=>'');
  console.log(`New Product Name/Desc loaded: ${newCurrentName !== ''}`);
  
  // Go back
  await page.goBack();
  await page.waitForTimeout(500);
  console.log(`URL after goBack: ${page.url()}`);
  
  console.log(`\n--- API REQUEST TEST ---`);
  console.log(`Total /api/v1/products GET requests intercepted during click test sequence: ${apiCallCount}`);
  
  await browser.close();
})();
