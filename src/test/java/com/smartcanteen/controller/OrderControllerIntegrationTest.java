package com.smartcanteen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcanteen.dto.CreateOrderRequest;
import com.smartcanteen.dto.OrderItemRequest;
import com.smartcanteen.model.MenuItem;
import com.smartcanteen.model.PaymentMethod;
import com.smartcanteen.repository.MenuItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Test
    @DisplayName("GET /api/menu returns active menu items")
    void testGetMenu() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("GET /api/menu/categories returns distinct categories")
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/menu/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("Breakfast")));
    }

    @Test
    @DisplayName("POST /api/orders creates order and returns 201 Created")
    void testPlaceOrderIntegration() throws Exception {
        MenuItem item = menuItemRepository.findByActiveTrue().stream()
                .filter(MenuItem::isAvailable)
                .findFirst()
                .orElseThrow();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Integration Student");
        request.setCustomerPhone("9876543210");
        request.setCustomerRollNumber("CS2026-INT");
        request.setPaymentMethod(PaymentMethod.DEMO_ONLINE);
        request.setItems(Collections.singletonList(new OrderItemRequest(item.getId(), 1)));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber", notNullValue()))
                .andExpect(jsonPath("$.customerName", is("Integration Student")))
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.queuePosition", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.estimatedWaitMinutes", greaterThan(0)));
    }

    @Test
    @DisplayName("POST /api/orders fails with 400 when cart is empty")
    void testPlaceOrderEmptyValidation() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Integration Student");
        request.setCustomerPhone("9876543210");
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setItems(Collections.emptyList());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/admin/stats returns 200 OK and metrics")
    void testGetAdminStats() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMenuItemsCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalOrdersCount", greaterThanOrEqualTo(0)));
    }
}
