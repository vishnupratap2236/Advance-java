<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<h1 class="h3 mb-1">My wishlist</h1>
<p class="text-muted mb-4">Products you saved for later.</p>

<c:choose>
    <c:when test="${empty wishlist}">
        <div class="ss-card ss-empty">
            <div class="ss-empty-icon">
                <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8l8.8 8.8 8.8-8.8a5.5 5.5 0 0 0 0-7.8z"/>
                </svg>
            </div>
            <h2 class="h5">Your wishlist is empty</h2>
            <p class="mb-3">Tap the heart on any product to save it here.</p>
            <a class="btn ss-btn-primary" href="${ctx}/products">Browse products</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row g-4">
            <c:forEach items="${wishlist}" var="product">
                <div class="col-6 col-md-4 col-xl-3">
                    <c:set var="product" value="${product}" scope="request"/><jsp:include page="/WEB-INF/views/fragments/product-card.jsp"/>
                    <form method="post" action="${ctx}/wishlist" class="mt-2">
                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                        <input type="hidden" name="productId" value="${product.id}">
                        <button class="btn btn-sm ss-btn-ghost w-100" type="submit">Remove from wishlist</button>
                    </form>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
