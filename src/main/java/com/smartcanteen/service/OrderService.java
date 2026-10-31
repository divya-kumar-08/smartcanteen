package com.smartcanteen.service;

import com.smartcanteen.dto.*;
import com.smartcanteen.exception.InvalidOrderException;
import com.smartcanteen.exception.ResourceNotFoundException;
import com.smartcanteen.model.*;
import com.smartcanteen.repository.MenuItemRepository;
import com.smartcanteen.repository.OrderRepository;
import com.smartcanteen.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final StudentRepository studentRepository;
    private final QueueEstimatorService queueEstimatorService;

    public OrderService(OrderRepository orderRepository,
                        MenuItemRepository menuItemRepository,
                        StudentRepository studentRepository,
                        QueueEstimatorService queueEstimatorService) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.studentRepository = studentRepository;
        this.queueEstimatorService = queueEstimatorService;
    }

    /**
     * Places a new canteen order with strict server-side validation and pricing calculation.
     */
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Cannot place an empty order. Please select at least one item from the menu.");
        }

        // 1. Validate items, availability, and calculate server-side totals
        BigDecimal orderTotal = BigDecimal.ZERO;
        List<OrderItem> orderItemsToSave = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() < 1) {
                throw new InvalidOrderException("Invalid item quantity: quantity must be at least 1.");
            }

            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + itemReq.getMenuItemId()));

            if (!menuItem.isActive()) {
                throw new InvalidOrderException(String.format("Item '%s' is no longer offered on the menu.", menuItem.getName()));
            }

            if (!menuItem.isAvailable()) {
                throw new InvalidOrderException(String.format("Item '%s' is currently OUT OF STOCK. Please remove it from your cart.", menuItem.getName()));
            }

            // Snapshot price at time of order
            BigDecimal itemPrice = menuItem.getPrice();
            BigDecimal subtotal = itemPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            orderTotal = orderTotal.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setItemName(menuItem.getName());
            orderItem.setItemPrice(itemPrice);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setSubtotal(subtotal);

            orderItemsToSave.add(orderItem);
        }

        // 2. Generate unique order number (e.g. SC-20261020-9421)
        String orderNumber = generateUniqueOrderNumber();

        // 3. Link or create student record if student roll number provided
        Student student = null;
        if (request.getCustomerRollNumber() != null && !request.getCustomerRollNumber().isBlank()) {
            student = studentRepository.findByStudentIdNumber(request.getCustomerRollNumber().trim())
                .orElseGet(() -> {
                    Student newStudent = new Student(
                        request.getCustomerRollNumber().trim(),
                        request.getCustomerName().trim(),
                        "",
                        request.getCustomerPhone().trim()
                    );
                    return studentRepository.save(newStudent);
                });
        }

        // 4. Create and persist Order
        Order order = new Order(
            orderNumber,
            request.getCustomerName().trim(),
            request.getCustomerPhone().trim(),
            request.getCustomerRollNumber() != null ? request.getCustomerRollNumber().trim() : "",
            orderTotal,
            request.getPaymentMethod(),
            request.getSpecialInstructions() != null ? request.getSpecialInstructions().trim() : ""
        );
        order.setStudent(student);

        // Associate items
        for (OrderItem oi : orderItemsToSave) {
            order.addItem(oi);
        }

        // Calculate initial queue delay and estimated preparation time
        int estimatedWait = queueEstimatorService.calculateOrderBasePrepTime(order.getItems());
        order.setEstimatedWaitMinutes(estimatedWait);

        // 5. Handle Payment record
        boolean isDemoOnline = (request.getPaymentMethod() == PaymentMethod.DEMO_ONLINE);
        Payment payment = new Payment(
            order,
            request.getPaymentMethod(),
            orderTotal,
            isDemoOnline ? PaymentStatus.SUCCESS : PaymentStatus.PENDING,
            isDemoOnline ? "SIM-UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                         : "CASH-COUNTER-" + orderNumber,
            isDemoOnline
        );
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        return mapToResponseDto(savedOrder);
    }

    /**
     * Generates a collision-resistant unique order number.
     */
    private String generateUniqueOrderNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderNum;
        do {
            int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999);
            orderNum = String.format("SC-%s-%04d", datePrefix, randomSuffix);
        } while (orderRepository.findByOrderNumber(orderNum).isPresent());
        return orderNum;
    }

    /**
     * Retrieves an order by its unique order number.
     */
    public OrderResponseDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return mapToResponseDto(order);
    }

    /**
     * Retrieves the live queue status for an order.
     */
    public QueueStatusDto getQueueStatus(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return queueEstimatorService.estimateQueueStatus(order);
    }

    /**
     * Retrieves order history by customer phone number or roll number.
     */
    public List<OrderResponseDto> getOrderHistory(String phone, String rollNumber) {
        List<Order> orders = new ArrayList<>();
        if (phone != null && !phone.isBlank()) {
            orders = orderRepository.findByCustomerPhoneOrderByCreatedAtDesc(phone.trim());
        } else if (rollNumber != null && !rollNumber.isBlank()) {
            orders = orderRepository.findByCustomerRollNumberIgnoreCaseOrderByCreatedAtDesc(rollNumber.trim());
        } else {
            // Default to most recent 20 orders if no filter provided
            orders = orderRepository.findAllByOrderByCreatedAtDesc().stream().limit(20).collect(Collectors.toList());
        }
        return orders.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    /**
     * Updates an order's status (Admin / Kitchen Display).
     */
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        // Validate state transitions
        if (currentStatus == OrderStatus.COMPLETED && newStatus != OrderStatus.COMPLETED) {
            throw new InvalidOrderException("Cannot change status of an already completed order.");
        }
        if (currentStatus == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Cannot change status of a cancelled order.");
        }

        order.setStatus(newStatus);

        // If order is marked as COMPLETED, auto-confirm counter cash payment if still pending
        if (newStatus == OrderStatus.COMPLETED && order.getPayment() != null) {
            order.setPaymentStatus(PaymentStatus.SUCCESS);
            order.getPayment().setPaymentStatus(PaymentStatus.SUCCESS);
        }

        if (newStatus == OrderStatus.CANCELLED && order.getPayment() != null && order.getPaymentStatus() == PaymentStatus.PENDING) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.getPayment().setPaymentStatus(PaymentStatus.FAILED);
        }

        Order saved = orderRepository.save(order);
        return mapToResponseDto(saved);
    }

    /**
     * Retrieves all active orders (PLACED or PREPARING or READY) for the Kitchen Display System.
     */
    public List<OrderResponseDto> getActiveOrdersForKitchen() {
        List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.PLACED, OrderStatus.PREPARING, OrderStatus.READY);
        return orderRepository.findByStatusInOrderByCreatedAtAsc(activeStatuses)
            .stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all orders for the admin dashboard.
     */
    public List<OrderResponseDto> getAllOrdersForAdmin(OrderStatus filterStatus) {
        List<Order> orders;
        if (filterStatus != null) {
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(filterStatus);
        } else {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        }
        return orders.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    /**
     * Computes dashboard statistics for the admin portal.
     */
    public AdminStatsDto getAdminStats() {
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalGrossRevenue(orderRepository.calculateTotalGrossRevenue());
        stats.setTotalOrdersCount(orderRepository.count());
        stats.setCompletedOrdersCount(orderRepository.countCompletedOrders());
        stats.setActiveOrdersCount(orderRepository.countActiveOrders(Arrays.asList(OrderStatus.PLACED, OrderStatus.PREPARING)));
        stats.setPreparingOrdersCount(orderRepository.countActiveOrders(Collections.singletonList(OrderStatus.PREPARING)));
        stats.setReadyOrdersCount(orderRepository.countActiveOrders(Collections.singletonList(OrderStatus.READY)));
        stats.setTotalMenuItemsCount(menuItemRepository.count());
        return stats;
    }

    /**
     * Helper mapper method converting entity to DTO.
     */
    public OrderResponseDto mapToResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerPhone(order.getCustomerPhone());
        dto.setCustomerRollNumber(order.getCustomerRollNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setSpecialInstructions(order.getSpecialInstructions());
        dto.setEstimatedWaitMinutes(order.getEstimatedWaitMinutes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        if (order.getPayment() != null) {
            dto.setTransactionReference(order.getPayment().getTransactionReference());
            dto.setSimulatedPayment(order.getPayment().isSimulated());
        }

        // Live Queue Estimation
        QueueStatusDto queueStatus = queueEstimatorService.estimateQueueStatus(order);
        dto.setQueuePosition(queueStatus.getQueuePosition());
        dto.setOrdersAheadCount(queueStatus.getOrdersAheadCount());

        List<OrderItemResponseDto> itemDtos = order.getItems().stream().map(item -> new OrderItemResponseDto(
            item.getId(),
            item.getMenuItem() != null ? item.getMenuItem().getId() : null,
            item.getItemName(),
            item.getItemPrice(),
            item.getQuantity(),
            item.getSubtotal()
        )).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}
