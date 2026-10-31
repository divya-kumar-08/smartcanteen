package com.smartcanteen.controller;

import com.smartcanteen.dto.CreateOrderRequest;
import com.smartcanteen.dto.OrderResponseDto;
import com.smartcanteen.dto.QueueStatusDto;
import com.smartcanteen.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /api/orders
     * Places a new canteen food order.
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponseDto response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/orders/{orderNumber}
     * Retrieves complete order details.
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponseDto> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByOrderNumber(orderNumber));
    }

    /**
     * GET /api/orders/{orderNumber}/queue
     * Retrieves real-time queue position and waiting time estimation.
     */
    @GetMapping("/{orderNumber}/queue")
    public ResponseEntity<QueueStatusDto> getQueueStatus(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getQueueStatus(orderNumber));
    }

    /**
     * GET /api/orders/history?phone=... or ?rollNumber=...
     * Retrieves student order history.
     */
    @GetMapping("/history")
    public ResponseEntity<List<OrderResponseDto>> getOrderHistory(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String rollNumber) {
        return ResponseEntity.ok(orderService.getOrderHistory(phone, rollNumber));
    }
}
