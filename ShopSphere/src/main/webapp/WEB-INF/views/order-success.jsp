<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<c:set var="o" value="${order}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="ss-card-lg p-4 p-lg-5 mb-4 text-center">
    <div class="ss-empty-icon mx-auto" style="background:#dcfce7;color:#15803d">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
             stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 6L9 17l-5-5"/>
        </svg>
    </div>
    <h1 class="h3 mb-2">Thank you, your order is confirmed</h1>
    <p class="text-muted mb-4">
        We have emailed a confirmation to <c:out value="${currentUser.email}"/>.
        You can follow the delivery status from your order history at any time.
    </p>

    <div class="row g-3 text-start justify-content-center">
        <div class="col-md-3 col-6">
            <div class="ss-card p-3 h-100">
                <span class="small text-muted d-block">Order number</span>
                <strong><c:out value="${o.orderNumber}"/></strong>
            </div>
        </div>
        <div class="col-md-3 col-6">
            <div class="ss-card p-3 h-100">
                <span class="small text-muted d-block">Order total</span>
                <strong><c:out value="${currencySymbol}"/><c:out value="${o.total}"/></strong>
            </div>
        </div>
        <div class="col-md-3 col-6">
            <div class="ss-card p-3 h-100">
                <span class="small text-muted d-block">Payment</span>
                <strong><c:out value="${o.paymentMethod}"/></strong>
            </div>
        </div>
        <div class="col-md-3 col-6">
            <div class="ss-card p-3 h-100">
                <span class="small text-muted d-block">Status</span>
                <strong><c:out value="${o.status.label}"/></strong>
            </div>
        </div>
    </div>

    <div class="d-flex flex-wrap justify-content-center gap-2 mt-4">
        <a class="btn ss-btn-primary" href="${ctx}/order?id=${o.id}">View order details</a>
        <a class="btn ss-btn-ghost" href="${ctx}/orders">Back to my orders</a>
        <a class="btn ss-btn-ghost" href="${ctx}/products">Continue shopping</a>
    </div>
</div>

<div class="row g-4">
    <div class="col-lg-7">
        <div class="ss-card p-4">
            <h2 class="h5 mb-3">What happens next</h2>
            <ul class="ss-timeline mb-0">
                <li>
                    <div class="fw-semibold">Order received</div>
                    <div class="small text-muted">We have your order and reserved the items.</div>
                </li>
                <li>
                    <div class="fw-semibold">Packing</div>
                    <div class="small text-muted">Your items are picked and packed in our warehouse.</div>
                </li>
                <li>
                    <div class="fw-semibold">Shipped</div>
                    <div class="small text-muted">The parcel leaves our warehouse and you get a tracking update.</div>
                </li>
                <li>
                    <div class="fw-semibold">Delivered</div>
                    <div class="small text-muted">
                        <c:choose>
                            <c:when test="${o.paymentMethod == 'COD'}">Pay the courier on delivery.</c:when>
                            <c:otherwise>Payment is confirmed in this demo environment.</c:otherwise>
                        </c:choose>
                    </div>
                </li>
            </ul>
        </div>
    </div>
    <div class="col-lg-5">
        <div class="ss-card p-4">
            <h2 class="h6 mb-3">Delivering to</h2>
            <p class="mb-1 fw-semibold"><c:out value="${o.recipientName}"/></p>
            <p class="mb-0 small text-muted"><c:out value="${o.shippingAddress}"/></p>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
