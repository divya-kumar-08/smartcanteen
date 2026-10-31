package com.smartcanteen.dto;

import java.math.BigDecimal;

public class OrderItemResponseDto {
    private Long id;
    private Long menuItemId;
    private String itemName;
    private BigDecimal itemPrice;
    private int quantity;
    private BigDecimal subtotal;

    public OrderItemResponseDto() {
    }

    public OrderItemResponseDto(Long id, Long menuItemId, String itemName, BigDecimal itemPrice, int quantity, BigDecimal subtotal) {
        this.id = id;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
