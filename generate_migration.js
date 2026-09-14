const fs = require('fs');

const products = [
  { id: '01320a9a-55f8-48e4-bab3-40d6b64e68f4', name: 'Apple MacBook Pro M3', sku: 'APP-MBP3-034' },
  { id: '0208b297-03a4-4b46-a7c7-e82ee0888da5', name: 'Samsung Galaxy Tab', sku: 'SAM-TAB-128-GRY' },
  { id: '1bb368d1-f70f-4b16-81ab-a11d862619d6', name: 'Sony PlayStation 2', sku: 'SON-PS2-040' },
  { id: '22b08a25-3088-4d92-864d-4def26cb8496', name: 'Lenovo ThinkPad X1 Carbon', sku: 'LEN-X1C-025' },
  { id: '267b51f7-7d7d-4b9b-8d29-b139f9f9bcc0', name: 'Apple iPad Air', sku: 'APP-IPADA-033' },
  { id: '2b80dffc-eafb-49f0-9efa-27afad426b14', name: 'Logitech MX Master 3S', sku: 'LOG-MX3S-GRAPH' },
  { id: '40782150-e765-4437-b15b-a6b25a426efa', name: 'Apple MacBook Air M2', sku: 'APP-MBA2-035' },
  { id: '474a78e4-34a0-497b-8b47-d4a95e7f04df', name: 'Sony Portable Bluetooth Speaker', sku: 'SONY-BT-SPK-BLK' },
  { id: '4d1dd8d0-871d-4b9e-8e86-ad927f8c7d84', name: 'Samsung Galaxy A35 5G Midnight', sku: 'SAM-A35M-024' },
  { id: '5036e462-a7af-4cf9-8742-1b31fe7bc3d9', name: 'Samsung Galaxy S10', sku: 'SAM-S10-022' },
  { id: '52141b49-7293-40e6-abf3-f9992327abf5', name: 'Sony WH-1000XM5', sku: 'SONY-WH1000XM5-BLK' },
  { id: '5249a4bc-8c3e-44fe-a032-8b2b5c084c7d', name: 'Logitech Mechanical Keyboard', sku: 'LOG-MECH-KB-BLK' },
  { id: '6efa253f-7d36-40cb-8ad1-6c883a92ad6d', name: 'Lenovo Legion Y7000P', sku: 'LEN-LEGY7-027' },
  { id: '72154c0c-744f-45d0-9f16-7efae2830c62', name: 'Lenovo ThinkPad E14', sku: 'LEN-E14-I5-16' },
  { id: '7f1efb3d-07ef-4265-a05c-a2b6281fb922', name: 'Samsung Galaxy S24', sku: 'SAM-S24-128-BLK' },
  { id: '84f20a2c-41a6-416d-a6ff-bcc68fcdd83f', name: 'Sony WF-1000XM4', sku: 'SON-WF4-036' },
  { id: '8b549fd9-19f2-42cc-aaff-2a68761a4210', name: 'Logitech K120 Keyboard', sku: 'LOG-K120-031' },
  { id: '8d248882-7f5d-4c93-a0b5-d374d2b61631', name: 'Samsung Galaxy A35 5G', sku: 'SAM-A35-021' },
  { id: '9702980a-0842-4c58-b58c-ed4337d65ce8', name: 'Logitech G305 Wireless Mouse', sku: 'LOG-G305-030' },
  { id: 'a8ee0cc8-ee17-4b37-8e43-d15ae608d609', name: 'Sony PlayStation 3', sku: 'SON-PS3-039' },
  { id: 'af13a232-0916-456f-a53b-2f06bb75ba7a', name: 'Samsung Galaxy Buds2 Pro', sku: 'SAM-BUDS2PRO-BLK' },
  { id: 'b3bc99e9-c26c-4ff1-8489-7e24d4f89cb2', name: 'Sony PlayStation 4', sku: 'SON-PS4-038' },
  { id: 'c1aca085-59a8-45d2-b220-72f9d63146e2', name: 'Lenovo Yoga 3 Pro', sku: 'LEN-YOGA3-028' },
  { id: 'ca9e867f-d6a4-47b2-ac4b-d9bc60837eba', name: 'Apple MacBook Air M3', sku: 'APP-MBA-M3-256' },
  { id: 'd405c95f-bbb3-4ba3-98cd-e5f4cdd0367c', name: 'Sony DualSense Wireless Controller', sku: 'SON-DS5-037' },
  { id: 'dfeb998b-79e1-4363-9846-e12ffa395c02', name: 'Apple iPhone 15', sku: 'APP-IP15-128-BLU' },
  { id: 'e00b2a75-69d5-4603-80b7-60663cf93960', name: 'Samsung Galaxy Note 10', sku: 'SAM-N10-023' },
  { id: 'e03da354-8ae6-46e3-89ac-e09194924208', name: 'Lenovo ThinkPad T14', sku: 'LEN-T14-026' },
  { id: 'e617e75e-f0d7-4f36-ae46-6f0b08d1bce5', name: 'Logitech C920 HD Webcam', sku: 'LOG-C920-032' },
  { id: 'f64fd75e-f89d-4838-8620-ce72779fb438', name: 'Logitech G502 Gaming Mouse', sku: 'LOG-G502-029' }
];

let sql = `
ALTER TABLE products ADD COLUMN IF NOT EXISTS highlights JSONB;
ALTER TABLE products ADD COLUMN IF NOT EXISTS specifications JSONB;

`;

function getHighlights(name) {
    name = name.toLowerCase();
    let highlights = [];
    if (name.includes("iphone 15")) {
        highlights = ["Dynamic Island alerts and Live Activities", "New 48MP Main camera for super-high-resolution photos", "USB-C connectivity"];
    } else if (name.includes("macbook air m3") || name.includes("macbook pro m3")) {
        highlights = ["Supercharged by M3 chip", "Up to 18 hours of battery life", "Liquid Retina display with 1 billion colors"];
    } else if (name.includes("thinkpad")) {
        highlights = ["Mil-Spec tested for durability", "Ergonomic legendary keyboard", "Rapid charge (80% in 1 hr)"];
    } else if (name.includes("keyboard") && name.includes("mechanical")) {
        highlights = ["Tactile quiet mechanical switches", "Customizable backlighting", "Multi-device connectivity"];
    } else if (name.includes("mx master 3s")) {
        highlights = ["8K DPI any-surface tracking", "Quiet Clicks - 90% less noise", "MagSpeed scrolling"];
    } else if (name.includes("buds2 pro")) {
        highlights = ["Active Noise Canceling", "24-bit Hi-Fi audio", "Comfortable ergonomic fit"];
    } else if (name.includes("galaxy s24")) {
        highlights = ["Galaxy AI enabled", "Armor Aluminum frame", "Nightography camera setup"];
    } else if (name.includes("galaxy tab")) {
        highlights = ["S Pen included in box", "120Hz vivid display", "DeX multitasking mode"];
    } else if (name.includes("portable bluetooth speaker")) {
        highlights = ["IP67 water and dust resistant", "Extra Bass technology", "24 hour battery life"];
    } else if (name.includes("wh-1000xm5")) {
        highlights = ["Industry-leading noise cancellation", "Auto NC Optimizer", "30-hour battery life with quick charge"];
    } else {
        highlights = ["Authentic Quality", "1 Year Manufacturer Warranty", "7-Day Replacement Policy"];
    }
    return highlights;
}

function extractStorageFromSku(sku) {
    const match = sku.match(/-(\d+)-/);
    if (match) {
        return match[1] + "GB";
    }
    // Try trailing number if separated by hyphen, e.g. -256
    const matchEnd = sku.match(/-(\d+)$/);
    if (matchEnd && matchEnd[1].length >= 3) {
        return matchEnd[1] + "GB";
    }
    return null;
}

function extractRamFromSku(sku) {
    if (sku.includes("-16")) return "16GB";
    if (sku.includes("-32")) return "32GB";
    if (sku.includes("-8")) return "8GB";
    return null;
}

function getSpecs(name, sku) {
    const lName = name.toLowerCase();
    let specs = {
        "Model Name": name
    };
    
    // Brand
    if (lName.includes("apple")) specs["Brand"] = "Apple";
    else if (lName.includes("samsung")) specs["Brand"] = "Samsung";
    else if (lName.includes("sony")) specs["Brand"] = "Sony";
    else if (lName.includes("lenovo")) specs["Brand"] = "Lenovo";
    else if (lName.includes("logitech")) specs["Brand"] = "Logitech";
    
    let storage = extractStorageFromSku(sku);
    let ram = extractRamFromSku(sku);
    
    if (lName.includes("iphone") || lName.includes("galaxy s") || lName.includes("galaxy a") || lName.includes("galaxy note") || lName.includes("galaxy tab") || lName.includes("ipad")) {
        specs["Storage"] = storage || "128GB";
        specs["Cellular Technology"] = (lName.includes("5g") || lName.includes("s24") || lName.includes("iphone 15")) ? "5G" : "4G/LTE";
        if (lName.includes("iphone") || lName.includes("ipad")) specs["Operating System"] = lName.includes("ipad") ? "iPadOS" : "iOS";
        else specs["Operating System"] = "Android";
    } else if (lName.includes("macbook") || lName.includes("thinkpad") || lName.includes("yoga") || lName.includes("legion")) {
        specs["RAM"] = ram || "16GB";
        specs["Storage"] = (storage || "512GB") + " SSD";
        if (lName.includes("14")) specs["Display"] = "14-inch";
        else if (lName.includes("16")) specs["Display"] = "16-inch";
        else specs["Display"] = "13.3-inch";
        
        if (lName.includes("macbook")) specs["Operating System"] = "macOS";
        else specs["Operating System"] = "Windows 11";
    } else if (lName.includes("keyboard") || lName.includes("mouse") || lName.includes("webcam") || lName.includes("mx master")) {
        specs["Connectivity"] = lName.includes("wireless") || lName.includes("mx master") || lName.includes("bluetooth") ? "Bluetooth / USB Receiver" : "Wired USB";
    } else if (lName.includes("audio") || lName.includes("buds") || lName.includes("speaker") || lName.includes("wh-") || lName.includes("wf-")) {
        specs["Connectivity"] = "Bluetooth 5.3";
    } else if (lName.includes("playstation")) {
        specs["Storage"] = storage || "1TB";
        specs["Resolution"] = lName.includes("4") ? "1080p / 4K" : "480p";
    } else {
        specs["Color"] = "Standard Edition";
    }
    
    return specs;
}

for (const p of products) {
    const h = getHighlights(p.name);
    const s = getSpecs(p.name, p.sku);
    
    // Use jsonb literals and escape single quotes just in case
    const jsonH = JSON.stringify(h).replace(/'/g, "''");
    const jsonS = JSON.stringify(s).replace(/'/g, "''");
    
    sql += `UPDATE products SET highlights = '${jsonH}'::jsonb, specifications = '${jsonS}'::jsonb WHERE id = '${p.id}';\n`;
}

fs.writeFileSync('migration.sql', sql);
console.log('migration.sql generated.');
