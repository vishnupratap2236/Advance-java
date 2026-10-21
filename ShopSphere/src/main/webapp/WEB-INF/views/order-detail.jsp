<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<c:set var="o" value="${order}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<nav class="ss-breadcrumb mb-3" aria-label="Breadcrumb">
    <a href="${ctx}/orders">My orders</a> / <span><c:out value="${o.orderNumber}"/></span>
</nav>

<div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
    <div>
        <h1 class="h3 mb-1">Order <c:out value="${o.orderNumber}"/></h1>
        <p class="text-muted mb-0">Placed on <c:out value="${com.shopsphere.util.Money.formatDateTime(o.placedAt)}"/></p>
    </div>
    <div class="text-end">
        <span class="ss-status ${o.status.badgeClass}"><c:out value="${o.status.label}"/></span>
        <div class="mt-2">
            <span class="text-muted small d-block">Total</span>
            <span class="ss-price"><c:out value="${currencySymbol}"/><c:out value="${o.total}"/></span>
        </div>
    </div>
</div>

<div class="row g-4">
    <section class="col-lg-8">
        <div class="ss-card p-4 mb-4">
            <h2 class="h5 mb-3">Items</h2>
            <c:forEach items="${o.items}" var="item">
                <div class="d-flex gap-3 mb-3">
                    <img class="ss-thumb" src="${ctx}/<c:out value='${item.imageUrl}'/>"
                         alt="<c:out value='${item.productName}'/>">
                    <div class="flex-grow-1">
                        <div class="fw-semibold"><c:out value="${item.productName}"/></div>
                        <div class="small text-muted">
                            SKU <c:out value="${item.productSku}"/> &middot;
                            <c:out value="${item.quantity}"/> &times;
                            <c:out value="${currencySymbol}"/><c:out value="${item.unitPrice}"/>
                        </div>
                    </div>
                    <div class="text-end">
                        <strong><c:out value="${currencySymbol}"/><c:out value="${item.lineTotal}"/></strong>
                    </div>
                </div>
            </c:forEach>
            <hr>
            <div class="d-flex justify-content-between mb-2">
                <span class="text-muted">Subtotal</span>
                <span><c:out value="${currencySymbol}"/><c:out value="${o.subtotal}"/></span>
            </div>
            <c:if test="${o.discount gt 0}">
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-muted">Discount (<c:out value="${o.couponCode}"/>)</span>
                    <span class="text-success">-&minus;<c:out value="${currencySymbol}"/><c:out value="${o.discount}"/></span>
                </div>
            </c:if>
            <div class="d-flex justify-content-between mb-2">
                <span class="text-muted">Shipping</span>
                <span>
                    <c:choose>
                        <c:when test="${o.shippingFee gt 0}"><c:out value="${currencySymbol}"/><c:out value="${o.shippingFee}"/></c:when>
                        <c:otherwise>Free</c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="d-flex justify-content-between mb-3">
                <span class="text-muted">Tax</span>
                <span><c:out value="${currencySymbol}"/><c:out value="${o.tax}"/></span>
            </div>
            <div class="d-flex justify-content-between align-items-baseline">
                <strong>Total</strong>
                <span class="ss-price" style="font-size:1.35rem">
                    <c:out value="${currencySymbol}"/><c:out value="${o.total}"/>
                </span>
            </div>
        </div>

        <div class="ss-card p-4">
            <h2 class="h5 mb-3">Status timeline</h2>
            <ul class="ss-timeline">
                <c:forEach items="${o.history}" var="event">
                    <li>
                        <div class="fw-semibold"><c:out value="${event.status.label}"/></div>
                        <div class="small text-muted">
                            <c:out value="${com.shopsphere.util.Money.formatDateTime(event.createdAt)}"/>
                            <c:if test="${not empty event.changedByName}"> &middot; by <c:out value="${event.changedByName}"/></c:if>
                        </div>
                        <c:if test="${not empty event.note}">
                            <div class="small mt-1"><c:out value="${event.note}"/></div>
                        </c:if>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </section>

    <aside class="col-lg-4">
        <div class="ss-card p-4 mb-3">
            <h2 class="h6 mb-3">Delivery address</h2>
            <p class="mb-1 fw-semibold"><c:out value="${o.recipientName}"/></p>
            <p class="mb-1 small text-muted"><c:out value="${o.phone}"/></p>
            <p class="mb-0 small text-muted"><c:out value="${o.shippingAddress}"/></p>
            <c:if test="${not empty o.notes}">
                <p class="small mt-2 mb-0"><strong>Note:</strong> <c:out value="${o.notes}"/></p>
            </c:if>
        </div>
        <div class="ss-card p-4 mb-3">
            <h2 class="h6 mb-2">Payment</h2>
            <p class="mb-1"><c:out value="${o.paymentMethod}"/></p>
            <span class="ss-chip ss-chip-muted"><c:out value="${o.paymentStatus}"/></span>
        </div>
        <c:if test="${o.status.cancellable}">
            <form method="post" action="${ctx}/order" onsubmit="return confirm('Cancel this order?');">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="cancel">
                <input type="hidden" name="id" value="${o.id}">
                <button class="btn ss-btn-ghost w-100" type="submit">Cancel this order</button>
            </form>
        </c:if>
    </aside>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>

