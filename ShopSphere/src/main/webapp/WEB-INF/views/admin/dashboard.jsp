<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
    <div>
        <h1 class="h3 mb-1">Dashboard</h1>
        <p class="text-muted mb-0">An overview of the catalogue, the customers and the orders.</p>
    </div>
    <a class="btn ss-btn-primary" href="${ctx}/admin/products/new">Add product</a>
</div>

<div class="row g-3 mb-4">
    <div class="col-6 col-lg-3">
        <div class="ss-kpi">
            <div class="d-flex justify-content-between align-items-start">
                <span class="ss-kpi-label">Revenue</span>
                <span class="ss-kpi-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                         stroke-linecap="round"><path d="M12 1v22M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                </span>
            </div>
            <div class="ss-kpi-value">
                <c:out value="${currencySymbol}"/><c:out value="${stats.totalRevenue}"/>
            </div>
            <span class="small text-muted">Average order <c:out value="${currencySymbol}"/><c:out value="${stats.averageOrderValue}"/></span>
        </div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="ss-kpi">
            <div class="d-flex justify-content-between align-items-start">
                <span class="ss-kpi-label">Orders</span>
                <span class="ss-kpi-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                         stroke-linecap="round"><path d="M6 2h12l2 5H4z"/><path d="M4 7v13a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7"/></svg>
                </span>
            </div>
            <div class="ss-kpi-value"><c:out value="${stats.totalOrders}"/></div>
            <span class="small text-muted"><c:out value="${stats.pendingOrders}"/> awaiting action</span>
        </div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="ss-kpi">
            <div class="d-flex justify-content-between align-items-start">
                <span class="ss-kpi-label">Products</span>
                <span class="ss-kpi-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                         stroke-linecap="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><path d="M3 6h18"/></svg>
                </span>
            </div>
            <div class="ss-kpi-value"><c:out value="${stats.activeProducts}"/></div>
            <span class="small text-muted"><c:out value="${stats.totalProducts}"/> in total</span>
        </div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="ss-kpi">
            <div class="d-flex justify-content-between align-items-start">
                <span class="ss-kpi-label">Customers</span>
                <span class="ss-kpi-icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                         stroke-linecap="round"><path d="M17 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9.5" cy="7" r="4"/></svg>
                </span>
            </div>
            <div class="ss-kpi-value"><c:out value="${stats.totalCustomers}"/></div>
            <span class="small text-muted">registered accounts</span>
        </div>
    </div>
</div>

<div class="row g-4">
    <div class="col-xl-8">
        <div class="ss-card mb-4">
            <div class="d-flex justify-content-between align-items-center p-3 border-bottom">
                <h2 class="h6 mb-0">Recent orders</h2>
                <a class="btn ss-btn-ghost btn-sm" href="${ctx}/admin/orders">View all</a>
            </div>
            <div class="table-responsive">
                <table class="table ss-table">
                    <thead>
                    <tr>
                        <th scope="col">Order</th>
                        <th scope="col">Customer</th>
                        <th scope="col">Total</th>
                        <th scope="col">Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty stats.recentOrders}">
                            <tr><td colspan="4" class="text-center text-muted py-4">No orders yet</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${stats.recentOrders}" var="order">
                                <tr>
                                    <td class="fw-semibold">
                                        <a href="${ctx}/admin/orders?id=${order.id}"><c:out value="${order.orderNumber}"/></a>
                                    </td>
                                    <td class="text-muted"><c:out value="${order.customerName}"/></td>
                                    <td><c:out value="${currencySymbol}"/><c:out value="${order.total}"/></td>
                                    <td><span class="ss-status ${order.status.badgeClass}"><c:out value="${order.status.label}"/></span></td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="ss-card">
            <div class="d-flex justify-content-between align-items-center p-3 border-bottom">
                <h2 class="h6 mb-0">Largest orders</h2>
            </div>
            <div class="table-responsive">
                <table class="table ss-table">
                    <thead>
                    <tr>
                        <th scope="col">Order</th>
                        <th scope="col">Customer</th>
                        <th scope="col">Items</th>
                        <th scope="col">Total</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty stats.topOrders}">
                            <tr><td colspan="4" class="text-center text-muted py-4">No orders yet</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${stats.topOrders}" var="order">
                                <tr>
                                    <td class="fw-semibold">
                                        <a href="${ctx}/admin/orders?id=${order.id}"><c:out value="${order.orderNumber}"/></a>
                                    </td>
                                    <td class="text-muted"><c:out value="${order.customerName}"/></td>
                                    <td class="text-muted"><c:out value="${order.itemCount}"/></td>
                                    <td><c:out value="${currencySymbol}"/><c:out value="${order.total}"/></td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="col-xl-4">
        <div class="ss-card p-3 mb-4">
            <h2 class="h6 mb-3">Inventory alerts</h2>
            <div class="d-flex gap-3 mb-3">
                <div class="flex-fill ss-card p-3 text-center">
                    <div class="ss-kpi-value" style="font-size:1.5rem;color:#b45309">
                        <c:out value="${stats.lowStockProducts}"/>
                    </div>
                    <span class="small text-muted">Low stock</span>
                </div>
                <div class="flex-fill ss-card p-3 text-center">
                    <div class="ss-kpi-value" style="font-size:1.5rem;color:#b91c1c">
                        <c:out value="${stats.outOfStockProducts}"/>
                    </div>
                    <span class="small text-muted">Out of stock</span>
                </div>
            </div>
            <c:forEach items="${stats.lowStockItems}" var="product">
                <div class="d-flex justify-content-between align-items-center border-top py-2">
                    <span class="small"><c:out value="${product.name}"/></span>
                    <span class="ss-chip ${product.outOfStock ? 'ss-chip-danger' : 'ss-chip-warning'}">
                        <c:out value="${product.stock}"/> left
                    </span>
                </div>
            </c:forEach>
            <c:if test="${empty stats.lowStockItems}">
                <p class="small text-muted mb-0">Every product is comfortably in stock.</p>
            </c:if>
        </div>

        <div class="ss-card p-3">
            <h2 class="h6 mb-3">Shortcuts</h2>
            <a class="d-block py-2 border-bottom" href="${ctx}/admin/products">Manage products</a>
            <a class="d-block py-2 border-bottom" href="${ctx}/admin/categories">Manage categories</a>
            <a class="d-block py-2 border-bottom" href="${ctx}/admin/orders">Process orders</a>
            <a class="d-block py-2" href="${ctx}/admin/customers">View customers</a>
        </div>
        <div class="ss-card p-3">
            <h2 class="h6 mb-3">Inventory service</h2>
            <p class="small text-muted mb-2">
                The stock is served by the following inventory implementation. The database
                transaction during checkout stays the authority, so a remote service can
                never oversell.
            </p>
            <span class="ss-chip ss-chip-brand"><c:out value="${stats.inventoryService}"/></span>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
