const fs = require('fs');

const products = [
  { 
    sku: 'APP-IPADA-033', 
    description: 'The Apple iPad Air features a stunning Liquid Retina display and the powerful M1 chip for smooth multitasking and gaming. With Touch ID built into the top button, it provides quick, easy, and secure authentication for everyday use.',
    highlights: ['10.9-inch Liquid Retina display with True Tone', 'M1 chip with Neural Engine', 'Touch ID for secure authentication', 'Supports Apple Pencil (2nd generation)', 'USB-C connector for charging and accessories'], 
    specs: {
      'General': { 'Brand': 'Apple', 'Model Name': 'Apple iPad Air', 'Operating System': 'iPadOS', 'Color': 'Not specified' },
      'Display': { 'Display Type': 'Liquid Retina', 'Screen Size': '10.9 inches', 'Resolution': '2360 x 1640' },
      'Processor': { 'Processor': 'Apple M1 chip' },
      'Memory & Storage': { 'RAM': '8GB', 'Storage': 'Not specified' },
      'Camera': { 'Rear Camera': '12MP Wide', 'Front Camera': '12MP Ultra Wide' },
      'Connectivity': { 'Cellular Technology': 'Not specified', 'Wi-Fi': 'Wi-Fi 6', 'USB': 'USB-C' }
    }
  },
  { 
    sku: 'APP-IP15-128-BLU', 
    description: 'Apple iPhone 15 combines a sleek aluminum design with powerful everyday performance. It features a 48MP main camera, Dynamic Island, and USB-C connectivity, making it ideal for photography, communication, and productivity.',
    highlights: ['Dynamic Island for alerts and Live Activities', '48MP Main camera for high-resolution photography', 'A16 Bionic chip for reliable performance', 'USB-C connectivity', '5G cellular connectivity'], 
    specs: {
      'General': { 'Brand': 'Apple', 'Model Name': 'Apple iPhone 15', 'Operating System': 'iOS', 'Color': 'Blue' },
      'Display': { 'Display Type': 'Super Retina XDR OLED', 'Screen Size': '6.1 inches' },
      'Processor': { 'Processor': 'A16 Bionic chip' },
      'Memory & Storage': { 'RAM': 'Not specified', 'Storage': '128GB' },
      'Camera': { 'Rear Camera': '48MP Main + 12MP Ultra Wide', 'Front Camera': '12MP TrueDepth' },
      'Connectivity': { 'Cellular Technology': '5G', 'USB': 'USB-C' }
    }
  },
  { 
    sku: 'APP-MBA2-035', 
    description: 'The Apple MacBook Air M2 is strikingly thin and brings exceptional speed and power efficiency within a durable aluminum enclosure. It features a 13.6-inch Liquid Retina display and up to 18 hours of battery life.',
    highlights: ['M2 chip for exceptional speed and efficiency', '13.6-inch Liquid Retina display', 'Up to 18 hours of battery life', '1080p FaceTime HD camera', 'MagSafe 3 charging port'], 
    specs: {
      'General': { 'Brand': 'Apple', 'Model Name': 'Apple MacBook Air M2', 'Operating System': 'macOS' },
      'Display': { 'Display Type': 'Liquid Retina', 'Screen Size': '13.6 inches' },
      'Processor': { 'Processor': 'Apple M2 chip' },
      'Memory': { 'RAM': 'Not specified' },
      'Storage': { 'Storage': 'Not specified' },
      'Connectivity': { 'Wi-Fi': 'Wi-Fi 6', 'Bluetooth': 'Bluetooth 5.3', 'Ports': '2x Thunderbolt / USB 4' },
      'Battery': { 'Battery Life': 'Up to 18 hours' }
    }
  },
  { 
    sku: 'APP-MBA-M3-256', 
    description: 'Supercharged by the M3 chip, the Apple MacBook Air M3 delivers even faster performance and advanced AI capabilities in a portable design. It supports up to two external displays and offers blazing-fast Wi-Fi 6E.',
    highlights: ['Supercharged by M3 chip with advanced AI capabilities', '13.6-inch Liquid Retina display with 1 billion colors', 'Supports up to two external displays (with laptop closed)', 'Up to 18 hours of battery life', 'Wi-Fi 6E connectivity'], 
    specs: {
      'General': { 'Brand': 'Apple', 'Model Name': 'Apple MacBook Air M3', 'Operating System': 'macOS' },
      'Display': { 'Display Type': 'Liquid Retina', 'Screen Size': '13.6 inches' },
      'Processor': { 'Processor': 'Apple M3 chip' },
      'Memory': { 'RAM': 'Not specified' },
      'Storage': { 'Storage': '256GB SSD' },
      'Connectivity': { 'Wi-Fi': 'Wi-Fi 6E', 'Bluetooth': 'Bluetooth 5.3', 'Ports': '2x Thunderbolt / USB 4' },
      'Battery': { 'Battery Life': 'Up to 18 hours' }
    }
  },
  { 
    sku: 'APP-MBP3-034', 
    description: 'The Apple MacBook Pro M3 is a powerhouse designed for pros, featuring a brilliant Liquid Retina XDR display and an advanced thermal architecture. It delivers sustained performance for demanding workflows and up to 22 hours of battery life.',
    highlights: ['M3 chip for pro-level performance', '14-inch Liquid Retina XDR display with ProMotion', 'Up to 22 hours of battery life', 'Hardware-accelerated ray tracing', 'Extensive connectivity including SDXC and HDMI'], 
    specs: {
      'General': { 'Brand': 'Apple', 'Model Name': 'Apple MacBook Pro M3', 'Operating System': 'macOS' },
      'Display': { 'Display Type': 'Liquid Retina XDR', 'Screen Size': '14 inches', 'Refresh Rate': 'Up to 120Hz (ProMotion)' },
      'Processor': { 'Processor': 'Apple M3 chip' },
      'Memory': { 'RAM': 'Not specified' },
      'Storage': { 'Storage': 'Not specified' },
      'Connectivity': { 'Wi-Fi': 'Wi-Fi 6E', 'Ports': 'MagSafe 3, 2x Thunderbolt / USB 4, HDMI, SDXC' },
      'Battery': { 'Battery Life': 'Up to 22 hours' }
    }
  },
  { 
    sku: 'SAM-A35-021', 
    description: 'The Samsung Galaxy A35 5G features a vibrant Super AMOLED display and a versatile 50MP high-resolution camera. It offers robust performance for everyday tasks and is protected by Samsung Knox Security.',
    highlights: ['Super AMOLED display with smooth refresh rate', '50MP high-resolution main camera', 'Samsung Knox Security for data protection', '5G cellular connectivity', 'Long-lasting battery'], 
    specs: {
      'General': { 'Brand': 'Samsung', 'Model Name': 'Samsung Galaxy A35 5G', 'Operating System': 'Android' },
      'Display': { 'Display Type': 'Super AMOLED', 'Screen Size': 'Not specified' },
      'Camera': { 'Rear Camera': '50MP Main', 'Front Camera': 'Not specified' },
      'Memory & Storage': { 'RAM': 'Not specified', 'Storage': 'Not specified' },
      'Connectivity': { 'Cellular Technology': '5G' },
      'Battery': { 'Battery Capacity': 'Not specified' }
    }
  },
  { 
    sku: 'SAM-A35M-024', 
    description: 'The Samsung Galaxy A35 5G Midnight edition combines a sleek, dark aesthetic with a vibrant Super AMOLED display. It includes a 50MP camera and robust Knox Security for safe, high-speed 5G browsing and streaming.',
    highlights: ['Super AMOLED display with smooth refresh rate', '50MP high-resolution main camera', 'Samsung Knox Security for data protection', '5G cellular connectivity', 'Midnight colorway'], 
    specs: {
      'General': { 'Brand': 'Samsung', 'Model Name': 'Samsung Galaxy A35 5G', 'Operating System': 'Android', 'Color': 'Midnight' },
      'Display': { 'Display Type': 'Super AMOLED', 'Screen Size': 'Not specified' },
      'Camera': { 'Rear Camera': '50MP Main', 'Front Camera': 'Not specified' },
      'Memory & Storage': { 'RAM': 'Not specified', 'Storage': 'Not specified' },
      'Connectivity': { 'Cellular Technology': '5G' },
      'Battery': { 'Battery Capacity': 'Not specified' }
    }
  },
  { 
    sku: 'SAM-S10-022', 
    description: 'The Samsung Galaxy S10 delivers a premium smartphone experience with its Cinematic Infinity Display and pro-grade camera system. It includes innovative features like Wireless PowerShare for charging compatible devices on the go.',
    highlights: ['Cinematic Infinity Display with near bezel-less design', 'Pro-grade multi-lens camera system', 'Wireless PowerShare device-to-device charging', 'In-display ultrasonic fingerprint sensor', 'Water and dust resistant (IP68)'], 
    specs: {
      'General': { 'Brand': 'Samsung', 'Model Name': 'Samsung Galaxy S10', 'Operating System': 'Android' },
      'Display': { 'Display Type': 'Dynamic AMOLED', 'Screen Size': '6.1 inches' },
      'Camera': { 'Rear Camera': 'Pro-grade multi-lens', 'Front Camera': 'Not specified' },
      'Memory & Storage': { 'RAM': 'Not specified', 'Storage': 'Not specified' },
      'Connectivity': { 'Cellular Technology': '4G LTE' },
      'Battery': { 'Charging': 'Wireless PowerShare supported' }
    }
  },
  { 
    sku: 'SAM-S24-128-BLK', 
    description: 'The Samsung Galaxy S24 introduces revolutionary Galaxy AI capabilities to enhance your daily communication and creativity. Encased in a durable Armor Aluminum frame, it features a powerful Nightography camera setup.',
    highlights: ['Galaxy AI enabled for advanced photo editing and live translation', 'Durable Armor Aluminum frame', 'Nightography camera setup for low-light photos', 'Dynamic AMOLED 2X display', '5G cellular connectivity'], 
    specs: {
      'General': { 'Brand': 'Samsung', 'Model Name': 'Samsung Galaxy S24', 'Operating System': 'Android', 'Color': 'Black' },
      'Display': { 'Display Type': 'Dynamic AMOLED 2X', 'Screen Size': 'Not specified' },
      'Camera': { 'Rear Camera': 'Advanced Nightography setup', 'Front Camera': 'Not specified' },
      'Memory & Storage': { 'RAM': 'Not specified', 'Storage': '128GB' },
      'Connectivity': { 'Cellular Technology': '5G' }
    }
  },
  { 
    sku: 'SAM-N10-023', 
    description: 'The Samsung Galaxy Note 10 brings a computer, gaming console, and pro-grade camera into one device. It features the intelligent S Pen for precise note-taking, drawing, and remote control capabilities.',
    highlights: ['Intelligent S Pen with Air Actions', 'Dynamic AMOLED display for brilliant colors', 'Pro-grade camera with advanced video capabilities', 'Super Fast Charging', 'Seamless Microsoft integration'], 
    specs: {
      'General': { 'Brand': 'Samsung', 'Model Name': 'Samsung Galaxy Note 10', 'Operating System': 'Android' },
      'Display': { 'Display Type': 'Dynamic AMOLED', 'Screen Size': 'Not specified' },
      'Camera': { 'Rear Camera': 'Pro-grade multi-lens', 'Front Camera': 'Not specified' },
      'Memory & Storage': { 'RAM': 'Not specified', 'Storage': 'Not specified' },
      'Connectivity': { 'Cellular Technology': '4G LTE' },
      'Accessories': { 'Stylus': 'S Pen included' }
    }
  },
  { 
    sku: 'SAM-TAB-128-GRY', 
    description: 'This Samsung Galaxy Tab offers a versatile Android tablet experience with an immersive 120Hz display. Equipped with an S Pen, it seamlessly transitions between a multimedia powerhouse and a productivity workstation using DeX mode.',
    highlights: ['120Hz vivid display for smooth scrolling', 'S Pen included in box for note-taking and drawing', 'DeX multitasking mode for a PC-like experience', 'Quad speakers tuned by AKG', 'Expandable storage via microSD'], 
    specs: {
      'General': { 'Brand': 'Samsung', 'Model Name': 'Samsung Galaxy Tab', 'Operating System': 'Android', 'Color': 'Grey' },
      'Display': { 'Display Type': 'Vivid Display', 'Refresh Rate': '120Hz' },
      'Memory & Storage': { 'RAM': 'Not specified', 'Storage': '128GB' },
      'Connectivity': { 'Cellular Technology': '4G LTE' },
      'Accessories': { 'Stylus': 'S Pen included' }
    }
  },
  { 
    sku: 'SAM-BUDS2PRO-BLK', 
    description: 'The Samsung Galaxy Buds2 Pro deliver studio-quality 24-bit Hi-Fi audio in a comfortable, ergonomic design. Featuring intelligent Active Noise Canceling, they block out unwanted sounds for an immersive listening experience.',
    highlights: ['Intelligent Active Noise Canceling', '24-bit Hi-Fi audio for studio-quality sound', 'Comfortable ergonomic fit for all-day wear', '360 Audio for a cinematic experience', 'IPX7 water resistance'], 
    specs: {
      'General': { 'Brand': 'Samsung', 'Model Name': 'Samsung Galaxy Buds2 Pro', 'Color': 'Black' },
      'Audio': { 'Noise Cancellation': 'Active Noise Canceling', 'Sound Quality': '24-bit Hi-Fi' },
      'Connectivity': { 'Bluetooth': 'Bluetooth 5.3' },
      'Battery': { 'Battery Life': 'Not specified' },
      'Durability': { 'Water Resistance': 'IPX7' }
    }
  },
  { 
    sku: 'LEN-E14-I5-16', 
    description: 'The Lenovo ThinkPad E14 is a robust business laptop designed for productivity and reliability. It features an ergonomic legendary keyboard, rapid charge technology, and Mil-Spec durability for on-the-go professionals.',
    highlights: ['Mil-Spec tested for extreme durability', 'Ergonomic legendary ThinkPad keyboard', 'Rapid charge capability (80% in 1 hr)', 'Integrated security features', '14-inch display for portable productivity'], 
    specs: {
      'General': { 'Brand': 'Lenovo', 'Model Name': 'Lenovo ThinkPad E14', 'Operating System': 'Windows 11' },
      'Display': { 'Display Type': 'Anti-glare', 'Screen Size': '14 inches' },
      'Processor': { 'Processor': 'Intel Core i5 (unspecified generation)' },
      'Memory': { 'RAM': '16GB' },
      'Storage': { 'Storage': 'Not specified' },
      'Connectivity': { 'Wi-Fi': 'Not specified', 'Bluetooth': 'Not specified' }
    }
  },
  { 
    sku: 'LEN-T14-026', 
    description: 'The Lenovo ThinkPad T14 is an enterprise-grade laptop built for serious business performance. It combines a premium durable chassis with all-day battery life and robust security features.',
    highlights: ['Enterprise-grade security and manageability', 'All-day battery life', 'Premium durable chassis', 'Legendary TrackPoint keyboard', 'Comprehensive port selection'], 
    specs: {
      'General': { 'Brand': 'Lenovo', 'Model Name': 'Lenovo ThinkPad T14', 'Operating System': 'Windows 11' },
      'Display': { 'Display Type': 'Anti-glare', 'Screen Size': '14 inches' },
      'Processor': { 'Processor': 'Not specified' },
      'Memory': { 'RAM': 'Not specified' },
      'Storage': { 'Storage': 'Not specified' },
      'Battery': { 'Battery Life': 'All-day battery life' }
    }
  },
  { 
    sku: 'LEN-X1C-025', 
    description: 'The Lenovo ThinkPad X1 Carbon is an ultralight executive laptop crafted with a carbon-fiber chassis. It offers premium display options and a Dolby Atmos speaker system, making it perfect for high-level productivity and media consumption.',
    highlights: ['Ultralight carbon-fiber chassis', 'Premium display options (up to 4K)', 'Dolby Atmos speaker system', 'Exceptional battery life', 'Top-tier enterprise security'], 
    specs: {
      'General': { 'Brand': 'Lenovo', 'Model Name': 'Lenovo ThinkPad X1 Carbon', 'Operating System': 'Windows 11' },
      'Display': { 'Display Type': 'Premium Panel', 'Screen Size': '14 inches' },
      'Processor': { 'Processor': 'Not specified' },
      'Memory': { 'RAM': 'Not specified' },
      'Storage': { 'Storage': 'Not specified' },
      'Audio': { 'Speakers': 'Dolby Atmos speaker system' }
    }
  },
  { 
    sku: 'LEN-YOGA3-028', 
    description: 'The Lenovo Yoga 3 Pro is an ultra-thin and lightweight convertible laptop featuring a unique 360-degree watchband hinge. Its flexible design allows seamless transition between laptop, tablet, tent, and stand modes.',
    highlights: ['360-degree watchband hinge for versatile modes', 'Ultra-thin and lightweight profile', 'QHD+ touchscreen display for sharp visuals', 'JBL stereo speakers', 'Long-lasting battery performance'], 
    specs: {
      'General': { 'Brand': 'Lenovo', 'Model Name': 'Lenovo Yoga 3 Pro', 'Operating System': 'Windows 10' },
      'Display': { 'Display Type': 'Touchscreen', 'Screen Size': '13.3 inches', 'Resolution': 'QHD+' },
      'Processor': { 'Processor': 'Not specified' },
      'Memory': { 'RAM': 'Not specified' },
      'Storage': { 'Storage': 'Not specified' },
      'Audio': { 'Speakers': 'JBL stereo speakers' }
    }
  },
  { 
    sku: 'LEN-LEGY7-027', 
    description: 'The Lenovo Legion Y7000P is a powerful gaming laptop built for competitive play. Featuring advanced Coldfront cooling and NVIDIA GeForce RTX graphics, it sustains high frame rates on a fast 15.6-inch display.',
    highlights: ['High-performance gaming display for smooth visuals', 'Advanced Coldfront cooling system', 'NVIDIA GeForce RTX graphics', 'TrueStrike keyboard with white backlight', 'Robust processing power for AAA gaming'], 
    specs: {
      'General': { 'Brand': 'Lenovo', 'Model Name': 'Lenovo Legion Y7000P', 'Operating System': 'Windows 11' },
      'Display': { 'Display Type': 'Gaming Panel', 'Screen Size': '15.6 inches' },
      'Processor': { 'Processor': 'Not specified' },
      'Graphics': { 'GPU': 'NVIDIA GeForce RTX' },
      'Memory': { 'RAM': 'Not specified' },
      'Storage': { 'Storage': 'Not specified' }
    }
  },
  { 
    sku: 'LOG-MECH-KB-BLK', 
    description: 'This Logitech Mechanical Keyboard offers a tactile, quiet typing experience with multi-device connectivity. Its customizable backlighting and durable build make it an excellent choice for both work and gaming.',
    highlights: ['Tactile quiet mechanical switches', 'Customizable backlighting', 'Multi-device connectivity', 'Durable aluminum top case', 'Wired USB connection'], 
    specs: {
      'General': { 'Brand': 'Logitech', 'Model Name': 'Logitech Mechanical Keyboard', 'Color': 'Black' },
      'Keys / Switches': { 'Switch Type': 'Tactile Quiet Mechanical' },
      'Connectivity': { 'Connection': 'Wired USB' },
      'Design': { 'Backlighting': 'Customizable' }
    }
  },
  { 
    sku: 'LOG-K120-031', 
    description: 'The Logitech K120 Keyboard provides a reliable and comfortable typing experience in a durable, spill-resistant design. Its simple plug-and-play USB connection ensures immediate setup without any software.',
    highlights: ['Durable, spill-resistant design', 'Quiet, comfortable typing', 'Plug-and-play USB connection', 'Sturdy adjustable tilt legs', 'Standard layout with full-size F-keys'], 
    specs: {
      'General': { 'Brand': 'Logitech', 'Model Name': 'Logitech K120 Keyboard' },
      'Connectivity': { 'Connection': 'Wired USB' },
      'Design': { 'Durability': 'Spill-resistant' }
    }
  },
  { 
    sku: 'LOG-MX3S-GRAPH', 
    description: 'The Logitech MX Master 3S is an advanced ergonomic mouse featuring an 8K DPI any-surface sensor and Quiet Clicks. It offers hyper-fast MagSpeed scrolling and deep customization for peak productivity.',
    highlights: ['8K DPI optical sensor tracks on any surface, even glass', 'Quiet Clicks deliver 90% less click noise', 'MagSpeed electromagnetic scrolling', 'Ergonomic silhouette crafted for comfort', 'Multi-device Bluetooth and USB receiver connectivity'], 
    specs: {
      'General': { 'Brand': 'Logitech', 'Model Name': 'Logitech MX Master 3S', 'Color': 'Graphite' },
      'Sensor': { 'Sensor Type': 'High Precision Optical', 'DPI': '8000 DPI' },
      'Connectivity': { 'Connection': 'Bluetooth / USB Receiver' },
      'Buttons': { 'Clicks': 'Quiet Clicks' },
      'Battery': { 'Charging': 'USB-C' }
    }
  },
  { 
    sku: 'LOG-G305-030', 
    description: 'The Logitech G305 is a lightweight wireless gaming mouse equipped with LIGHTSPEED technology and the precise HERO sensor. It delivers ultra-long battery life and reliable performance for competitive gaming.',
    highlights: ['LIGHTSPEED wireless technology for lag-free gaming', 'HERO sensor for precise tracking up to 12,000 DPI', 'Ultra-long battery life on a single AA battery', 'Lightweight design (99 grams)', '6 programmable buttons'], 
    specs: {
      'General': { 'Brand': 'Logitech', 'Model Name': 'Logitech G305 Wireless Mouse' },
      'Sensor': { 'Sensor Type': 'HERO Optical', 'DPI': '12,000 DPI' },
      'Connectivity': { 'Connection': 'Wireless USB Receiver' },
      'Buttons': { 'Programmable Buttons': '6' },
      'Battery': { 'Battery Type': '1x AA Battery' }
    }
  },
  { 
    sku: 'LOG-G502-029', 
    description: 'The Logitech G502 Gaming Mouse is a high-performance wired mouse featuring the HERO 25K sensor. With 11 customizable buttons and an adjustable weight system, it provides ultimate control and personalization.',
    highlights: ['HERO 25K sensor for sub-micron precision', '11 customizable buttons', 'Adjustable weight system for personalized balance', 'Customizable LIGHTSYNC RGB', 'Dual-mode hyper-fast scroll wheel'], 
    specs: {
      'General': { 'Brand': 'Logitech', 'Model Name': 'Logitech G502 Gaming Mouse' },
      'Sensor': { 'Sensor Type': 'HERO 25K Optical', 'DPI': '25,600 DPI' },
      'Connectivity': { 'Connection': 'Wired USB' },
      'Buttons': { 'Programmable Buttons': '11' },
      'Customization': { 'Weights': 'Adjustable weight system' }
    }
  },
  { 
    sku: 'LOG-C920-032', 
    description: 'The Logitech C920 HD Webcam delivers crisp, detailed Full HD 1080p video calling. Equipped with dual stereo microphones and automatic low-light correction, it ensures you look and sound your best on any platform.',
    highlights: ['Full HD 1080p video calling and recording', 'Dual stereo microphones for clear natural audio', 'Automatic low-light correction', 'Premium glass lens with autofocus', 'Universal clip fits laptops and monitors'], 
    specs: {
      'General': { 'Brand': 'Logitech', 'Model Name': 'Logitech C920 HD Webcam' },
      'Video': { 'Resolution': '1080p / 30fps', 'Focus': 'Autofocus' },
      'Audio': { 'Microphone': 'Dual stereo microphones' },
      'Connectivity': { 'Connection': 'Wired USB' }
    }
  },
  { 
    sku: 'SONY-WH1000XM5-BLK', 
    description: 'The Sony WH-1000XM5 wireless headphones offer industry-leading noise cancellation and exceptional high-resolution audio. With a sleek, lightweight design and up to 30 hours of battery life, they provide superior comfort for extended listening.',
    highlights: ['Industry-leading noise cancellation with multiple microphones', 'Auto NC Optimizer adapts to your environment', '30-hour battery life with quick charge', 'Ultra-comfortable, lightweight design', 'Crystal clear hands-free calling'], 
    specs: {
      'General': { 'Brand': 'Sony', 'Model Name': 'Sony WH-1000XM5', 'Color': 'Black' },
      'Audio': { 'Noise Cancellation': 'Active Noise Cancellation' },
      'Connectivity': { 'Bluetooth': 'Bluetooth 5.2' },
      'Battery': { 'Battery Life': 'Up to 30 hours' },
      'Controls': { 'Interface': 'Touch Sensor' }
    }
  },
  { 
    sku: 'SON-WF4-036', 
    description: 'The Sony WF-1000XM4 truly wireless earbuds feature the Integrated Processor V1 for unmatched noise cancellation. They deliver exceptional sound quality and crystal-clear calls in a compact, water-resistant design.',
    highlights: ['Integrated Processor V1 for superior noise cancellation', 'Crystal-clear call quality with beamforming microphones', 'IPX4 water resistance for everyday use', 'High-Resolution Audio Wireless support', 'Ergonomic surface design for a stable fit'], 
    specs: {
      'General': { 'Brand': 'Sony', 'Model Name': 'Sony WF-1000XM4' },
      'Audio': { 'Noise Cancellation': 'Active Noise Cancellation' },
      'Connectivity': { 'Bluetooth': 'Bluetooth 5.2' },
      'Durability': { 'Water Resistance': 'IPX4' },
      'Battery': { 'Battery Life': 'Not specified' }
    }
  },
  { 
    sku: 'SONY-BT-SPK-BLK', 
    description: 'This Sony Portable Bluetooth Speaker delivers deep, punchy Extra Bass in a durable, travel-ready design. With an IP67 rating and a 24-hour battery life, it is the perfect companion for outdoor adventures and parties.',
    highlights: ['Extra Bass technology for deep, punchy sound', 'IP67 water and dust resistant', 'Up to 24 hours of battery life', 'Built-in microphone for hands-free calling', 'Compact and portable design'], 
    specs: {
      'General': { 'Brand': 'Sony', 'Model Name': 'Sony Portable Bluetooth Speaker', 'Color': 'Black' },
      'Audio': { 'Sound Technology': 'Extra Bass' },
      'Connectivity': { 'Bluetooth': 'Bluetooth 5.0' },
      'Battery': { 'Battery Life': 'Up to 24 hours' },
      'Durability': { 'Water & Dust Resistance': 'IP67' }
    }
  },
  { 
    sku: 'SON-PS2-040', 
    description: 'The Sony PlayStation 2 is a legendary home video game console that redefined entertainment. It boasts a vast library of classic games and doubles as a built-in DVD player for movies and media.',
    highlights: ['Vast library of legendary classic games', 'Built-in DVD/CD player', 'DualShock 2 analog controller', 'Compact and reliable hardware', 'Supports multiplayer gaming'], 
    specs: {
      'General': { 'Brand': 'Sony', 'Model Name': 'Sony PlayStation 2' },
      'Performance': { 'Processor': 'Emotion Engine' },
      'Video Output': { 'Resolution': '480i / 480p' },
      'Storage': { 'Storage': 'Not specified (Requires Memory Card)' },
      'Controllers': { 'Included': 'DualShock 2' }
    }
  },
  { 
    sku: 'SON-PS3-039', 
    description: 'The Sony PlayStation 3 ushered in the era of high-definition gaming and multimedia. Featuring a built-in Blu-ray player and free PlayStation Network access, it serves as a complete home entertainment hub.',
    highlights: ['High-definition gaming and Blu-ray player', 'Free PlayStation Network access for online play', 'Wireless DualShock 3 controller', 'Internal hard drive for digital downloads', 'Wi-Fi connectivity'], 
    specs: {
      'General': { 'Brand': 'Sony', 'Model Name': 'Sony PlayStation 3' },
      'Video Output': { 'Resolution': '720p / 1080p', 'Media': 'Blu-ray Disc Drive' },
      'Storage': { 'Storage': 'Not specified' },
      'Controllers': { 'Included': 'DualShock 3' },
      'Connectivity': { 'Network': 'Wi-Fi & Ethernet' }
    }
  },
  { 
    sku: 'SON-PS4-038', 
    description: 'The Sony PlayStation 4 delivers incredible HDR visuals and an extensive library of exclusive games. The refined DualShock 4 wireless controller provides precise control and a dedicated Share button to broadcast your gameplay.',
    highlights: ['Incredible HDR visuals for stunning gameplay', 'Extensive exclusive game library', 'DualShock 4 wireless controller with touchpad', 'Share button for social streaming', 'Robust online multiplayer ecosystem'], 
    specs: {
      'General': { 'Brand': 'Sony', 'Model Name': 'Sony PlayStation 4' },
      'Video Output': { 'Resolution': '1080p (HDR Supported)', 'Media': 'Blu-ray Disc Drive' },
      'Storage': { 'Storage': 'Not specified' },
      'Controllers': { 'Included': 'DualShock 4' },
      'Connectivity': { 'Network': 'Wi-Fi & Ethernet' }
    }
  },
  { 
    sku: 'SON-DS5-037', 
    description: 'The Sony DualSense Wireless Controller for PlayStation 5 offers immersive haptic feedback and dynamic adaptive triggers. It features a built-in microphone and headset jack within an iconic, comfortable design.',
    highlights: ['Immersive haptic feedback technology', 'Dynamic adaptive triggers', 'Built-in microphone and headset jack', 'Create button for sharing gameplay', 'Integrated battery with USB-C charging'], 
    specs: {
      'General': { 'Brand': 'Sony', 'Model Name': 'Sony DualSense Wireless Controller' },
      'Features': { 'Feedback': 'Haptic feedback', 'Triggers': 'Adaptive triggers' },
      'Connectivity': { 'Connection': 'Bluetooth / USB-C' },
      'Audio': { 'Microphone': 'Built-in' }
    }
  }
];

let sql = '';
for (const p of products) {
  const descStr = p.description.replace(/'/g, "''");
  const highlightsJson = JSON.stringify(p.highlights).replace(/'/g, "''");
  const specsJson = JSON.stringify(p.specs).replace(/'/g, "''");
  sql += `UPDATE products SET description = '${descStr}', highlights = '${highlightsJson}'::jsonb, specifications = '${specsJson}'::jsonb WHERE sku = '${p.sku}';\n`;
}

fs.writeFileSync('C:/Users/Siva Krishna/Desktop/Ecommerce/fix_data_grouped.sql', sql);
