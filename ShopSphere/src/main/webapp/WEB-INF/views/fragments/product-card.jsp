<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${contextPath}"/>
<c:set var="p" value="${product}"/>
<article class="ss-product-card">
    <a class="ss-product-media" href="${ctx}/product?id=${p.id}" aria-label="<c:out value='${p.name}'/>">
        <c:choose>
            <c:when test="${not empty p.imageUrl}">
                <img src="${ctx}/<c:out value='${p.imageUrl}'/>" alt="<c:out value='${p.name}'/>" loading="lazy">
            </c:when>
            <c:otherwise>
                <img src="${ctx}/images/placeholder.svg" alt="<c:out value='${p.name}'/>" loading="lazy">
            </c:otherwise>
        </c:choose>
        <c:if test="${p.onSale}">
            <span class="ss-chip ss-chip-danger position-absolute top-0 end-0 m-2">-${p.discountPercent}%</span>
        </c:if>
        <c:if test="${p.outOfStock}">
            <span class="ss-chip ss-chip-muted position-absolute bottom-0 start-0 m-2">Out of stock</span>
        </c:if>
    </a>
    <div class="ss-product-body">
        <span class="ss-chip ss-chip-brand align-self-start mb-2"><c:out value="${p.categoryName}"/></span>
        <a class="ss-product-title" href="${ctx}/product?id=${p.id}"><c:out value="${p.name}"/></a>
        <div class="d-flex align-items-center gap-1 mt-1 mb-2" style="font-size:.82rem;color:var(--ss-muted)">
            <span aria-hidden="true">&#9733;</span>
            <span><c:out value="${p.rating}"/></span>
            <span class="ms-1">(<c:out value="${p.reviewCount}"/> reviews)</span>
        </div>
        <div class="mt-auto d-flex align-items-baseline gap-2">
            <span class="ss-price"><c:out value="${currencySymbol}"/><c:out value="${p.price}"/></span>
            <c:if test="${p.onSale}">
                <span class="ss-price-strike"><c:out value="${currencySymbol}"/><c:out value="${p.comparePrice}"/></span>
            </c:if>
        </div>
        <c:if test="${p.inStock}">
            <span class="small mt-1" style="color:#15803d">In stock (<c:out value="${p.stock}"/>)</span>
        </c:if>
        <c:if test="${p.lowStock}">
            <span class="small mt-1" style="color:#b45309">Only <c:out value="${p.stock}"/> left</span>
        </c:if>
    </div>
</article>
