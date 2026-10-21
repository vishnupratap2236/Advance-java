-- ShopSphere :: V2 - Demo catalog seed data
-- Inserted with plain INSERTs so the script stays re-runnable inside a transaction
-- (the migration runner only applies a migration once, guarded by schema_migrations).

INSERT INTO categories (name, slug, description, image_url) VALUES
('Audio',            'audio',            'Headphones, speakers and everything that sounds good.',        'images/categories/audio.svg'),
('Wearables',        'wearables',        'Smart watches and trackers built for everyday movement.',      'images/categories/wearables.svg'),
('Accessories',      'accessories',      'Keyboards, laptop stands, bags and desk essentials.',        'images/categories/accessories.svg'),
('Displays',         'displays',         'Monitors and screens for work and entertainment.',            'images/categories/displays.svg'),
('Home & Office',    'home-office',      'Lighting, seating and furniture for your workspace.',         'images/categories/home-office.svg'),
('Fitness',          'fitness',          'Training and wellness gear for an active routine.',            'images/categories/fitness.svg'),
('Kitchen',          'kitchen',          'Cookware and tableware made for daily use.',                  'images/categories/kitchen.svg'),
('Storage',          'storage',          'Fast, portable storage for your files and backups.',           'images/categories/storage.svg');

INSERT INTO products
(category_id, name, slug, sku, short_description, description, price, compare_price, stock, image_url, active, featured, rating, review_count)
VALUES
(1, 'Aurora Wireless Headphones', 'aurora-wireless-headphones', 'SS-AUD-1001',
 'Hybrid active noise cancelling over-ear headphones with 40 hour battery life.',
 'Aurora pairs a pair of 40mm bio-cellulose drivers with adaptive hybrid noise cancelling so the music stays forward and the office stays behind. Memory-foam earcups, multipoint Bluetooth pairing and a 40 hour battery make it an easy all-day companion.',
 12999.00, 15999.00, 40, 'images/products/aurora-headphones.svg', 1, 1, 4.70, 218),

(1, 'Nimbus Portable Speaker', 'nimbus-portable-speaker', 'SS-AUD-1002',
 '360 degree sound in a water resistant shell with 20 hours of playback.',
 'Nimbus is built for rooms, patios and campsites. A passive radiator delivers surprisingly deep bass for its size, while IPX7 water resistance and a braided carry loop keep it ready for anything.',
 3499.00, 4299.00, 65, 'images/products/nimbus-speaker.svg', 1, 1, 4.40, 141),

(1, 'Echo Buds Pro', 'echo-buds-pro', 'SS-AUD-1003',
 'Compact true wireless earbuds with transparency mode and wireless charging case.',
 'Echo Buds Pro are small, light and surprisingly durable. Six microphones cut background noise, transparency mode lets the world in when you need it, and the case adds 24 more hours of listening.',
 4999.00, 5999.00, 0, 'images/products/echo-buds.svg', 1, 0, 4.20, 89),

(2, 'Pulse Smart Watch', 'pulse-smart-watch', 'SS-WEA-2001',
 'AMOLED fitness watch with GPS, heart rate tracking and 14 day battery.',
 'Pulse tracks every run, ride and swim with dual-band GPS, then shows you what actually matters: recovery, sleep quality and stress. The always-on AMOLED display stays readable in direct sun and lasts up to two weeks between charges.',
 7999.00, 9499.00, 32, 'images/products/pulse-watch.svg', 1, 1, 4.60, 312),

(2, 'Band Loop Fitness Tracker', 'band-loop-fitness-tracker', 'SS-WEA-2002',
 'Slim 24/7 heart rate, sleep and step tracker with a two week battery.',
 'Band Loop is the tracker you forget you are wearing. It weighs less than a coin, sleeps through a shower and syncs automatically to your phone.',
 2499.00, 2999.00, 120, 'images/products/band-loop.svg', 1, 0, 4.00, 76),

(3, 'Vertex Mechanical Keyboard', 'vertex-mechanical-keyboard', 'SS-ACC-3001',
 'Hot-swappable 75% mechanical keyboard with PBT keycaps and wireless support.',
 'Vertex is a 75% layout that keeps the arrow keys and removes the clutter. Hot-swap sockets, PBT double-shot keycaps and switch options make it as pleasant to type on as it is to look at on a desk.',
 5499.00, 6999.00, 48, 'images/products/vertex-keyboard.svg', 1, 1, 4.80, 264),

(3, 'Nova Laptop Stand', 'nova-laptop-stand', 'SS-ACC-3002',
 'Aluminium folding laptop riser with six height positions.',
 'Nova folds flat into a sleeve and holds laptops from 11 to 17 inches at six angles. The anodised aluminium body dissipates heat and does not wobble.',
 1299.00, 1799.00, 95, 'images/products/nova-stand.svg', 1, 0, 4.30, 58),

(3, 'Orbit Laptop Backpack', 'orbit-laptop-backpack', 'SS-ACC-3003',
 'Water resistant 22L commuter backpack with a dedicated 16 inch laptop sleeve.',
 'Orbit keeps a 16 inch laptop, charger, bottle and folder organised without looking like a hiking pack. The back panel is padded and breathable for long commutes.',
 2799.00, 3299.00, 74, 'images/products/orbit-backpack.svg', 1, 0, 4.50, 133),

(4, 'Lumen 27 inch 4K Monitor', 'lumen-27-inch-4k-monitor', 'SS-DIS-4001',
 '27 inch 4K IPS panel with USB-C power delivery, 99 percent sRGB and a fully adjustable stand.',
 'Lumen is colour accurate out of the box, with a matte IPS panel, 99 percent sRGB coverage and a single USB-C cable that charges a laptop while carrying video and data. The stand pivots, tilts and lifts to 120mm.',
 21999.00, 25999.00, 18, 'images/products/lumen-monitor.svg', 1, 1, 4.70, 97),

(5, 'Halo Desk Lamp', 'halo-desk-lamp', 'SS-HOM-5001',
 'Dimmable LED task lamp with adjustable colour temperature and touch controls.',
 'Halo casts even light across a desk without glare. Choose warm, neutral or daylight white, dim it to ten percent, and set the arm exactly where you want it.',
 2299.00, 2799.00, 60, 'images/products/halo-lamp.svg', 1, 0, 4.40, 61),

(5, 'Drift Ergonomic Chair', 'drift-ergonomic-chair', 'SS-HOM-5002',
 'Mesh office chair with adjustable lumbar support, 4D armrests and a 5 year warranty.',
 'Drift keeps your spine supported through long sessions. The breathable mesh back flexes with you, the lumbar dial adjusts in millimetres, and the seat slides forward for short legs.',
 18999.00, 22999.00, 12, 'images/products/drift-chair.svg', 1, 1, 4.50, 44),

(6, 'Terra Pro Yoga Mat', 'terra-pro-yoga-mat', 'SS-FIT-6001',
 '6mm non-slip natural rubber yoga mat with alignment markings.',
 'Terra is dense, grippy and kind to your joints. The surface texture stays stable in a hot studio, and the alignment markers make it easy to hold a pose without a mirror.',
 1499.00, 1999.00, 88, 'images/products/terra-mat.svg', 1, 0, 4.60, 152),

(6, 'Kinetic Adjustable Dumbbells', 'kinetic-adjustable-dumbbells', 'SS-FIT-6002',
 'Dial adjustable dumbbells replacing 15 pairs of fixed weights.',
 'One dial per dumbbell replaces fifteen pairs. Each weight locks with a satisfying click and the whole set fits in a single corner of the room.',
 14999.00, 17999.00, 9, 'images/products/kinetic-dumbbells.svg', 1, 0, 4.30, 38),

(7, 'Zen Ceramic Dinner Set', 'zen-ceramic-dinner-set', 'SS-KIT-7001',
 'Service for four: four dinner plates, four side plates and four bowls in matte stoneware.',
 'Zen is glazed stoneware fired at high temperature, so it goes from freezer to oven to table. The matte finish hides the marks of everyday use and the stack-friendly profiles keep the cupboard tidy.',
 3299.00, 3999.00, 27, 'images/products/zen-dinner-set.svg', 1, 0, 4.20, 29),

(8, 'Quantum 1TB Portable SSD', 'quantum-1tb-portable-ssd', 'SS-STO-8001',
 'USB 3.2 Gen 2x2 external SSD at 2000MB/s with hardware encryption.',
 'Quantum moves large files at 2000MB/s and survives a two metre drop. Hardware encryption keeps the drive unreadable without your key, and the cable is USB-C to USB-C with a USB-A adapter in the box.',
 8999.00, 10999.00, 0, 'images/products/quantum-ssd.svg', 1, 1, 4.70, 186);

INSERT INTO product_images (product_id, image_url, alt_text, sort_order) VALUES
(1, 'images/products/aurora-headphones.svg', 'Aurora wireless headphones, front view', 0),
(1, 'images/products/aurora-headphones-alt.svg', 'Aurora wireless headphones, folded', 1),
(2, 'images/products/nimbus-speaker.svg', 'Nimbus portable speaker, front view', 0),
(3, 'images/products/echo-buds.svg', 'Echo Buds Pro earbuds with charging case', 0),
(4, 'images/products/pulse-watch.svg', 'Pulse smart watch on a dark background', 0),
(4, 'images/products/pulse-watch-alt.svg', 'Pulse smart watch screen showing activity', 1),
(5, 'images/products/vertex-keyboard.svg', 'Vertex mechanical keyboard', 0),
(6, 'images/products/nova-stand.svg', 'Nova aluminium laptop stand', 0),
(7, 'images/products/orbit-backpack.svg', 'Orbit commuter backpack', 0),
(8, 'images/products/lumen-monitor.svg', 'Lumen 4K monitor on an adjustable stand', 0),
(9, 'images/products/halo-lamp.svg', 'Halo LED desk lamp lit on a desk', 0),
(10, 'images/products/drift-chair.svg', 'Drift ergonomic office chair', 0),
(11, 'images/products/terra-mat.svg', 'Terra pro yoga mat rolled up', 0),
(12, 'images/products/kinetic-dumbbells.svg', 'Kinetic adjustable dumbbells', 0),
(13, 'images/products/zen-dinner-set.svg', 'Zen ceramic dinner set', 0),
(14, 'images/products/quantum-ssd.svg', 'Quantum portable SSD', 0);

INSERT INTO coupons (code, discount_percent, min_order_amount, max_discount, usage_limit, active) VALUES
('WELCOME10', 10.00, 999.00, 1500.00, 500, 1),
('BIGDEAL20', 20.00, 4999.00, 4000.00, 200, 1);
