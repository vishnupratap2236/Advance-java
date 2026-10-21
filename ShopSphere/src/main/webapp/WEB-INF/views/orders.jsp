<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<h1 class="h3 mb-1">My orders</h1>
<p class="text-muted mb-4">Track the status of every order you have placed.</p>

<c:choose>
    <c:when test="${orders.isEmpty()}">
        <div class="ss-card ss-empty">
            <div class="ss-empty-icon">
                <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="1.7" stroke-linecap="round">
                    <path d="M6 2h12l2 5H4z"/><path d="M4 7v13a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7"/><path d="M9 12h6"/>
                </svg>
            </div>
            <h2 class="h5">No orders yet</h2>
            <p class="mb-3">When you place an order it will show up here with its delivery status.</p>
            <a class="btn ss-btn-primary" href="${ctx}/products">Start shopping</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="ss-card overflow-hidden">
            <div class="table-responsive">
                <table class="table ss-table align-middle">
                    <thead>
                    <tr>
                        <th scope="col">Order</th>
                        <th scope="col">Date</th>
                        <th scope="col">Items</th>
                        <th scope="col">Total</th>
                        <th scope="col">Status</th>
                        <th scope="col"><span class="visually-hidden">Actions</span></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${orders.items}" var="order">
                        <tr>
                            <td class="fw-semibold"><c:out value="${order.orderNumber}"/></td>
                            <td class="text-muted">
                                <c:out value="${com.shopsphere.util.Money.formatDateTime(order.placedAt)}"/>
                            </td>
                            <td class="text-muted"><c:out value="${order.itemCount}"/> item(s)</td>
                            <td class="fw-semibold">
                                <c:out value="${currencySymbol}"/><c:out value="${order.total}"/>
                            </td>
                            <td>
                                <span class="ss-status ${order.status.badgeClass}">
                                    <c:out value="${order.status.label}"/>
                                </span>
                            </td>
                            <td class="text-end">
                                <a class="btn ss-btn-ghost btn-sm" href="${ctx}/order?id=${order.id}">View</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <c:if test="${orders.totalPages > 1}">
            <nav class="mt-4" aria-label="Order pages">
                <ul class="pagination ss-pagination justify-content-center">
                    <li class="page-item ${orders.hasPrevious ? '' : 'disabled'}">
                        <a class="page-link" href="${ctx}/orders?page=${orders.previousPage}">Previous</a>
                    </li>
                    <c:forEach begin="1" end="${orders.totalPages}" var="pageNo">
                        <li class="page-item ${pageNo == orders.page ? 'active' : ''}">
                            <a class="page-link" href="${ctx}/orders?page=${pageNo}"><c:out value="${pageNo}"/></a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${orders.hasNext ? '' : 'disabled'}">
                        <a class="page-link" href="${ctx}/orders?page=${orders.nextPage}">Next</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>

