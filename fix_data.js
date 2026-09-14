const fs = require('fs');

const products = [
  { sku: 'APP-IPADA-033', highlights: ["Liquid Retina display", "M1 chip performance", "Touch ID built into top button"], specs: {"Brand":"Apple", "Model Name":"Apple iPad Air", "Operating System":"iPadOS", "Storage":"Not specified"} },
  { sku: 'APP-IP15-128-BLU', highlights: ["Dynamic Island alerts and Live Activities", "New 48MP Main camera for super-high-resolution photos", "USB-C connectivity"], specs: {"Brand":"Apple", "Model Name":"Apple iPhone 15", "Operating System":"iOS", "Storage":"128GB", "Cellular Technology":"5G"} },
  { sku: 'APP-MBA2-035', highlights: ["M2 chip", "Liquid Retina display", "1080p FaceTime HD camera"], specs: {"Brand":"Apple", "Model Name":"Apple MacBook Air M2", "Operating System":"macOS", "Display":"13.6-inch", "RAM":"Not specified", "Storage":"Not specified"} },
  { sku: 'APP-MBA-M3-256', highlights: ["Supercharged by M3 chip", "Up to 18 hours of battery life", "Liquid Retina display with 1 billion colors"], specs: {"Brand":"Apple", "Model Name":"Apple MacBook Air M3", "Operating System":"macOS", "Display":"13.6-inch", "RAM":"Not specified", "Storage":"256GB SSD"} },
  { sku: 'APP-MBP3-034', highlights: ["M3 chip for pro performance", "Brilliant Liquid Retina XDR display", "Up to 22 hours of battery life"], specs: {"Brand":"Apple", "Model Name":"Apple MacBook Pro M3", "Operating System":"macOS", "Display":"14-inch", "RAM":"Not specified", "Storage":"Not specified"} },
  
  { sku: 'SAM-A35-021', highlights: ["Super AMOLED display", "50MP high-resolution camera", "Knox Security"], specs: {"Brand":"Samsung", "Model Name":"Samsung Galaxy A35 5G", "Operating System":"Android", "Cellular Technology":"5G", "Storage":"Not specified"} },
  { sku: 'SAM-A35M-024', highlights: ["Super AMOLED display", "50MP high-resolution camera", "Knox Security"], specs: {"Brand":"Samsung", "Model Name":"Samsung Galaxy A35 5G Midnight", "Operating System":"Android", "Cellular Technology":"5G", "Storage":"Not specified"} },
  { sku: 'SAM-S10-022', highlights: ["Cinematic Infinity Display", "Pro-grade camera", "Wireless PowerShare"], specs: {"Brand":"Samsung", "Model Name":"Samsung Galaxy S10", "Operating System":"Android", "Cellular Technology":"4G/LTE", "Storage":"Not specified"} },
  { sku: 'SAM-S24-128-BLK', highlights: ["Galaxy AI enabled", "Armor Aluminum frame", "Nightography camera setup"], specs: {"Brand":"Samsung", "Model Name":"Samsung Galaxy S24", "Operating System":"Android", "Cellular Technology":"5G", "Storage":"128GB"} },
  { sku: 'SAM-N10-023', highlights: ["Intelligent S Pen", "Dynamic AMOLED display", "Pro-grade camera"], specs: {"Brand":"Samsung", "Model Name":"Samsung Galaxy Note 10", "Operating System":"Android", "Cellular Technology":"4G/LTE", "Storage":"Not specified"} },
  { sku: 'SAM-TAB-128-GRY', highlights: ["S Pen included in box", "120Hz vivid display", "DeX multitasking mode"], specs: {"Brand":"Samsung", "Model Name":"Samsung Galaxy Tab", "Operating System":"Android", "Cellular Technology":"4G/LTE", "Storage":"128GB"} },
  { sku: 'SAM-BUDS2PRO-BLK', highlights: ["Active Noise Canceling", "24-bit Hi-Fi audio", "Comfortable ergonomic fit"], specs: {"Brand":"Samsung", "Model Name":"Samsung Galaxy Buds2 Pro", "Connectivity":"Bluetooth 5.3"} },

  { sku: 'LEN-E14-I5-16', highlights: ["Mil-Spec tested for durability", "Ergonomic legendary keyboard", "Rapid charge (80% in 1 hr)"], specs: {"Brand":"Lenovo", "Model Name":"Lenovo ThinkPad E14", "Operating System":"Windows 11", "RAM":"16GB", "Display":"14-inch", "Storage":"Not specified"} },
  { sku: 'LEN-T14-026', highlights: ["Enterprise-grade security", "All-day battery life", "Premium durable chassis"], specs: {"Brand":"Lenovo", "Model Name":"Lenovo ThinkPad T14", "Operating System":"Windows 11", "RAM":"Not specified", "Display":"14-inch", "Storage":"Not specified"} },
  { sku: 'LEN-X1C-025', highlights: ["Ultralight carbon-fiber chassis", "Premium 4K display options", "Dolby Atmos speaker system"], specs: {"Brand":"Lenovo", "Model Name":"Lenovo ThinkPad X1 Carbon", "Operating System":"Windows 11", "RAM":"Not specified", "Display":"14-inch", "Storage":"Not specified"} },
  { sku: 'LEN-YOGA3-028', highlights: ["360-degree watchband hinge", "Ultra-thin and lightweight", "QHD+ touchscreen display"], specs: {"Brand":"Lenovo", "Model Name":"Lenovo Yoga 3 Pro", "Operating System":"Windows 10", "RAM":"Not specified", "Display":"13.3-inch", "Storage":"Not specified"} },
  { sku: 'LEN-LEGY7-027', highlights: ["High-performance gaming display", "Advanced Coldfront cooling", "NVIDIA GeForce RTX graphics"], specs: {"Brand":"Lenovo", "Model Name":"Lenovo Legion Y7000P", "Operating System":"Windows 11", "RAM":"Not specified", "Display":"15.6-inch", "Storage":"Not specified"} },

  { sku: 'LOG-MECH-KB-BLK', highlights: ["Tactile quiet mechanical switches", "Customizable backlighting", "Multi-device connectivity"], specs: {"Brand":"Logitech", "Model Name":"Logitech Mechanical Keyboard", "Connectivity":"Wired USB"} },
  { sku: 'LOG-K120-031', highlights: ["Durable, spill-resistant design", "Quiet, comfortable typing", "Plug-and-play USB connection"], specs: {"Brand":"Logitech", "Model Name":"Logitech K120 Keyboard", "Connectivity":"Wired USB"} },
  { sku: 'LOG-MX3S-GRAPH', highlights: ["8K DPI any-surface tracking", "Quiet Clicks - 90% less noise", "MagSpeed scrolling"], specs: {"Brand":"Logitech", "Model Name":"Logitech MX Master 3S", "Connectivity":"Bluetooth / USB Receiver"} },
  { sku: 'LOG-G305-030', highlights: ["LIGHTSPEED wireless technology", "HERO sensor for precise tracking", "Ultra-long battery life"], specs: {"Brand":"Logitech", "Model Name":"Logitech G305 Wireless Mouse", "Connectivity":"Wireless USB Receiver"} },
  { sku: 'LOG-G502-029', highlights: ["HERO 25K sensor", "11 customizable buttons", "Adjustable weight system"], specs: {"Brand":"Logitech", "Model Name":"Logitech G502 Gaming Mouse", "Connectivity":"Wired USB"} },
  { sku: 'LOG-C920-032', highlights: ["Full HD 1080p video calling", "Dual stereo microphones", "Automatic low-light correction"], specs: {"Brand":"Logitech", "Model Name":"Logitech C920 HD Webcam", "Connectivity":"Wired USB", "Resolution":"1080p"} },

  { sku: 'SONY-WH1000XM5-BLK', highlights: ["Industry-leading noise cancellation", "Auto NC Optimizer", "30-hour battery life with quick charge"], specs: {"Brand":"Sony", "Model Name":"Sony WH-1000XM5", "Connectivity":"Bluetooth 5.2"} },
  { sku: 'SON-WF4-036', highlights: ["Integrated Processor V1", "Crystal-clear call quality", "IPX4 water resistance"], specs: {"Brand":"Sony", "Model Name":"Sony WF-1000XM4", "Connectivity":"Bluetooth 5.2"} },
  { sku: 'SONY-BT-SPK-BLK', highlights: ["IP67 water and dust resistant", "Extra Bass technology", "24 hour battery life"], specs: {"Brand":"Sony", "Model Name":"Sony Portable Bluetooth Speaker", "Connectivity":"Bluetooth 5.0"} },
  { sku: 'SON-PS2-040', highlights: ["Vast library of classic games", "Built-in DVD player", "DualShock 2 analog controller"], specs: {"Brand":"Sony", "Model Name":"Sony PlayStation 2", "Resolution":"480i/480p", "Storage":"Not specified"} },
  { sku: 'SON-PS3-039', highlights: ["High-definition Blu-ray player", "PlayStation Network access", "Wireless DualShock 3 controller"], specs: {"Brand":"Sony", "Model Name":"Sony PlayStation 3", "Resolution":"720p/1080p", "Storage":"Not specified"} },
  { sku: 'SON-PS4-038', highlights: ["Incredible HDR visuals", "Extensive exclusive game library", "DualShock 4 wireless controller"], specs: {"Brand":"Sony", "Model Name":"Sony PlayStation 4", "Resolution":"1080p", "Storage":"Not specified"} },
  { sku: 'SON-DS5-037', highlights: ["Haptic feedback technology", "Adaptive triggers", "Built-in microphone and headset jack"], specs: {"Brand":"Sony", "Model Name":"Sony DualSense Wireless Controller", "Connectivity":"Bluetooth / USB-C"} }
];

let sql = '';
for (const p of products) {
  const highlightsJson = JSON.stringify(p.highlights).replace(/'/g, "''");
  const specsJson = JSON.stringify(p.specs).replace(/'/g, "''");
  sql += `UPDATE products SET highlights = '${highlightsJson}'::jsonb, specifications = '${specsJson}'::jsonb WHERE sku = '${p.sku}';\n`;
}

fs.writeFileSync('fix_data.sql', sql);
