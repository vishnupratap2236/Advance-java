<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<h1 class="h3 mb-1">Shopping cart</h1>
<p class="text-muted mb-4">Review the items before you continue to checkout.</p>

<c:choose>
    <c:when test="${cart.isEmpty()}">
        <div class="ss-card ss-empty">
            <div class="ss-empty-icon">
                <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="1.7" stroke-linecap="round">
                    <circle cx="9" cy="21" r="1.4"/><circle cx="19" cy="21" r="1.4"/>
                    <path d="M1 1h4l2.7 13.4a2 2 0 0 0 2 1.6h9.7a2 2 0 0 0 2-1.6L23 6H6"/>
                </svg>
            </div>
            <h2 class="h5">Your cart is empty</h2>
            <p class="mb-3">Browse the catalogue and add something you like.</p>
            <a class="btn ss-btn-primary" href="${ctx}/products">Start shopping</a>
        </div>
    </c:when>
    <c:otherwise>
        <c:if test="${not empty cart.warnings}">
            <div class="alert alert-warning">
                <strong>Please note:</strong>
                <ul class="mb-0 mt-1">
                    <c:forEach items="${cart.warnings}" var="warning">
                        <li><c:out value="${warning}"/></li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>

        <div class="row g-4">
            <section class="col-lg-8">
                <div class="ss-card overflow-hidden">
                    <c:forEach items="${cart.items}" var="item">
                        <div class="d-flex flex-wrap align-items-center gap-3 p-3 border-bottom">
                            <img class="ss-thumb" src="${ctx}/<c:out value='${item.product.imageUrl}'/>"
                                 alt="<c:out value='${item.product.name}'/>">
                            <div class="flex-grow-1">
                                <a class="fw-semibold d-block" href="${ctx}/product?id=${item.productId}">
                                    <c:out value="${item.product.name}"/>
                                </a>
                                <span class="small text-muted">
                                    <c:out value="${currencySymbol}"/><c:out value="${item.product.price}"/> each
                                    <c:if test="${item.quantityAboveStock}">
                                        <span class="text-danger"> &middot; only <c:out value="${item.product.stock}"/> in stock</span>
                                    </c:if>
                                </span>
                            </div>
                            <form method="post" action="${ctx}/cart" class="d-flex align-items-center gap-2">
                                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="productId" value="${item.productId}">
                                <div class="ss-qty-box">
                                    <button type="button" data-qty-step="-1" aria-label="Decrease quantity">&minus;</button>
                                    <input type="number" name="quantity" value="${item.quantity}" min="0" max="20"
                                           aria-label="Quantity for <c:out value='${item.product.name}'/>">
                                    <button type="button" data-qty-step="1" aria-label="Increase quantity">+</button>
                                </div>
                                <button class="btn ss-btn-ghost btn-sm" type="submit">Update</button>
                            </form>
                            <div class="text-end" style="min-width:6.5rem">
                                <strong><c:out value="${currencySymbol}"/><c:out value="${item.lineTotal}"/></strong>
                            </div>
                            <form method="post" action="${ctx}/cart">
                                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                <input type="hidden" name="action" value="remove">
                                <input type="hidden" name="productId" value="${item.productId}">
                                <button class="btn btn-link text-danger p-0" type="submit" aria-label="Remove item">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                         stroke-width="2" stroke-linecap="round">
                                        <path d="M18 6L6 18M6 6l12 12"/>
                                    </svg>
                                </button>
                            </form>
                        </div>
                    </c:forEach>
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 p-3">
                        <a class="btn ss-btn-ghost" href="${ctx}/products">Continue shopping</a>
                        <form method="post" action="${ctx}/cart"
                              onsubmit="return confirm('Remove all items from your cart?');">
                            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                            <input type="hidden" name="action" value="clear">
                            <button class="btn btn-link text-danger text-decoration-none" type="submit">
                                Clear cart
                            </button>
                        </form>
                    </div>
                </div>
            </section>

            <aside class="col-lg-4">
                <div class="ss-card p-4">
                    <h2 class="h5 mb-3">Order summary</h2>

                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Subtotal (<c:out value="${cart.itemCount}"/> items)</span>
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
                    <hr>
                    <div class="d-flex justify-content-between align-items-baseline mb-3">
                        <strong>Total</strong>
                        <span class="ss-price" style="font-size:1.4rem">
                            <c:out value="${currencySymbol}"/><c:out value="${cart.total}"/>
                        </span>
                    </div>

                    <c:if test="${not empty cart.couponMessage}">
                        <p class="small text-danger mb-2"><c:out value="${cart.couponMessage}"/></p>
                    </c:if>

                    <form method="get" action="${ctx}/cart" class="d-flex gap-2 mb-3">
                        <label class="visually-hidden" for="coupon">Coupon code</label>
                        <input type="text" class="form-control" id="coupon" name="coupon" placeholder="Coupon code"
                               value="<c:out value='${couponCode}'/>">
                        <button class="btn ss-btn-ghost" type="submit">Apply</button>
                    </form>

                    <a class="btn ss-btn-primary w-100" href="${ctx}/checkout">Proceed to checkout</a>
                    <p class="small text-muted mt-3 mb-0">
                        Totals are calculated on the server from the current product prices.
                    </p>
                </div>
            </aside>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
