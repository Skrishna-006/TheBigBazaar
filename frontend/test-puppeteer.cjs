const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
  console.log('Starting Puppeteer verification...');
  
  // Test 1: Granted Geolocation
  let browser = await puppeteer.launch({ headless: true });
  let context = browser.defaultBrowserContext();
  await context.overridePermissions('http://localhost:5173', ['geolocation']);
  
  let page = await browser.newPage();
  await page.setGeolocation({ latitude: 20.5937, longitude: 78.9629 }); // India
  
  console.log('Test 1: Registration with location permission ALLOWED');
  await page.goto('http://localhost:5173/register');
  
  // Wait for the country code to populate
  await page.waitForSelector('.dial-code');
  await new Promise(r => setTimeout(r, 2000));
  
  let dialCodeText = await page.evaluate(() => document.querySelector('.dial-code').textContent);
  console.log('Detected Dial Code:', dialCodeText);
  assert(dialCodeText === '+91', 'Did not detect India correctly.');

  // Test 2: Denied Geolocation
  await browser.close();
  browser = await puppeteer.launch({ headless: true });
  context = browser.defaultBrowserContext();
  // Deny permission explicitly
  await context.overridePermissions('http://localhost:5173', []);
  
  page = await browser.newPage();
  console.log('Test 2: Registration with location permission DENIED');
  await page.goto('http://localhost:5173/register');
  await page.waitForSelector('.dial-code');
  await new Promise(r => setTimeout(r, 2000));
  
  let deniedDialCode = await page.evaluate(() => document.querySelector('.dial-code').textContent);
  console.log('Denied Dial Code:', deniedDialCode);
  assert(deniedDialCode === 'Select' || deniedDialCode.includes('Select'), 'Fallback was not "Select".');

  // Test 3: Manual country selection
  console.log('Test 3: Manual country selection');
  await page.click('.country-selector-btn');
  // Wait for dropdown
  await page.waitForSelector('.country-dropdown');
  // Click United States
  await page.evaluate(() => {
    const options = Array.from(document.querySelectorAll('.country-option'));
    const usOption = options.find(el => el.textContent.includes('United States'));
    usOption.click();
  });
  
  let selectedDialCode = await page.evaluate(() => document.querySelector('.dial-code').textContent);
  console.log('Selected Dial Code:', selectedDialCode);
  assert(selectedDialCode === '+1', 'Did not manually select USA.');

  // Test 4: Cached on Login
  console.log('Test 4: Login with manually selected country');
  await page.goto('http://localhost:5173/login');
  await page.waitForSelector('.dial-code');
  let loginDialCode = await page.evaluate(() => document.querySelector('.dial-code').textContent);
  console.log('Login Dial Code (should be cached +1):', loginDialCode);
  assert(loginDialCode === '+1', 'Cached manual selection failed on Login.');

  // Test 5: Cached on Forgot Password
  console.log('Test 5: Forgot Password with manually selected country');
  await page.goto('http://localhost:5173/forgot-password');
  await page.waitForSelector('.dial-code');
  let resetDialCode = await page.evaluate(() => document.querySelector('.dial-code').textContent);
  console.log('Reset Dial Code (should be cached +1):', resetDialCode);
  assert(resetDialCode === '+1', 'Cached manual selection failed on Forgot Password.');

  // Test 6: Manual override after automatic detection
  await browser.close();
  browser = await puppeteer.launch({ headless: true });
  context = browser.defaultBrowserContext();
  await context.overridePermissions('http://localhost:5173', ['geolocation']);
  
  page = await browser.newPage();
  await page.setGeolocation({ latitude: 51.5074, longitude: -0.1278 }); // UK
  
  console.log('Test 6: Manual override after automatic detection');
  await page.goto('http://localhost:5173/login');
  await page.waitForSelector('.dial-code');
  await new Promise(r => setTimeout(r, 2000));
  
  let ukDialCode = await page.evaluate(() => document.querySelector('.dial-code').textContent);
  console.log('Detected Dial Code (should be UK +44):', ukDialCode);
  assert(ukDialCode === '+44', 'UK detection failed.');
  
  await page.click('.country-selector-btn');
  await page.waitForSelector('.country-dropdown');
  await page.evaluate(() => {
    const options = Array.from(document.querySelectorAll('.country-option'));
    const auOption = options.find(el => el.textContent.includes('Australia'));
    auOption.click();
  });
  
  let auDialCode = await page.evaluate(() => document.querySelector('.dial-code').textContent);
  console.log('Overridden Dial Code (should be AU +61):', auDialCode);
  assert(auDialCode === '+61', 'Override failed.');

  await browser.close();
  console.log('All Browser UI Tests Passed!');
})();
