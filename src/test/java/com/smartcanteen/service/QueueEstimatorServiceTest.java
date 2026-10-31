package com.smartcanteen.service;

import com.smartcanteen.dto.QueueStatusDto;
import com.smartcanteen.model.*;
import com.smartcanteen.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueEstimatorServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private QueueEstimatorService queueEstimatorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(queueEstimatorService, "kitchenConcurrency", 2);
        ReflectionTestUtils.setField(queueEstimatorService, "defaultItemPrepMinutes", 5);
        ReflectionTestUtils.setField(queueEstimatorService, "minWaitMinutes", 3);
    }

    @Test
    @DisplayName("Calculate base prep time for order with items")
    void testCalculateOrderBasePrepTime() {
        MenuItem item1 = new MenuItem("Dosa", "", "Breakfast", new BigDecimal("60.00"), "", true, 6);
        MenuItem item2 = new MenuItem("Coffee", "", "Beverages", new BigDecimal("30.00"), "", true, 2);

        Order dummyOrder = new Order();
        OrderItem oi1 = new OrderItem(dummyOrder, item1, "Dosa", new BigDecimal("60.00"), 2, new BigDecimal("120.00")); // 6 + (2-1)*2 = 8
        OrderItem oi2 = new OrderItem(dummyOrder, item2, "Coffee", new BigDecimal("30.00"), 1, new BigDecimal("30.00")); // 2

        int prepTime = queueEstimatorService.calculateOrderBasePrepTime(Arrays.asList(oi1, oi2));
        assertEquals(10, prepTime, "Total prep time should be 8 + 2 = 10 mins");
    }

    @Test
    @DisplayName("Queue position is 1 and wait time is minimum when no active orders are ahead")
    void testQueuePositionWhenNoOrdersAhead() {
        Order currentOrder = new Order("SC-100", "Alice", "9876543210", "CS-01",
                new BigDecimal("50.00"), PaymentMethod.CASH, "");
        currentOrder.setStatus(OrderStatus.PLACED);
        currentOrder.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
                eq(Arrays.asList(OrderStatus.PLACED, OrderStatus.PREPARING)), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        QueueStatusDto statusDto = queueEstimatorService.estimateQueueStatus(currentOrder);

        assertNotNull(statusDto);
        assertEquals(1, statusDto.getQueuePosition(), "Should be #1 in line");
        assertEquals(0, statusDto.getOrdersAheadCount(), "Orders ahead should be 0");
        assertTrue(statusDto.getEstimatedWaitMinutes() >= 3, "Wait time should be at least minimum wait");
        assertTrue(statusDto.getCalculationExplanation().contains("#1 in line"));
    }

    @Test
    @DisplayName("Queue position and wait time account for active orders ahead")
    void testQueuePositionWithOrdersAhead() {
        Order currentOrder = new Order("SC-200", "Bob", "9876543211", "CS-02",
                new BigDecimal("60.00"), PaymentMethod.CASH, "");
        currentOrder.setStatus(OrderStatus.PLACED);
        currentOrder.setCreatedAt(LocalDateTime.now());

        // Ahead order 1: PREPARING (estimated wait 8m)
        Order ahead1 = new Order("SC-101", "Charlie", "9876543212", "CS-03",
                new BigDecimal("40.00"), PaymentMethod.CASH, "");
        ahead1.setStatus(OrderStatus.PREPARING);
        ahead1.setEstimatedWaitMinutes(8);

        // Ahead order 2: PLACED (estimated wait 6m)
        Order ahead2 = new Order("SC-102", "David", "9876543213", "CS-04",
                new BigDecimal("50.00"), PaymentMethod.CASH, "");
        ahead2.setStatus(OrderStatus.PLACED);
        ahead2.setEstimatedWaitMinutes(6);

        when(orderRepository.findByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
                eq(Arrays.asList(OrderStatus.PLACED, OrderStatus.PREPARING)), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(ahead1, ahead2));

        QueueStatusDto statusDto = queueEstimatorService.estimateQueueStatus(currentOrder);

        assertEquals(3, statusDto.getQueuePosition(), "Queue position should be 2 ahead + 1 = 3");
        assertEquals(2, statusDto.getOrdersAheadCount(), "Orders ahead should be 2");
        assertTrue(statusDto.getEstimatedWaitMinutes() > 3, "Wait time should include queue delay");
        assertTrue(statusDto.getCalculationExplanation().contains("You are #3 in the queue"));
    }

    @Test
    @DisplayName("Ready orders have queue position 0 and 0 minutes wait")
    void testReadyOrderQueueStatus() {
        Order readyOrder = new Order("SC-300", "Eve", "9876543214", "CS-05",
                new BigDecimal("30.00"), PaymentMethod.CASH, "");
        readyOrder.setStatus(OrderStatus.READY);

        QueueStatusDto statusDto = queueEstimatorService.estimateQueueStatus(readyOrder);

        assertEquals(0, statusDto.getQueuePosition(), "Ready order should have queue position 0");
        assertEquals(0, statusDto.getEstimatedWaitMinutes(), "Ready order wait time should be 0");
        assertTrue(statusDto.getCalculationExplanation().contains("ready for pickup"));
    }

    @Test
    @DisplayName("Completed or cancelled orders have position -1 and active=false")
    void testCompletedOrCancelledQueueStatus() {
        Order compOrder = new Order("SC-400", "Frank", "9876543215", "CS-06",
                new BigDecimal("30.00"), PaymentMethod.CASH, "");
        compOrder.setStatus(OrderStatus.COMPLETED);

        QueueStatusDto compDto = queueEstimatorService.estimateQueueStatus(compOrder);
        assertEquals(-1, compDto.getQueuePosition());
        assertFalse(compDto.isActive());

        Order cancelOrder = new Order("SC-401", "Grace", "9876543216", "CS-07",
                new BigDecimal("30.00"), PaymentMethod.CASH, "");
        cancelOrder.setStatus(OrderStatus.CANCELLED);

        QueueStatusDto cancelDto = queueEstimatorService.estimateQueueStatus(cancelOrder);
        assertEquals(-1, cancelDto.getQueuePosition());
        assertFalse(cancelDto.isActive());
    }
}
