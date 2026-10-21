<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${empty contextPath ? pageContext.request.contextPath : contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-7 col-lg-5">
        <div class="ss-card-lg p-4 p-lg-5 text-center">
            <div class="ss-empty-icon mx-auto" style="background:#fee2e2;color:#b91c1c">
                <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round">
                    <circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/>
                </svg>
            </div>
            <h1 class="h4 mb-2">Something went wrong</h1>
            <p class="text-muted mb-4">
                <c:out value="${empty errorMessage ? 'The request could not be completed. Please try again.' : errorMessage}"/>
            </p>
            <div class="d-flex flex-wrap justify-content-center gap-2">
                <a class="btn ss-btn-primary" href="${ctx}/">Back to the shop</a>
                <a class="btn ss-btn-ghost" href="${ctx}/products">Browse products</a>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
