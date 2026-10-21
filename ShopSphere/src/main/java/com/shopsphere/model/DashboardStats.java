package com.shopsphere.model;

import java.math.BigDecimal;
import java.util.List;

/** Aggregated figures shown on the administration dashboard. */
public class DashboardStats {

    private long totalCustomers;
    private long totalProducts;
    private long activeProducts;
    private long totalOrders;
    private long pendingOrders;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal averageOrderValue = BigDecimal.ZERO;
    private long lowStockProducts;
    private long outOfStockProducts;
    private List<Order> recentOrders = List.of();
    private List<Product> lowStockItems = List.of();
    private List<Order> topOrders = List.of();
    private String inventoryService = "Local database inventory";

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(long activeProducts) {
        this.activeProducts = activeProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue == null ? BigDecimal.ZERO : totalRevenue;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue == null ? BigDecimal.ZERO : averageOrderValue;
    }

    public long getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(long lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public long getOutOfStockProducts() {
        return outOfStockProducts;
    }

    public void setOutOfStockProducts(long outOfStockProducts) {
        this.outOfStockProducts = outOfStockProducts;
    }

    public List<Order> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<Order> recentOrders) {
        this.recentOrders = recentOrders == null ? List.of() : recentOrders;
    }

    public List<Product> getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(List<Product> lowStockItems) {
        this.lowStockItems = lowStockItems == null ? List.of() : lowStockItems;
    }

    public List<Order> getTopOrders() {
        return topOrders;
    }

    public void setTopOrders(List<Order> topOrders) {
        this.topOrders = topOrders == null ? List.of() : topOrders;
    }

    /** Which inventory service the application is connected to. */
    public String getInventoryService() {
        return inventoryService;
    }

    public void setInventoryService(String inventoryService) {
        this.inventoryService = inventoryService;
    }
}
