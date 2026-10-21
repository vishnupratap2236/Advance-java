<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<h1 class="h3 mb-1">Orders</h1>
<p class="text-muted mb-4">Search orders and move them through the fulfilment flow.</p>

<div class="ss-card mb-4">
    <div class="p-3 border-bottom">
        <form method="get" action="${ctx}/admin/orders" class="row g-2 align-items-end">
            <div class="col-md-5">
                <label class="ss-form-label" for="admin-order-search">Search</label>
                <input class="form-control" id="admin-order-search" name="q"
                       placeholder="Order number, customer name or email"
                       value="<c:out value='${searchTerm}'/>">
            </div>
            <div class="col-md-4">
                <label class="ss-form-label" for="admin-order-status">Status</label>
                <select class="form-select" id="admin-order-status" name="status" data-auto-submit>
                    <option value="ALL" <c:if test="${statusFilter == 'ALL'}">selected</c:if>>All statuses</option>
                    <c:forEach items="${statuses}" var="status">
                        <option value="${status.name()}" <c:if test="${statusFilter == status.name()}">selected</c:if>>
                            <c:out value="${status.label}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </form>
    </div>

    <div class="table-responsive">
        <table class="table ss-table align-middle">
            <thead>
            <tr>
                <th scope="col">Order</th>
                <th scope="col">Customer</th>
                <th scope="col">Items</th>
                <th scope="col">Total</th>
                <th scope="col">Payment</th>
                <th scope="col">Status</th>
                <th scope="col" class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${orders.isEmpty()}">
                    <tr><td colspan="7" class="text-center text-muted py-4">No orders match your search.</td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${orders.items}" var="order">
                        <tr>
                            <td class="fw-semibold">
                                <a href="${ctx}/admin/orders?id=${order.id}"><c:out value="${order.orderNumber}"/></a>
                            </td>
                            <td>
                                <div><c:out value="${order.customerName}"/></div>
                                <div class="small text-muted"><c:out value="${order.customerEmail}"/></div>
                            </td>
                            <td class="text-muted"><c:out value="${order.itemCount}"/></td>
                            <td><c:out value="${currencySymbol}"/><c:out value="${order.total}"/></td>
                            <td>
                                <div class="small"><c:out value="${order.paymentMethod}"/></div>
                                <span class="ss-chip ss-chip-muted"><c:out value="${order.paymentStatus}"/></span>
                            </td>
                            <td><span class="ss-status ${order.status.badgeClass}"><c:out value="${order.status.label}"/></span></td>
                            <td class="text-end">
                                <a class="btn ss-btn-ghost btn-sm" href="${ctx}/admin/orders?id=${order.id}">Manage</a>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>

<c:if test="${orders.totalPages > 1}">
    <nav aria-label="Order pages">
        <ul class="pagination ss-pagination justify-content-center">
            <li class="page-item ${orders.hasPrevious ? '' : 'disabled'}">
                <a class="page-link" href="${ctx}/admin/orders?status=<c:out value='${statusFilter}'/>&page=${orders.previousPage}">Previous</a>
            </li>
            <c:forEach begin="1" end="${orders.totalPages}" var="pageNo">
                <li class="page-item ${pageNo == orders.page ? 'active' : ''}">
                    <a class="page-link"
                       href="${ctx}/admin/orders?status=<c:out value='${statusFilter}'/>&page=${pageNo}">
                        <c:out value="${pageNo}"/>
                    </a>
                </li>
            </c:forEach>
            <li class="page-item ${orders.hasNext ? '' : 'disabled'}">
                <a class="page-link" href="${ctx}/admin/orders?status=<c:out value='${statusFilter}'/>&page=${orders.nextPage}">Next</a>
            </li>
        </ul>
    </nav>
</c:if>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
