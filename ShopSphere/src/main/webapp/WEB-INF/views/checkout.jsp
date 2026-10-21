<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<nav class="ss-breadcrumb mb-3" aria-label="Breadcrumb">
    <a href="${ctx}/cart">Cart</a> / <span>Checkout</span>
</nav>

<h1 class="h3 mb-1">Checkout</h1>
<p class="text-muted mb-4">
    This is a demonstration checkout. Choose cash on delivery or the simulated card
    payment &mdash; no real money is moved and no card data is stored.
</p>

<c:if test="${not empty formError}">
    <div class="alert alert-danger" role="alert"><c:out value="${formError}"/></div>
</c:if>

<div class="row g-4">
    <section class="col-lg-7">
        <form method="post" action="${ctx}/checkout" class="ss-card p-4">
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
            <input type="hidden" name="action" value="placeOrder">

            <h2 class="h5 mb-3">Delivery address</h2>

            <c:if test="${not empty addresses}">
                <div class="mb-3">
                    <label class="ss-form-label" for="saved-address">Use a saved address</label>
                    <select class="form-select" id="saved-address" name="savedAddressId">
                        <option value="">Enter a new address</option>
                        <c:forEach items="${addresses}" var="saved">
                            <option value="${saved.id}">
                                <c:out value="${saved.recipientName}"/> &middot; <c:out value="${saved.formatted}"/>
                                <c:if test="${saved.defaultAddress}"> (default)</c:if>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <hr class="my-4">
            </c:if>

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="ss-form-label" for="recipientName">Recipient name</label>
                    <input class="form-control" id="recipientName" name="recipientName" required maxlength="120"
                           value="<c:out value='${empty address ? "" : address.recipientName}'/>">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="phone">Contact phone</label>
                    <input class="form-control" id="phone" name="phone" required maxlength="40"
                           placeholder="+91 98765 43210"
                           value="<c:out value='${empty address ? "" : address.phone}'/>">
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="line1">Street address</label>
                    <input class="form-control" id="line1" name="line1" required maxlength="200"
                           value="<c:out value='${empty address ? "" : address.line1}'/>">
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="line2">Apartment, landmark (optional)</label>
                    <input class="form-control" id="line2" name="line2" maxlength="200"
                           value="<c:out value='${empty address ? "" : address.line2}'/>">
                </div>
                <div class="col-md-4">
                    <label class="ss-form-label" for="city">City</label>
                    <input class="form-control" id="city" name="city" required maxlength="100"
                           value="<c:out value='${empty address ? "" : address.city}'/>">
                </div>
                <div class="col-md-4">
                    <label class="ss-form-label" for="state">State</label>
                    <input class="form-control" id="state" name="state" required maxlength="100"
                           value="<c:out value='${empty address ? "" : address.state}'/>">
                </div>
                <div class="col-md-4">
                    <label class="ss-form-label" for="postalCode">Postal code</label>
                    <input class="form-control" id="postalCode" name="postalCode" required maxlength="20"
                           value="<c:out value='${empty address ? "" : address.postalCode}'/>">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="country">Country</label>
                    <input class="form-control" id="country" name="country" maxlength="100"
                           value="<c:out value='${empty address ? "" : address.country}'/>">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="notes">Delivery notes (optional)</label>
                    <input class="form-control" id="notes" name="notes" maxlength="500"
                           placeholder="Ring the bell twice">
                </div>
            </div>

            <h2 class="h5 mt-4 mb-3">Payment method</h2>
            <div class="d-flex flex-column gap-2 mb-4">
                <label class="ss-card p-3 d-flex align-items-center gap-3 mb-0">
                    <input type="radio" name="paymentMethod" value="COD" checked>
                    <span>
                        <strong class="d-block">Cash on delivery</strong>
                        <span class="small text-muted">Pay the courier when the parcel arrives.</span>
                    </span>
                </label>
                <label class="ss-card p-3 d-flex align-items-center gap-3 mb-0">
                    <input type="radio" name="paymentMethod" value="DEMO_CARD">
                    <span>
                        <strong class="d-block">Demo card payment</strong>
                        <span class="small text-muted">Simulated payment &mdash; no card details are collected.</span>
                    </span>
                </label>
                <label class="ss-card p-3 d-flex align-items-center gap-3 mb-0">
                    <input type="radio" name="paymentMethod" value="BANK_TRANSFER">
                    <span>
                        <strong class="d-block">Bank transfer (demo)</strong>
                        <span class="small text-muted">The order is marked as awaiting payment.</span>
                    </span>
                </label>
            </div>

            <button class="btn ss-btn-primary btn-lg px-4" type="submit">Place order</button>
            <a class="btn ss-btn-ghost btn-lg ms-2" href="${ctx}/cart">Back to cart</a>
        </form>
    </section>

    <aside class="col-lg-5">
        <div class="ss-card p-4">
            <h2 class="h5 mb-3">Your order</h2>
            <c:forEach items="${cart.items}" var="item">
                <div class="d-flex gap-3 mb-3">
                    <img class="ss-thumb" src="${ctx}/<c:out value='${item.product.imageUrl}'/>"
                         alt="<c:out value='${item.product.name}'/>">
                    <div class="flex-grow-1">
                        <div class="fw-semibold"><c:out value="${item.product.name}"/></div>
                        <div class="small text-muted">
                            Qty <c:out value="${item.quantity}"/> &times;
                            <c:out value="${currencySymbol}"/><c:out value="${item.product.price}"/>
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
                <span><c:out value="${currencySymbol}"/><c:out value="${cart.subtotal}"/></span>
            </div>
            <c:if test="${cart.discount gt 0}">
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-muted">Coupon (<c:out value="${cart.couponCode}"/>)</span>
                    <span class="text-success">-&minus;<c:out value="${currencySymbol}"/><c:out value="${cart.discount}"/></span>
                </div>
            </c:if>
            <div class="d-flex justify-content-between mb-2">
                <span class="text-muted">Shipping</span>
                <span>
                    <c:choose>
                        <c:when test="${cart.shippingFee gt 0}"><c:out value="${currencySymbol}"/><c:out value="${cart.shippingFee}"/></c:when>
                        <c:otherwise>Free</c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="d-flex justify-content-between mb-3">
                <span class="text-muted">Tax (GST)</span>
                <span><c:out value="${currencySymbol}"/><c:out value="${cart.tax}"/></span>
            </div>
            <div class="d-flex justify-content-between align-items-baseline">
                <strong>Total</strong>
                <span class="ss-price" style="font-size:1.4rem">
                    <c:out value="${currencySymbol}"/><c:out value="${cart.total}"/>
                </span>
            </div>
        </div>
    </aside>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
