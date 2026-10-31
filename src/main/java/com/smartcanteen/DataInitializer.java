package com.smartcanteen;

import com.smartcanteen.model.*;
import com.smartcanteen.repository.MenuItemRepository;
import com.smartcanteen.repository.OrderRepository;
import com.smartcanteen.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MenuItemRepository menuItemRepository;
    private final StudentRepository studentRepository;
    private final OrderRepository orderRepository;

    public DataInitializer(MenuItemRepository menuItemRepository,
                           StudentRepository studentRepository,
                           OrderRepository orderRepository) {
        this.menuItemRepository = menuItemRepository;
        this.studentRepository = studentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedStudents();
        seedMenuItems();
        seedInitialDemoOrder();
    }

    private void seedStudents() {
        if (studentRepository.count() == 0) {
            studentRepository.save(new Student("CS2026-042", "Rahul Sharma", "rahul@college.edu", "9876543210"));
            studentRepository.save(new Student("EC2026-105", "Priya Patel", "priya@college.edu", "9876543211"));
            studentRepository.save(new Student("ME2026-077", "Ananya Verma", "ananya@college.edu", "9876543212"));
            System.out.println(">>> Sample Students Seeded.");
        }
    }

    private void seedMenuItems() {
        if (menuItemRepository.count() == 0) {
            List<MenuItem> items = Arrays.asList(
                new MenuItem(
                    "Crispy Masala Dosa",
                    "Golden thin rice crepe filled with spiced potato masala, served with coconut chutney & sambar.",
                    "Breakfast",
                    new BigDecimal("60.00"),
                    "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?w=400&q=80",
                    true,
                    7
                ),
                new MenuItem(
                    "Steamed Idli Sambar (3 pcs)",
                    "Soft fluffy steamed rice cakes served with hot lentil vegetable stew and fresh coconut chutney.",
                    "Breakfast",
                    new BigDecimal("40.00"),
                    "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=400&q=80",
                    true,
                    4
                ),
                new MenuItem(
                    "Veg Grilled Cheese Sandwich",
                    "Toasted triple-decker sandwich with bell peppers, onions, tomatoes and melted cheese.",
                    "Snacks",
                    new BigDecimal("55.00"),
                    "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400&q=80",
                    true,
                    6
                ),
                new MenuItem(
                    "Hot Samosa with Mint Chutney (2 pcs)",
                    "Crisp flaky pastry filled with savory spiced peas and potatoes.",
                    "Snacks",
                    new BigDecimal("30.00"),
                    "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=400&q=80",
                    true,
                    3
                ),
                new MenuItem(
                    "Paneer Tikka Roll",
                    "Smoky grilled cottage cheese cubes wrapped in warm paratha with sliced onions and mint yogurt sauce.",
                    "Snacks",
                    new BigDecimal("80.00"),
                    "https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?w=400&q=80",
                    true,
                    8
                ),
                new MenuItem(
                    "Deluxe North Indian Thali",
                    "Complete meal with 2 rotis, Jeera rice, Dal tadka, Paneer butter masala, raita & gulab jamun.",
                    "Meals",
                    new BigDecimal("140.00"),
                    "https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?w=400&q=80",
                    true,
                    10
                ),
                new MenuItem(
                    "Veg Fried Rice with Manchurian",
                    "Wok-tossed aromatic basmati rice with crunchy veggies and 4 veg manchurian dumplings in gravy.",
                    "Meals",
                    new BigDecimal("110.00"),
                    "https://images.unsplash.com/photo-1585032226651-759b368d7246?w=400&q=80",
                    true,
                    8
                ),
                new MenuItem(
                    "Chilled Cold Coffee with Ice Cream",
                    "Creamy rich blended coffee topped with a generous scoop of vanilla ice cream and chocolate drizzle.",
                    "Beverages",
                    new BigDecimal("50.00"),
                    "https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=400&q=80",
                    true,
                    3
                ),
                new MenuItem(
                    "Masala Chai",
                    "Authentic Indian spiced milk tea brewed with crushed ginger, cardamom and cloves.",
                    "Beverages",
                    new BigDecimal("15.00"),
                    "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=400&q=80",
                    true,
                    2
                ),
                new MenuItem(
                    "Hot Gulab Jamun (2 pcs)",
                    "Warm melt-in-mouth cottage cheese dumplings soaked in rose-flavored saffron sugar syrup.",
                    "Desserts",
                    new BigDecimal("40.00"),
                    "https://images.unsplash.com/photo-1589119908995-c6837fa14d48?w=400&q=80",
                    true,
                    2
                ),
                new MenuItem(
                    "Cheesy Veg Burger (Out of Stock Demo)",
                    "Sesame seed bun loaded with crispy vegetable patty, cheddar cheese slice and herb mayo.",
                    "Snacks",
                    new BigDecimal("65.00"),
                    "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&q=80",
                    false, // Intentionally Out of Stock to demonstrate stock validation!
                    7
                )
            );
            menuItemRepository.saveAll(items);
            System.out.println(">>> Sample Canteen Menu Items Seeded.");
        }
    }

    private void seedInitialDemoOrder() {
        if (orderRepository.count() == 0) {
            MenuItem dosa = menuItemRepository.findAll().stream()
                .filter(m -> m.getName().contains("Dosa"))
                .findFirst().orElse(null);

            if (dosa != null) {
                Order demoOrder = new Order(
                    "SC-DEMO-0001",
                    "Kavya Reddy",
                    "9876543299",
                    "CS2026-015",
                    dosa.getPrice(),
                    PaymentMethod.DEMO_ONLINE,
                    "Extra chutney please"
                );
                demoOrder.setStatus(OrderStatus.PREPARING);
                demoOrder.setPaymentStatus(PaymentStatus.SUCCESS);
                demoOrder.setEstimatedWaitMinutes(6);

                OrderItem item = new OrderItem(demoOrder, dosa, dosa.getName(), dosa.getPrice(), 1, dosa.getPrice());
                demoOrder.addItem(item);

                Payment payment = new Payment(
                    demoOrder,
                    PaymentMethod.DEMO_ONLINE,
                    dosa.getPrice(),
                    PaymentStatus.SUCCESS,
                    "SIM-UPI-DEMO01",
                    true
                );
                demoOrder.setPayment(payment);

                orderRepository.save(demoOrder);
                System.out.println(">>> Demo Active Order Seeded (Order #SC-DEMO-0001 in PREPARING status).");
            }
        }
    }
}
