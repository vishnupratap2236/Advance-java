<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-8 col-lg-6">
        <div class="ss-card-lg p-4 p-lg-5">
            <h1 class="h3 mb-1">Create your account</h1>
            <p class="text-muted mb-4">It takes a minute and keeps your orders and addresses in one place.</p>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger" role="alert"><c:out value="${errorMessage}"/></div>
            </c:if>

            <form method="post" action="${ctx}/register" class="row g-3">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <div class="col-12">
                    <label class="ss-form-label" for="fullName">Full name</label>
                    <input class="form-control" id="fullName" name="fullName" required maxlength="120"
                           value="<c:out value='${fullName}'/>" autofocus>
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="email">Email address</label>
                    <input class="form-control" type="email" id="email" name="email" required
                           autocomplete="email" value="<c:out value='${email}'/>">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="phone">Phone (optional)</label>
                    <input class="form-control" id="phone" name="phone" maxlength="40"
                           value="<c:out value='${phone}'/>">
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="password">Password</label>
                    <input class="form-control" type="password" id="password" name="password" required
                           minlength="8" autocomplete="new-password">
                    <div class="form-text">At least 8 characters with letters and digits.</div>
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="confirmPassword">Confirm password</label>
                    <input class="form-control" type="password" id="confirmPassword" name="confirmPassword"
                           required minlength="8" autocomplete="new-password">
                </div>
                <div class="col-12">
                    <button class="btn ss-btn-primary w-100" type="submit">Create account</button>
                </div>
            </form>

            <p class="text-center text-muted mt-4 mb-0">
                Already have an account? <a href="${ctx}/login" class="fw-semibold">Sign in</a>
            </p>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
