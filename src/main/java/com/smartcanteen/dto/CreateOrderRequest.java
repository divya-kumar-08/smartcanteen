package com.smartcanteen.dto;

import com.smartcanteen.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreateOrderRequest {

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    @Size(min = 7, max = 20, message = "Valid phone number is required")
    private String customerPhone;

    private String customerRollNumber;

    @NotEmpty(message = "Cart cannot be empty. Please select at least one item.")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Payment method is required (CASH or DEMO_ONLINE)")
    private PaymentMethod paymentMethod;

    private String specialInstructions;

    public CreateOrderRequest() {
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

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
}
