package com.smartcanteen.dto;

import com.smartcanteen.model.OrderStatus;
import com.smartcanteen.model.PaymentMethod;
import com.smartcanteen.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDto {

    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private String customerRollNumber;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String statusDisplayName;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String paymentMethodDisplayName;
    private String transactionReference;
    private boolean simulatedPayment;
    private String specialInstructions;
    private int estimatedWaitMinutes;
    private int queuePosition;
    private int ordersAheadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponseDto> items;

    public OrderResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerRollNumber() {
        return customerRollNumber;
    }

    public void setCustomerRollNumber(String customerRollNumber) {
        this.customerRollNumber = customerRollNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.statusDisplayName = (status != null) ? status.getDisplayName() : null;
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        this.paymentMethodDisplayName = (paymentMethod != null) ? paymentMethod.getDisplayName() : null;
    }

    public String getPaymentMethodDisplayName() {
        return paymentMethodDisplayName;
    }

    public void setPaymentMethodDisplayName(String paymentMethodDisplayName) {
        this.paymentMethodDisplayName = paymentMethodDisplayName;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public boolean isSimulatedPayment() {
        return simulatedPayment;
    }

    public void setSimulatedPayment(boolean simulatedPayment) {
        this.simulatedPayment = simulatedPayment;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public int getOrdersAheadCount() {
        return ordersAheadCount;
    }

    public void setOrdersAheadCount(int ordersAheadCount) {
        this.ordersAheadCount = ordersAheadCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItemResponseDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponseDto> items) {
        this.items = items;
    }
}
