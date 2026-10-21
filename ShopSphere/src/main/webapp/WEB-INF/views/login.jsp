<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-6 col-lg-5">
        <div class="ss-card-lg p-4 p-lg-5">
            <h1 class="h3 mb-1">Welcome back</h1>
            <p class="text-muted mb-4">Sign in to your ShopSphere account.</p>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger" role="alert"><c:out value="${errorMessage}"/></div>
            </c:if>

            <form method="post" action="${ctx}/login" class="row g-3">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <c:if test="${not empty redirectTo}">
                    <input type="hidden" name="redirectTo" value="<c:out value='${redirectTo}'/>">
                </c:if>
                <div class="col-12">
                    <label class="ss-form-label" for="email">Email address</label>
                    <input class="form-control" type="email" id="email" name="email" required autocomplete="email"
                           value="<c:out value='${email}'/>" autofocus>
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="password">Password</label>
                    <input class="form-control" type="password" id="password" name="password" required
                           autocomplete="current-password">
                </div>
                <div class="col-12">
                    <button class="btn ss-btn-primary w-100" type="submit">Sign in</button>
                </div>
            </form>

            <p class="text-center text-muted mt-4 mb-0">
                New to ShopSphere?
                <a href="${ctx}/register" class="fw-semibold">Create an account</a>
            </p>
        </div>

        <div class="ss-card p-3 mt-3 small text-muted">
            <strong class="d-block mb-1">Demo checkout</strong>
            This is a demonstration shop. No real payment is processed and no card data is collected.
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
