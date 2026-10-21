package com.shopsphere.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A payment that belongs to an order.
 *
 * <p>The reference implementation of this project stored the gateway answer of the
 * payment provider here. This project uses a clearly labelled demo payment, so
 * {@code transactionId} and {@code gatewayResponse} describe the simulated
 * transaction and never contain real payment data.
 */
public class Payment {

    private long id;
    private long orderId;
    private String paymentMethod = "COD";
    private String transactionId;
    private BigDecimal amount = BigDecimal.ZERO;
    private String paymentStatus = "PENDING";
    private String gatewayResponse;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount == null ? BigDecimal.ZERO : amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
