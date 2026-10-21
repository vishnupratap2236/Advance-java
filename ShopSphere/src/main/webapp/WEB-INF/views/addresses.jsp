<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<h1 class="h3 mb-1">Address book</h1>
<p class="text-muted mb-4">Save the addresses you use most often for faster checkout.</p>

<div class="row g-4">
    <div class="col-lg-7">
        <c:choose>
            <c:when test="${empty addresses}">
                <div class="ss-card ss-empty">
                    <div class="ss-empty-icon">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="1.7" stroke-linecap="round">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                        </svg>
                    </div>
                    <h2 class="h5">No addresses saved yet</h2>
                    <p class="mb-0">Add an address using the form on the right.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="d-flex flex-column gap-3">
                    <c:forEach items="${addresses}" var="address">
                        <div class="ss-card p-3 d-flex flex-wrap justify-content-between gap-3">
                            <div>
                                <div class="d-flex align-items-center gap-2 mb-1">
                                    <strong><c:out value="${address.recipientName}"/></strong>
                                    <c:if test="${address.defaultAddress}">
                                        <span class="ss-chip ss-chip-brand">Default</span>
                                    </c:if>
                                </div>
                                <div class="small text-muted"><c:out value="${address.phone}"/></div>
                                <div class="small text-muted"><c:out value="${address.formatted}"/></div>
                            </div>
                            <div class="d-flex align-items-start gap-2">
                                <c:if test="${not address.defaultAddress}">
                                    <form method="post" action="${ctx}/account/addresses">
                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                        <input type="hidden" name="action" value="setDefault">
                                        <input type="hidden" name="id" value="${address.id}">
                                        <button class="btn ss-btn-ghost btn-sm" type="submit">Make default</button>
                                    </form>
                                </c:if>
                                <form method="post" action="${ctx}/account/addresses"
                                      onsubmit="return confirm('Delete this address?');">
                                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${address.id}">
                                    <button class="btn btn-link text-danger btn-sm text-decoration-none" type="submit">
                                        Delete
                                    </button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="col-lg-5">
        <div class="ss-card p-4">
            <h2 class="h5 mb-3">Add a new address</h2>
            <form method="post" action="${ctx}/account/addresses" class="row g-3">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="add">
                <div class="col-12">
                    <label class="ss-form-label" for="recipientName">Recipient name</label>
                    <input class="form-control" id="recipientName" name="recipientName" required maxlength="120">
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="phone">Phone</label>
                    <input class="form-control" id="phone" name="phone" required maxlength="40"
                           placeholder="+91 98765 43210">
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="line1">Street address</label>
                    <input class="form-control" id="line1" name="line1" required maxlength="200">
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="line2">Apartment, landmark (optional)</label>
                    <input class="form-control" id="line2" name="line2" maxlength="200">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="city">City</label>
                    <input class="form-control" id="city" name="city" required maxlength="100">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="state">State</label>
                    <input class="form-control" id="state" name="state" required maxlength="100">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="postalCode">Postal code</label>
                    <input class="form-control" id="postalCode" name="postalCode" required maxlength="20">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="country">Country</label>
                    <input class="form-control" id="country" name="country" maxlength="100" value="India">
                </div>
                <div class="col-12">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" id="isDefault" name="isDefault" value="true">
                        <label class="form-check-label" for="isDefault">Use as my default address</label>
                    </div>
                </div>
                <div class="col-12">
                    <button class="btn ss-btn-primary" type="submit">Save address</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
