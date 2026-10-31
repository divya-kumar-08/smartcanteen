-- ====================================================================
-- Smart Canteen Queue & Food Ordering System
-- Sample Menu & Initial Data
-- ====================================================================

USE smart_canteen_db;

-- Clear previous sample data if desired
DELETE FROM order_items;
DELETE FROM payments;
DELETE FROM orders;
DELETE FROM menu_items;
DELETE FROM students;

-- Insert Sample Students
INSERT INTO students (id, student_id_number, name, email, phone) VALUES
(1, 'CS2026-042', 'Rahul Sharma', 'rahul.sharma@college.edu', '9876543210'),
(2, 'EC2026-105', 'Priya Patel', 'priya.patel@college.edu', '9876543211'),
(3, 'ME2026-077', 'Ananya Verma', 'ananya.v@college.edu', '9876543212');

-- Insert Sample Menu Items across categories
INSERT INTO menu_items (id, name, description, category, price, image_url, available, preparation_time_minutes, active) VALUES
(1, 'Crispy Masala Dosa', 'Golden thin rice crepe filled with spiced potato masala, served with coconut chutney & sambar.', 'Breakfast', 60.00, 'https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=400&q=80', TRUE, 7, TRUE),
(2, 'Steamed Idli Sambar (3 pcs)', 'Soft fluffy steamed rice cakes served with hot lentil vegetable stew and fresh coconut chutney.', 'Breakfast', 40.00, 'https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=400&q=80', TRUE, 4, TRUE),
(3, 'Veg Grilled Cheese Sandwich', 'Toasted triple-decker sandwich with bell peppers, onions, tomatoes and melted cheese.', 'Snacks', 55.00, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400&q=80', TRUE, 6, TRUE),
(4, 'Hot Samosa with Mint Chutney (2 pcs)', 'Crisp flaky pastry filled with savory spiced peas and potatoes.', 'Snacks', 30.00, 'https://images.unsplash.com/photo-1601050690597-df0568f70950?w=400&q=80', TRUE, 3, TRUE),
(5, 'Paneer Tikka Roll', 'Smoky grilled cottage cheese cubes wrapped in warm paratha with sliced onions and mint yogurt sauce.', 'Snacks', 80.00, 'https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?w=400&q=80', TRUE, 8, TRUE),
(6, 'Deluxe North Indian Thali', 'Complete meal with 2 rotis, Jeera rice, Dal tadka, Paneer butter masala, raita & gulab jamun.', 'Meals', 140.00, 'https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?w=400&q=80', TRUE, 10, TRUE),
(7, 'Veg Fried Rice with Manchurian', 'Wok-tossed aromatic basmati rice with crunchy veggies and 4 veg manchurian dumplings in gravy.', 'Meals', 110.00, 'https://images.unsplash.com/photo-1585032226651-759b368d7246?w=400&q=80', TRUE, 8, TRUE),
(8, 'Chilled Cold Coffee with Ice Cream', 'Creamy rich blended coffee topped with a generous scoop of vanilla ice cream and chocolate drizzle.', 'Beverages', 50.00, 'https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400&q=80', TRUE, 3, TRUE),
(9, 'Masala Chai', 'Authentic Indian spiced milk tea brewed with crushed ginger, cardamom and cloves.', 'Beverages', 15.00, 'https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=400&q=80', TRUE, 2, TRUE),
(10, 'Fresh Lime Soda', 'Zesty fresh lemon juice sparkling with club soda, served sweet or salted as per your preference.', 'Beverages', 35.00, 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=400&q=80', TRUE, 2, TRUE),
(11, 'Hot Gulab Jamun (2 pcs)', 'Warm melt-in-mouth cottage cheese dumplings soaked in rose-flavored saffron sugar syrup.', 'Desserts', 40.00, 'https://images.unsplash.com/photo-1589119908995-c6837fa14d48?w=400&q=80', TRUE, 2, TRUE),
(12, 'Cheesy Veg Burger (Currently Sold Out)', 'Sesame seed bun loaded with crispy vegetable patty, cheddar cheese slice and herb mayo.', 'Snacks', 65.00, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&q=80', FALSE, 7, TRUE);
