const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({ headless: 'new' });
  const page = await browser.newPage();
  
  // Set viewport to desktop
  await page.setViewport({ width: 1280, height: 800 });
  
  await page.goto('http://localhost:5173/products/dfeb998b-79e1-4363-9846-e12ffa395c02', { waitUntil: 'networkidle2' });
  
  // Check the DOM structure in .product-extended-info
  const content = await page.evaluate(() => {
    const infoSections = Array.from(document.querySelectorAll('.product-extended-info .product-info-section'));
    
    return infoSections.map(section => {
      const title = section.querySelector('.section-title')?.innerText || 'No Title';
      let data = [];
      
      if (title === 'Description') {
         data.push(section.querySelector('p')?.innerText);
      } else if (title === 'Product Highlights') {
         data = Array.from(section.querySelectorAll('li')).map(li => li.innerText);
      } else if (title === 'Specifications') {
         const groups = Array.from(section.querySelectorAll('.spec-group'));
         if (groups.length > 0) {
            data = groups.map(g => {
               const gTitle = g.querySelector('h3')?.innerText;
               const rows = Array.from(g.querySelectorAll('.spec-table-row')).map(row => {
                  return {
                     key: row.querySelector('.spec-name')?.innerText,
                     value: row.querySelector('.spec-value')?.innerText
                  };
               });
               return { group: gTitle, rows };
            });
         }
      }
      return { title, data };
    });
  });
  
  console.log(JSON.stringify(content, null, 2));
  
  // Test mobile layout
  await page.setViewport({ width: 375, height: 667 });
  await new Promise(resolve => setTimeout(resolve, 500)); // wait for layout shift
  
  const mobileDisplay = await page.evaluate(() => {
     const row = document.querySelector('.spec-table-row');
     if (row) {
        const style = window.getComputedStyle(row);
        return { display: style.display, gridTemplateColumns: style.gridTemplateColumns };
     }
     return null;
  });
  
  console.log('Mobile Layout:', mobileDisplay);

  await browser.close();
})();
