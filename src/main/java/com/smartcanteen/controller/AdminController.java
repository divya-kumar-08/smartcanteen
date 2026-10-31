package com.smartcanteen.controller;

import com.smartcanteen.dto.AdminStatsDto;
import com.smartcanteen.dto.MenuItemRequest;
import com.smartcanteen.dto.OrderResponseDto;
import com.smartcanteen.dto.OrderStatusUpdateRequest;
import com.smartcanteen.model.MenuItem;
import com.smartcanteen.model.OrderStatus;
import com.smartcanteen.service.MenuItemService;
import com.smartcanteen.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final OrderService orderService;
    private final MenuItemService menuItemService;

    public AdminController(OrderService orderService, MenuItemService menuItemService) {
        this.orderService = orderService;
        this.menuItemService = menuItemService;
    }

    /**
     * GET /api/admin/stats
     * Key dashboard metrics (revenue, active orders, completed count).
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(orderService.getAdminStats());
    }

    /**
     * GET /api/admin/orders
     * All orders, optionally filtered by status (PLACED, PREPARING, READY, COMPLETED, CANCELLED).
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders(@RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin(status));
    }

    /**
     * GET /api/admin/orders/active
     * Real-time Kitchen Display System list (orders awaiting preparation or ready).
     */
    @GetMapping("/orders/active")
    public ResponseEntity<List<OrderResponseDto>> getActiveKitchenOrders() {
        return ResponseEntity.ok(orderService.getActiveOrdersForKitchen());
    }

    /**
     * PUT /api/admin/orders/{id}/status
     * Updates an order status (e.g. PLACED -> PREPARING -> READY -> COMPLETED).
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    /**
     * GET /api/admin/menu
     * All menu items including hidden/inactive ones.
     */
    @GetMapping("/menu")
    public ResponseEntity<List<MenuItem>> getAllMenuItems() {
        return ResponseEntity.ok(menuItemService.getAllMenuItemsForAdmin());
    }

    /**
     * POST /api/admin/menu
     * Adds a new menu item.
     */
    @PostMapping("/menu")
    public ResponseEntity<MenuItem> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.createMenuItem(request));
    }

    /**
     * PUT /api/admin/menu/{id}
     * Updates an existing menu item.
     */
    @PutMapping("/menu/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, request));
    }

    /**
     * PATCH /api/admin/menu/{id}/toggle-availability
     * Fast toggle between Available and Out of Stock.
     */
    @PatchMapping("/menu/{id}/toggle-availability")
    public ResponseEntity<MenuItem> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.toggleAvailability(id));
    }

    /**
     * PATCH /api/admin/menu/{id}/toggle-active
     * Soft delete or reactivate a menu item.
     */
    @PatchMapping("/menu/{id}/toggle-active")
    public ResponseEntity<MenuItem> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.toggleActive(id));
    }
}
