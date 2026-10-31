package com.smartcanteen.service;

import com.smartcanteen.dto.CreateOrderRequest;
import com.smartcanteen.dto.OrderItemRequest;
import com.smartcanteen.dto.OrderResponseDto;
import com.smartcanteen.dto.OrderStatusUpdateRequest;
import com.smartcanteen.exception.InvalidOrderException;
import com.smartcanteen.exception.ResourceNotFoundException;
import com.smartcanteen.model.*;
import com.smartcanteen.repository.MenuItemRepository;
import com.smartcanteen.repository.OrderRepository;
import com.smartcanteen.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private QueueEstimatorService queueEstimatorService;

    @InjectMocks
    private OrderService orderService;

    private MenuItem samosa;
    private MenuItem coffee;
    private MenuItem soldOutBurger;

    @BeforeEach
    void setUp() {
        samosa = new MenuItem("Hot Samosa", "Crisp samosa", "Snacks", new BigDecimal("30.00"), "", true, 3);
        samosa.setId(1L);

        coffee = new MenuItem("Cold Coffee", "Chilled coffee", "Beverages", new BigDecimal("50.00"), "", true, 3);
        coffee.setId(2L);

        soldOutBurger = new MenuItem("Veg Burger", "Cheesy burger", "Snacks", new BigDecimal("65.00"), "", false, 5);
        soldOutBurger.setId(3L);
    }

    @Test
    @DisplayName("Successfully place order: server computes total and creates simulated demo payment")
    void testCreateOrderSuccess() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Rohan Mehta");
        request.setCustomerPhone("9876543210");
        request.setCustomerRollNumber("CS2026-099");
        request.setPaymentMethod(PaymentMethod.DEMO_ONLINE);
        request.setItems(Collections.singletonList(new OrderItemRequest(1L, 2))); // 2 x 30 = 60.00

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(samosa));
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(10L);
            return o;
        });
        when(queueEstimatorService.calculateOrderBasePrepTime(anyList())).thenReturn(5);
        when(queueEstimatorService.estimateQueueStatus(any(Order.class))).thenReturn(
                new com.smartcanteen.dto.QueueStatusDto("SC-TEST", OrderStatus.PLACED, 1, 0, 5, "You are #1")
        );

        OrderResponseDto response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("Rohan Mehta", response.getCustomerName());
        assertEquals(new BigDecimal("60.00"), response.getTotalAmount(), "Total must be 2 * 30.00 = 60.00");
        assertEquals(OrderStatus.PLACED, response.getStatus());
        assertEquals(PaymentMethod.DEMO_ONLINE, response.getPaymentMethod());
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertTrue(response.isSimulatedPayment(), "Demo payment should be marked as simulated");
        assertNotNull(response.getTransactionReference());
        assertTrue(response.getTransactionReference().startsWith("SIM-UPI-"));
    }

    @Test
    @DisplayName("Reject empty order request")
    void testCreateOrderEmptyCart() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Rohan Mehta");
        request.setCustomerPhone("9876543210");
        request.setItems(Collections.emptyList());

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.createOrder(request));
        assertTrue(ex.getMessage().contains("Cannot place an empty order"));
    }

    @Test
    @DisplayName("Reject order when food item is out of stock")
    void testCreateOrderUnavailableItem() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Sneha Gupta");
        request.setCustomerPhone("9876543211");
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setItems(Collections.singletonList(new OrderItemRequest(3L, 1)));

        when(menuItemRepository.findById(3L)).thenReturn(Optional.of(soldOutBurger));

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.createOrder(request));
        assertTrue(ex.getMessage().contains("OUT OF STOCK"));
    }

    @Test
    @DisplayName("Reject order with invalid quantity (e.g. 0)")
    void testCreateOrderInvalidQuantity() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Sneha Gupta");
        request.setCustomerPhone("9876543211");
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setItems(Collections.singletonList(new OrderItemRequest(1L, 0)));

        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.createOrder(request));
        assertTrue(ex.getMessage().contains("quantity must be at least 1"));
    }

    @Test
    @DisplayName("Transition order status and auto-confirm cash payment upon completion")
    void testUpdateOrderStatus() {
        Order order = new Order("SC-20261020-001", "Karan", "9876543210", "",
                new BigDecimal("50.00"), PaymentMethod.CASH, "");
        order.setId(5L);
        Payment payment = new Payment(order, PaymentMethod.CASH, new BigDecimal("50.00"), PaymentStatus.PENDING, "CASH-1", false);
        order.setPayment(payment);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(queueEstimatorService.estimateQueueStatus(any(Order.class))).thenReturn(
                new com.smartcanteen.dto.QueueStatusDto("SC-20261020-001", OrderStatus.COMPLETED, -1, 0, 0, "Completed")
        );

        OrderResponseDto updated = orderService.updateOrderStatus(5L, new OrderStatusUpdateRequest(OrderStatus.COMPLETED));

        assertEquals(OrderStatus.COMPLETED, updated.getStatus());
        assertEquals(PaymentStatus.SUCCESS, updated.getPaymentStatus(), "Cash payment should auto-confirm on completion");
    }

    @Test
    @DisplayName("Prevent changing status of an already completed order")
    void testPreventStatusChangeOnCompletedOrder() {
        Order order = new Order("SC-COMP-01", "Karan", "9876543210", "",
                new BigDecimal("50.00"), PaymentMethod.CASH, "");
        order.setId(6L);
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));

        InvalidOrderException ex = assertThrows(InvalidOrderException.class,
                () -> orderService.updateOrderStatus(6L, new OrderStatusUpdateRequest(OrderStatus.PREPARING)));
        assertTrue(ex.getMessage().contains("Cannot change status of an already completed order"));
    }
}
