<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<h1 class="h3 mb-1">My account</h1>
<p class="text-muted mb-4">Manage your details, your password and your delivery addresses.</p>

<c:if test="${not empty formError}">
    <div class="alert alert-danger" role="alert"><c:out value="${formError}"/></div>
</c:if>

<div class="row g-4">
    <div class="col-lg-4">
        <div class="ss-card p-4 mb-3">
            <div class="d-flex align-items-center gap-3 mb-3">
                <span class="ss-avatar" style="width:52px;height:52px;font-size:1.1rem">
                    <c:out value="${profile.initials}"/>
                </span>
                <div>
                    <h2 class="h6 mb-0"><c:out value="${profile.fullName}"/></h2>
                    <span class="small text-muted"><c:out value="${profile.email}"/></span>
                </div>
            </div>
            <dl class="row mb-0 small">
                <dt class="col-5 text-muted">Role</dt>
                <dd class="col-7"><c:out value="${profile.role}"/></dd>
                <dt class="col-5 text-muted">Member since</dt>
                <dd class="col-7"><c:out value="${profile.createdAt}"/></dd>
            </dl>
        </div>
        <div class="ss-card p-4">
            <h2 class="h6 mb-2">Quick links</h2>
            <a class="d-block py-1" href="${ctx}/orders">My orders</a>
            <a class="d-block py-1" href="${ctx}/account/addresses">Address book</a>
            <a class="d-block py-1" href="${ctx}/cart">Shopping cart</a>
        </div>
    </div>

    <div class="col-lg-8">
        <div class="ss-card p-4 mb-4">
            <h2 class="h5 mb-3">Profile details</h2>
            <form method="post" action="${ctx}/account" class="row g-3">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="updateProfile">
                <div class="col-md-6">
                    <label class="ss-form-label" for="fullName">Full name</label>
                    <input class="form-control" id="fullName" name="fullName" required maxlength="120"
                           value="<c:out value='${profile.fullName}'/>">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="phone">Phone</label>
                    <input class="form-control" id="phone" name="phone" maxlength="40"
                           value="<c:out value='${profile.phone}'/>">
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="email-readonly">Email address</label>
                    <input class="form-control" id="email-readonly" value="<c:out value='${profile.email}'/>"
                           disabled>
                    <div class="form-text">Contact support to change the email on your account.</div>
                </div>
                <div class="col-12">
                    <button class="btn ss-btn-primary" type="submit">Save profile</button>
                </div>
            </form>
        </div>

        <div class="ss-card p-4">
            <h2 class="h5 mb-3">Change password</h2>
            <form method="post" action="${ctx}/account" class="row g-3">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="changePassword">
                <div class="col-md-4">
                    <label class="ss-form-label" for="currentPassword">Current password</label>
                    <input class="form-control" type="password" id="currentPassword" name="currentPassword"
                           required autocomplete="current-password">
                </div>
                <div class="col-md-4">
                    <label class="ss-form-label" for="newPassword">New password</label>
                    <input class="form-control" type="password" id="newPassword" name="newPassword" required
                           minlength="8" autocomplete="new-password">
                </div>
                <div class="col-md-4">
                    <label class="ss-form-label" for="confirmPassword">Confirm new password</label>
                    <input class="form-control" type="password" id="confirmPassword" name="confirmPassword"
                           required minlength="8" autocomplete="new-password">
                </div>
                <div class="col-12">
                    <button class="btn ss-btn-primary" type="submit">Update password</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
