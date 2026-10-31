package com.smartcanteen.service;

import com.smartcanteen.dto.QueueStatusDto;
import com.smartcanteen.model.Order;
import com.smartcanteen.model.OrderItem;
import com.smartcanteen.model.OrderStatus;
import com.smartcanteen.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class QueueEstimatorService {

    private final OrderRepository orderRepository;

    @Value("${canteen.queue.kitchen-concurrency:2}")
    private int kitchenConcurrency;

    @Value("${canteen.queue.default-item-prep-minutes:5}")
    private int defaultItemPrepMinutes;

    @Value("${canteen.queue.min-wait-minutes:3}")
    private int minWaitMinutes;

    public QueueEstimatorService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Calculates the preparation time for a specific order based on its ordered items and quantities.
     */
    public int calculateOrderBasePrepTime(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return minWaitMinutes;
        }

        int totalPrepTime = 0;
        for (OrderItem item : items) {
            int itemPrepTime = defaultItemPrepMinutes;
            if (item.getMenuItem() != null && item.getMenuItem().getPreparationTimeMinutes() > 0) {
                itemPrepTime = item.getMenuItem().getPreparationTimeMinutes();
            }
            // Add prep time: Base item prep time plus a small increment for each additional quantity
            totalPrepTime += itemPrepTime + (Math.max(0, item.getQuantity() - 1) * 2);
        }

        return Math.max(minWaitMinutes, totalPrepTime);
    }

    /**
     * Computes the live queue position and estimated waiting time for an existing order.
     */
    public QueueStatusDto estimateQueueStatus(Order order) {
        OrderStatus status = order.getStatus();

        // 1. Ready, Completed or Cancelled orders
        if (status == OrderStatus.READY) {
            return new QueueStatusDto(
                order.getOrderNumber(),
                status,
                0, // 0 indicates ready for pickup!
                0,
                0,
                "Your order is ready for pickup at the canteen counter! Please show your order number."
            );
        }

        if (status == OrderStatus.COMPLETED) {
            return new QueueStatusDto(
                order.getOrderNumber(),
                status,
                -1,
                0,
                0,
                "This order has been completed and collected."
            );
        }

        if (status == OrderStatus.CANCELLED) {
            return new QueueStatusDto(
                order.getOrderNumber(),
                status,
                -1,
                0,
                0,
                "This order has been cancelled."
            );
        }

        // 2. Active orders ahead (Orders with PLACED or PREPARING placed before this order's timestamp)
        List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.PLACED, OrderStatus.PREPARING);
        List<Order> activeOrdersAhead = orderRepository.findByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
            activeStatuses, order.getCreatedAt()
        );

        int ordersAheadCount = activeOrdersAhead.size();
        int queuePosition = ordersAheadCount + 1;

        // 3. Compute ahead backlog preparation time
        int aheadBacklogMinutes = 0;
        for (Order aheadOrder : activeOrdersAhead) {
            if (aheadOrder.getStatus() == OrderStatus.PREPARING) {
                // If already in preparation, half of its estimated time remains
                aheadBacklogMinutes += Math.max(2, aheadOrder.getEstimatedWaitMinutes() / 2);
            } else {
                aheadBacklogMinutes += Math.max(3, aheadOrder.getEstimatedWaitMinutes());
            }
        }

        // Concurrency factor (e.g. 2 parallel kitchen cook stations)
        int effectiveConcurrency = Math.max(1, kitchenConcurrency);
        int queueDelay = (int) Math.ceil((double) aheadBacklogMinutes / effectiveConcurrency);

        // Current order base preparation time
        int myPrepTime = calculateOrderBasePrepTime(order.getItems());

        int totalEstimatedWait;
        if (status == OrderStatus.PREPARING) {
            // Already on the stove: remaining time is based on myPrepTime with queue delay already past
            totalEstimatedWait = Math.max(minWaitMinutes, myPrepTime / 2);
        } else {
            totalEstimatedWait = Math.max(minWaitMinutes, myPrepTime + queueDelay);
        }

        // Build human-friendly explanation breakdown
        String explanation;
        if (status == OrderStatus.PREPARING) {
            explanation = String.format(
                "Your order is currently being prepared in the kitchen (Position #%d). Estimated ~%d mins remaining.",
                queuePosition, totalEstimatedWait
            );
        } else if (ordersAheadCount == 0) {
            explanation = String.format(
                "You are #1 in line! Kitchen is preparing your order next. Estimated time: ~%d mins.",
                totalEstimatedWait
            );
        } else {
            explanation = String.format(
                "You are #%d in the queue. %d active order(s) ahead of you. Estimated wait: ~%d mins (calculated across %d active kitchen stations).",
                queuePosition, ordersAheadCount, totalEstimatedWait, effectiveConcurrency
            );
        }

        return new QueueStatusDto(
            order.getOrderNumber(),
            status,
            queuePosition,
            ordersAheadCount,
            totalEstimatedWait,
            explanation
        );
    }
}
